package service

import com.typesafe.scalalogging.LazyLogging
import io.circe
import io.circe.*
import models.{Item, ItemDto, ItemsDto}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.*
import repositories.kafka.KafkaProducerRepository
import repositories.kafka.model.{DetectedObject, ImageRecognitionJobMessage}
import repositories.{GoogleBucketRepository, GoogleFireStoreRepository}
import utils.{ImageHelper, ImageResizer}

import java.nio.file.{Files, Path}
import java.security.MessageDigest
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ItemService @Inject()(
    googleBucketRepository: GoogleBucketRepository,
    googleFireStoreRepository: GoogleFireStoreRepository,
    kafkaProducerRepository: KafkaProducerRepository
) extends LazyLogging:

  def getAllItemMetadata(projectId: String): Future[ItemsDto] =
    for
      items <- googleFireStoreRepository.findAll(projectId)
      itemsDto = items.map(item => ItemDto.from(item))
    yield ItemsDto(itemsDto)

  def search(projectId: String, fileName: String): Future[ItemsDto] =
    for
      items <- googleFireStoreRepository.search(projectId, fileName)
      itemsDto = items.map(item => ItemDto.from(item))
    yield ItemsDto(itemsDto)

  def addItem(projectId: String, filePart: FilePart[TemporaryFile]): Unit =
    val path = ImageResizer.resize(filePart.ref.path)
    addItem(projectId, path)

  def addItem(projectId: String, path: Path): Unit =
    val fileName: String = path.getFileName.getFileName.toString
    val contentType: String = Option(Files.probeContentType(path)) match
      case Some(contentType) => contentType
      case None              => "image/png"

    logger.info(s"Save image and send to imageRecognitionApp. [projectId:'$projectId', fileName: '$fileName']")

    val bucketId = MessageDigest
      .getInstance("SHA-256")
      .digest(System.nanoTime().toString.getBytes ++ fileName.getBytes("UTF-8"))
      .map("%02X".format(_))
      .mkString

    // save image
    googleBucketRepository.upload(projectId, bucketId, path)

    // save meta data
    val newItem = new Item("-", fileName, contentType, bucketId)
    googleFireStoreRepository.save(projectId, newItem)

    // send imageRecognitionApp analyse job
    val message = ImageRecognitionJobMessage(projectId, bucketId, ImageHelper.readImageFromPath(path, contentType))
    kafkaProducerRepository.sendToImageRecognitionApp(message)

  def getItem(projectId: String, itemId: String): Future[Option[Item]] =
    googleFireStoreRepository.findById(projectId, itemId)

  def deleteItem(projectId: String, itemId: String): Future[Option[String]] =
    for
      maybeFileItem <- googleFireStoreRepository.findById(projectId, itemId)
      result <- maybeFileItem match
        case None => Future.failed(new IllegalArgumentException(
          s"No item found. [projectId: '$projectId', itemId: '$itemId']"))
        case Some(_) =>
          googleBucketRepository.delete(projectId, itemId)
          googleFireStoreRepository
            .delete(projectId, itemId)
            .map(_ => Some(itemId))
    yield result

  def deleteProject(projectId : String): Unit =
    googleBucketRepository.deleteAll(projectId)
    googleFireStoreRepository.deleteAll(projectId)

  def saveImageRecognition(bucketId: String, detectedObject: Array[DetectedObject]): Unit =
    googleFireStoreRepository.findById()
    logger.info(s"TODO save this ${bucketId} + ${detectedObject.mkString("Array(", ", ", ")")}")