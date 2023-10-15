package controllers
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import service.FileListService

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
class FileListController @Inject() (
    val controllerComponents: ControllerComponents,
    val fileListService: FileListService
) extends BaseController:

  def getAllItemMetadata(): Action[AnyContent] = Action.async { implicit request =>
    fileListService.getAllItemMetadata().map { item =>
      Ok(Json.toJson(item))
    }
  }

  def downloadFile(id: Int): Action[AnyContent] = Action.async { request =>
    fileListService.getFileItem(id).map {
      case Some(fileItem) =>
        Ok(fileItem.data)
          .as(fileItem.contentType)
          .withHeaders("Content-Disposition" -> s"attachment; filename=${fileItem.fileName}")
      case None =>
        NotFound("File not found")
    }
  }
