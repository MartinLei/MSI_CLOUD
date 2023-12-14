package service

import com.typesafe.scalalogging.LazyLogging
import models.{FileItem, FileItemDto, FileItemsDto}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.*
import repositories.kafka.model.ImageRecognitionMessage
import repositories.kafka.KafkaProducerRepository
import repositories.{GoogleBucketRepository, GoogleFireStoreRepository}
import utils.ImageHelper

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, IOException}
import java.nio.file.{Path, Paths}
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

  def getAllItemMetadata: Future[FileItemsDto] =
    for
      items <- googleFireStoreRepository.findAll
      itemsDto = items.map(item => FileItemDto.from(item))
    yield FileItemsDto(itemsDto)

  def search(name: String): Future[FileItemsDto] =
    for
      items <- googleFireStoreRepository.search(name)
      itemsDto = items.map(item => FileItemDto.from(item))
    yield FileItemsDto(itemsDto)

  def addFileItem(itemName: String, file: FilePart[TemporaryFile]): Unit =
    // only get the last part of the filename
    // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
    val fileName: String = Paths.get(file.filename).getFileName.toString
    val contentType: String = file.contentType.getOrElse("text/plain")
    val filePath = file.ref.path

    val bucketItemId = MessageDigest
      .getInstance("SHA-256")
      .digest(System.nanoTime().toString.getBytes ++ fileName.getBytes("UTF-8"))
      .map("%02X".format(_))
      .mkString

    // save image
    googleBucketRepository.upload(filePath, bucketItemId)

    // save meta data
    val newItem = new FileItem("-", itemName, fileName, contentType, bucketItemId)
    googleFireStoreRepository.save(newItem)

    // give image recognition app a job
    val message = ImageRecognitionMessage(bucketItemId, ImageHelper.readImageFromPath(filePath, contentType))
    // kafkaProducerRepository.sendToImageRecognitionApp(message) // TODO

  def getFileItem(documentId: String): Future[Option[FileItem]] =
    googleFireStoreRepository.findById(documentId)

  def deleteFileItem(documentId: String): Future[Option[String]] =
    for
      maybeFileItem <- googleFireStoreRepository.findById(documentId)
      result <- maybeFileItem match
        case None => Future.failed(new IllegalArgumentException(s"No fileItem with $documentId found"))
        case Some(_) =>
          googleBucketRepository.delete(documentId)
          googleFireStoreRepository
            .delete(documentId)
            .map(_ => Some(documentId))
    yield result

  def deleteAll(): Unit =
    googleBucketRepository.deleteAll()
    googleFireStoreRepository.deleteAll()
