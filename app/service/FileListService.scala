package service

import com.typesafe.scalalogging.LazyLogging
import models.{FileItem, FileItemDto, FileItemsDto}
import play.api.Logger
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.*
import repositories.{FileListRepository, GoogleBucketRepository, GoogleFireStoreRepository}

import java.nio.file.{Files, Paths}
import java.security.MessageDigest
import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.hashing.MurmurHash3

class FileListService @Inject() (
    fileListRepository: FileListRepository,
    googleBucketRepository: GoogleBucketRepository,
    googleFireStoreRepository: GoogleFireStoreRepository
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

  def addFileItem(itemName: String, file: FilePart[TemporaryFile]): Future[Int] =
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

    googleBucketRepository.upload(filePath, bucketItemId)

    val newItem = new FileItem("-", itemName, fileName, contentType, bucketItemId)
    googleFireStoreRepository.save(newItem)

  def getFileItem(documentId: String): Future[Option[FileItem]] =
    googleFireStoreRepository.findById(documentId)

  def deleteFileItem(documentId: String): Future[Option[String]] =
    for
      maybeFileItem <- googleFireStoreRepository.findById(documentId)
      result <- maybeFileItem match
        case None => Future.failed(new IllegalArgumentException(s"No fileItem with $documentId found"))
        case Some(fileItem) =>
          googleBucketRepository.delete(fileItem.bucketItemId)
          googleFireStoreRepository
            .delete(documentId)
            .map(_ => Some(fileItem.id))
    yield result