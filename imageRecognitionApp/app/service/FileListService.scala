package service

import com.typesafe.scalalogging.LazyLogging
import models.{FileItem, FileItemDto, FileItemsDto}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.*

import java.nio.file.Paths
import java.security.MessageDigest
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileListService @Inject()() extends LazyLogging:

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


    val newItem = new FileItem("-", itemName, fileName, contentType, bucketItemId)
    Future.successful(1)
