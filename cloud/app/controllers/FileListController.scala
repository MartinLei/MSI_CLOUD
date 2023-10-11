package controllers
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}
import service.FileListService

import javax.inject.Inject

class FileListController @Inject()(val fileListService: FileListService,
                                   val controllerComponents: ControllerComponents) extends BaseController {

  def getAll() = Action {
    Ok(Json.toJson(fileListService.getAll()))
  }
}
