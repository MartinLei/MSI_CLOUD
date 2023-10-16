package controllers
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request}
import service.FileListService

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
class FileListController @Inject() (
    val fileListService: FileListService,
    val controllerComponents: ControllerComponents
) extends BaseController:

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

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
