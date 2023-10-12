package controllers

import play.api.libs.Files
import play.api.mvc.{AbstractController, Action, ControllerComponents, MultipartFormData}
import service.FileListService

import java.nio.file.Paths
import javax.inject.*

class FileUploadController @Inject()(cc: ControllerComponents, fileListService: FileListService)
  extends AbstractController(cc):

  def upload: Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { request =>
    request.body
      .file("picture")
      .map { file =>
        // only get the last part of the filename
        // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
        val filename = Paths.get(file.filename).getFileName.toString
        fileListService.uploadFile(filename, file)
        Ok("File uploaded")
      }
      .getOrElse {
        BadRequest("File upload failed")
      }
  }

