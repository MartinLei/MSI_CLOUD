package service

import com.typesafe.scalalogging.LazyLogging
import io.circe
import io.circe.*
import models.{FileItem, FileItemDto, FileItemsDto}
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

class FileListService @Inject() (
    googleBucketRepository: GoogleBucketRepository,
    googleFireStoreRepository: GoogleFireStoreRepository,
    kafkaProducerRepository: KafkaProducerRepository
) extends LazyLogging:

  def getAllItemMetadata(projectId: String): Future[FileItemsDto] =
    for
      items <- googleFireStoreRepository.findAll(projectId)
      itemsDto = items.map(item => FileItemDto.from(item))
    yield FileItemsDto(itemsDto)

  def search(projectId: String, fileName: String): Future[FileItemsDto] =
    for
      items <- googleFireStoreRepository.search(projectId, fileName)
      itemsDto = items.map(item => FileItemDto.from(item))
    yield FileItemsDto(itemsDto)

  def addItem(projectId: String, filePart: FilePart[TemporaryFile]): Unit =
    val path = ImageResizer.resize(filePart.ref.path)
    addItem(projectId, path)

  def addItem(projectId: String, path: Path): Unit =
    val fileName: String = path.getFileName.getFileName.toString
    val contentType: String = Option(Files.probeContentType(path)) match
      case Some(contentType) => contentType
      case None              => "image/png"

    logger.info(s"Save image and send to imageRecognitionApp. [projectId:'$projectId', fileName: '$fileName']")

    val imageId = MessageDigest
      .getInstance("SHA-256")
      .digest(System.nanoTime().toString.getBytes ++ fileName.getBytes("UTF-8"))
      .map("%02X".format(_))
      .mkString

    // save image
    googleBucketRepository.upload(projectId, imageId, path)

    // save meta data
    val newItem = new FileItem("-", fileName, contentType, imageId)
    googleFireStoreRepository.save(projectId, newItem)

    // give image recognition app a job
    val message = ImageRecognitionJobMessage(imageId, ImageHelper.readImageFromPath(path, contentType))
    kafkaProducerRepository.sendToImageRecognitionApp(message)

  def getItem(projectId: String, itemId: String): Future[Option[FileItem]] =
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
    logger.info(s"TODO save this ${bucketId} + ${detectedObject.mkString("Array(", ", ", ")")}")
