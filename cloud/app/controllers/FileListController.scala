package controllers
import play.api.mvc.{BaseController, ControllerComponents, PlayBodyParsers}
import play.api.libs.json.Json

import javax.inject.Inject

class FileListController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def getAll() = Action {
    Ok(Json.toJson("Test"))
  }
}
