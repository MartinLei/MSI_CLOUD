package service

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
):

  private val logger = Logger(getClass)

  def getAllItemMetadata: Future[FileItemsDto] =
    googleFireStoreRepository.findAll
      .map(items => items.map(item => FileItemDto.from(item)))
      .map(itemsDto => FileItemsDto(itemsDto))

  def search(name: String): Future[FileItemsDto] =
    fileListRepository.findAll
      .map(items =>
        items
          .filter(item => item.itemName.contains(name) || item.fileName.contains(name))
          .map(item => FileItemDto.from(item))
      )
      .map(itemsDto => FileItemsDto(itemsDto))

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

    val newItem = new FileItem(0, itemName, fileName, contentType, bucketItemId)
    googleFireStoreRepository.save(newItem)

  def getFileItem(id: Int): Future[Option[FileItem]] =
    fileListRepository.findById(id)

  def removeFileItem(id: Int): Future[Int] =
    Await.result(fileListRepository.findById(id), Duration.Inf) match
      case Some(fileItem: FileItem) =>
        googleBucketRepository.delete(fileItem.bucketItemId)
        fileListRepository.removeById(id)
      case None =>
        Future.failed(new NoSuchElementException())
