package service

import com.typesafe.scalalogging.LazyLogging
import io.circe
import io.circe.parser.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import models.{FileItem, FileItemDto, FileItemsDto}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.*
import repositories.kafka.model.{DetectedObject, ImageRecognitionJobMessage, ImageRecognitionResultMessage, Message}
import repositories.kafka.KafkaProducerRepository
import repositories.{GoogleBucketRepository, GoogleFireStoreRepository}
import utils.{ImageHelper, ImageResizer}

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, IOException}
import java.nio.file.{Files, Path, Paths}
import java.security.MessageDigest
import javax.imageio.ImageIO
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileListService @Inject() (
    googleBucketRepository: GoogleBucketRepository,
    googleFireStoreRepository: GoogleFireStoreRepository,
    kafkaProducerRepository: KafkaProducerRepository
) extends LazyLogging:

  def getAllItemMetadata(projectId : String): Future[FileItemsDto] =
    for
      items <- googleFireStoreRepository.findAll(projectId)
      itemsDto = items.map(item => FileItemDto.from(item))
    yield FileItemsDto(itemsDto)

  def search(projectId: String, fileName : String): Future[FileItemsDto] =
    for
      items <- googleFireStoreRepository.search(projectId, fileName)
      itemsDto = items.map(item => FileItemDto.from(item))
    yield FileItemsDto(itemsDto)

  def addFileItem(projectId: String, filePart: FilePart[TemporaryFile]): Unit =
    val path = ImageResizer.resize(filePart.ref.path)
     addFileItem(projectId, path)

  def addFileItem(projectId: String, path: Path): Unit =
    val fileName: String = path.getFileName.getFileName.toString
    val contentType: String = Option(Files.probeContentType(path))
    match {
      case Some(contentType) => contentType
      case None => "image/png"
    }

    logger.info(s"Save image and send to imageRecognitionApp. [projectId:'$projectId', fileName: '$fileName']")

    val fileId = MessageDigest
      .getInstance("SHA-256")
      .digest(System.nanoTime().toString.getBytes ++ fileName.getBytes("UTF-8"))
      .map("%02X".format(_))
      .mkString

    // save image
    googleBucketRepository.upload(projectId, fileId,path)

    // save meta data
    val newItem = new FileItem("-", fileName, contentType, fileId)
    googleFireStoreRepository.save(projectId, newItem)

    // give image recognition app a job
    val message = ImageRecognitionJobMessage(fileId, ImageHelper.readImageFromPath(path, contentType))
    kafkaProducerRepository.sendToImageRecognitionApp(message)

  def getFileItem(projectId: String, documentId: String): Future[Option[FileItem]] =
    googleFireStoreRepository.findById(projectId, documentId)

  def deleteFileItem(projectId : String, documentId: String): Future[Option[String]] =
    for
      maybeFileItem <- googleFireStoreRepository.findById(projectId, documentId)
      result <- maybeFileItem match
        case None => Future.failed(new IllegalArgumentException(s"No fileItem with $documentId found"))
        case Some(_) =>
          googleBucketRepository.delete(projectId, documentId)
          googleFireStoreRepository
            .delete(projectId, documentId)
            .map(_ => Some(documentId))
    yield result

  def deleteAll(): Unit =
    googleBucketRepository.deleteAll("TODO")
    googleFireStoreRepository.deleteAll("TODO")


  def saveImageRecognition(bucketId: String, detectedObject: Array[DetectedObject]): Unit =
    logger.info(s"TODO save this ${bucketId} + ${detectedObject.mkString("Array(", ", ", ")")}")

