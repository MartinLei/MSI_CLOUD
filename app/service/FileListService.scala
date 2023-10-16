package service

import models.{FileItem, FileItemDto, FileItemsDto}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.*
import repositories.FileListRepository

import java.nio.file.{Files, Paths}
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileListService @Inject() (val fileListRepository: FileListRepository):

  def getAllItemMetadata(): Future[FileItemsDto] =
    fileListRepository.findAll
      .map(items => items.map(item => FileItemDto.from(item)))
      .map(itemsDto => FileItemsDto(itemsDto))

  def addFileItem(itemName: String, file: FilePart[TemporaryFile]): Future[Int] =
    // only get the last part of the filename
    // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
    val fileName: String = Paths.get(file.filename).getFileName.toString
    val contentType: String = file.contentType.getOrElse("text/plain")
    val data: Array[Byte] = Files.readAllBytes(file.ref.path)
//    val date: Timestamp = Timestamp.valueOf(LocalDateTime.now())
    val date = "dateTest"

    val newItem = new FileItem(0, itemName, fileName, contentType, data, date)
    fileListRepository.save(newItem)

  def getFileItem(id: Int): Future[Option[FileItem]] =
    fileListRepository.findById(id)

  def removeFileItem(id: Int): Future[Int] =
    fileListRepository.removeById(id)