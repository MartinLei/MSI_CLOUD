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
    fileListService.getAll().map { people =>
      Ok(Json.toJson(people))
    }
  }
