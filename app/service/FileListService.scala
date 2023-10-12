package service

import models.{FileItem, FileItems}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.*
import repositories.FileListRepository

import java.nio.file.{Files, Paths}
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileListService @Inject() (val fileListRepository: FileListRepository):

  def getAll(): Future[FileItems] =
    fileListRepository.findAllDB
      .map(a => FileItems(a))

  def addFileItem(itemName: String, file: FilePart[TemporaryFile]): Future[Int] =
    // only get the last part of the filename
    // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
    val fileName: String = Paths.get(file.filename).getFileName.toString
    val contentType: String = file.contentType.getOrElse("text/plain")
    val data: Array[Byte] = Files.readAllBytes(file.ref.path)

    val newItem = new FileItem(0, itemName, fileName, contentType, data)
    fileListRepository.save(newItem)

  def getFileItem(id: Int): Future[Option[FileItem]] = {
    fileListRepository.findById(id)
  }