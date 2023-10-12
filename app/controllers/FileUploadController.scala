package controllers

import play.api.libs.Files
import play.api.mvc.*
import service.FileListService

import javax.inject.*

class FileUploadController @Inject()(cc: ControllerComponents, fileListService: FileListService)
  extends AbstractController(cc):

  def upload: Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { request =>
    request.body
      .file("picture")
      .map { file =>
        val itemId: String = "TODO"
        fileListService.addFileItem(itemId, file)
        Ok("File uploaded")
      }
      .getOrElse {
        BadRequest("File upload failed")
      }
  }

