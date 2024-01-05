package service

import com.typesafe.scalalogging.LazyLogging
import io.circe
import io.circe.*
import io.circe.syntax.*
import io.circe.generic.auto.*
import io.circe.parser.*
import models.{Item, ItemDto, ItemsDto}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.*
import repositories.kafka.KafkaProducerRepository
import repositories.kafka.model.{DetectedObject, ImageRecognitionJobMessage}
import repositories.{GoogleBucketRepository, GoogleFireStoreRepository}
import utils.{ImageHelper, ImageResizer}
import com.github.nscala_time.time.Imports.*
import scala.concurrent.duration.*
import java.nio.file.{Files, Path}
import java.security.MessageDigest
import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class ItemService @Inject() (
    googleBucketRepository: GoogleBucketRepository,
    googleFireStoreRepository: GoogleFireStoreRepository,
    kafkaProducerRepository: KafkaProducerRepository
) extends LazyLogging:

  def getAllItemMetadata(projectId: String): Future[ItemsDto] =
    for
      items <- googleFireStoreRepository.findAll(projectId)
      itemsDto = items.map(item => ItemDto.from(item))
    yield ItemsDto(itemsDto)

  def addItem(projectId: String, filePart: FilePart[TemporaryFile]): Unit =
    val path = ImageResizer.resize(filePart.ref.path)
    try {
      val fileSizeOld = Files.size(filePart.ref.path)
      val fileSize = Files.size(path)
      logger.info(s"Resized image. [old:'$fileSizeOld', new:'$fileSize']")
    } catch {
      case e: Exception =>
        logger.info("Unable to get file size: " + e.getMessage)
        return
    }

    addItem(projectId, path)

  def addItem(projectId: String, path: Path): Unit =
    val contentType: String = Option(Files.probeContentType(path)) match
      case Some(contentType) => contentType
      case None              => "image/png"

    val bucketId = MessageDigest
      .getInstance("SHA-256")
      .digest(System.nanoTime().toString.getBytes)
      .map("%02X".format(_))
      .mkString
      .take(10)

    // save image
    googleBucketRepository.upload(projectId, bucketId, path)

    // save meta data
    val newItem = new Item("will_be_filled_with_document_id", contentType, DateTime.now().toString, bucketId, "")
    val savedItem = Await.result(googleFireStoreRepository.save(projectId, newItem), 2.seconds)
    logger.info(s"Save image and send to imageRecognitionApp. [projectId:'$projectId', itemId: '${savedItem.itemId}']")

    // send imageRecognitionApp analyse job
    val message = ImageRecognitionJobMessage(projectId, bucketId, ImageHelper.readImageFromPath(path, contentType))
    kafkaProducerRepository.sendToImageRecognitionApp(message)

  def getItem(projectId: String, itemId: String): Future[Option[Item]] =
    googleFireStoreRepository.findById(projectId, itemId)

  def deleteItem(projectId: String, itemId: String): Future[Option[String]] =
    for
      maybeFileItem <- googleFireStoreRepository.findById(projectId, itemId)
      result <- maybeFileItem match
        case None =>
          Future.failed(new IllegalArgumentException(s"No item found. [projectId: '$projectId', itemId: '$itemId']"))
        case Some(_) =>
          googleBucketRepository.delete(projectId, itemId)
          googleFireStoreRepository
            .delete(projectId, itemId)
            .map(_ => Some(itemId))
    yield result

  def deleteProject(projectId: String): Unit =
    googleBucketRepository.deleteAll(projectId)
    googleFireStoreRepository.deleteAll(projectId)

  def saveImageRecognition(projectId: String, bucketId: String, detectedObject: List[DetectedObject]): Unit =
    val item = googleFireStoreRepository.findByBucketId(projectId, bucketId)
    val documentId = Await.result(item, 2.seconds)

    documentId.map(_.itemId) match
      case None =>
        logger.info(
          s"Could not find bucketId to update detectedObjects. " +
            s"[projectId: '$projectId', bucketId: '$bucketId']"
        )
      case Some(id) =>
        val detectedObjectSerialized = detectedObject.asJson.noSpaces
        try
          val updatedItem = googleFireStoreRepository
            .updateField_detectedObjectSerialized(projectId, id, detectedObjectSerialized)
          Await.result(updatedItem, 2.seconds)
        catch case e: Exception => logger.info(e.getMessage)
        logger.info(s"Update item with detectedObjects. [projectId: '$projectId', bucketId: '$bucketId']")
