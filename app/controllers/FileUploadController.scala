package controllers

import play.api.libs.Files
import play.api.mvc.*
import service.FileListService

import javax.inject.*

class FileUploadController @Inject()(cc: ControllerComponents, fileListService: FileListService)
  extends AbstractController(cc):

  def upload(itemName: String): Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { request =>
    request.body
      .file("file")
      .map { file =>
        fileListService.addFileItem(itemName, file)
        Ok("File uploaded")
      }
      .getOrElse {
        BadRequest("File upload failed")
      }
  }
