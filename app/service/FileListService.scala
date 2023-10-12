package service

import models.{FileItem, FileItems}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.*
import repositories.FileListRepository

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileListService @Inject() (val fileListRepository: FileListRepository):


  def getAll(): Future[FileItems] =
    fileListRepository.findAllDB
      .map(a => FileItems(a))

  def uploadFile(filename: String, file: FilePart[TemporaryFile]): Future[Int] =
    val newItem = new FileItem(0, filename, "data")
    fileListRepository.save(newItem)
