package controllers
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}
import service.FileListService

import javax.inject.Inject
import concurrent.ExecutionContext.Implicits.global
class FileListController @Inject() (
    val fileListService: FileListService,
    val controllerComponents: ControllerComponents
) extends BaseController:

  def getAll = Action.async { implicit request =>
    fileListService.getAll().map { people =>
      Ok(Json.toJson(people))
    }
  }
