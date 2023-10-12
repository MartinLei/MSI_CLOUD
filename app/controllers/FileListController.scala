package controllers
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import service.FileListService

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
class FileListController @Inject() (
    val fileListService: FileListService,
    val controllerComponents: ControllerComponents
) extends BaseController:

  def getAll: Action[AnyContent] = Action.async { implicit request =>
    fileListService.getAll().map { item =>
      Ok(Json.toJson(item))
    }
  }

  def downloadFile(id: Int): Action[AnyContent] = Action.async { request =>
    fileListService.getFileItem(id).map {
      case Some(fileContent) =>
        Ok(fileContent.data)
          .as("image/jpeg")
          .withHeaders("Content-Disposition" -> "attachment; filename=TODO.csv")
      case None =>
        NotFound("File not found")
    }
  }
