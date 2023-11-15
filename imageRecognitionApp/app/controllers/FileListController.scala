package controllers
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc.*
import service.FileListService

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
class FileListController @Inject() (
    val controllerComponents: ControllerComponents,
    val fileListService: FileListService
) extends BaseController:


  def analyseImage(itemName: String): Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) {
    request =>
      request.body
        .file("file")
        .map { file =>
          fileListService.addFileItem(itemName, file)
          Ok("Done")
        }
        .getOrElse {
          BadRequest("File upload failed")
        }
  }
