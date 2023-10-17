package controllers

import play.api.libs.Files
import play.api.mvc.*
import service.FileListService

import java.nio.file.Paths
import javax.inject.*

class FileUploadController @Inject() (cc: ControllerComponents, fileListService: FileListService)
    extends AbstractController(cc):

  def upload(): Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) {
    request =>
      request.body
        .file("file")
        .map { file =>
          fileListService.addFileItem(Paths.get(file.filename).getFileName.toString, file)
          Redirect(routes.FileListController.index())
        }
        .getOrElse {
          BadRequest("File upload failed")
        }
  }
