package controllers
import play.api.libs.json.Json
import play.api.mvc.*
import repositories.GoogleBucketRepository
import service.FileListService

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
class FileListController @Inject() (
    val controllerComponents: ControllerComponents,
    val fileListService: FileListService,
    val googleBucketRepository: GoogleBucketRepository
) extends BaseController:

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def getAllItemMetadata(): Action[AnyContent] = Action.async { implicit request =>
    fileListService.getAllItemMetadata.map { item =>
      Ok(Json.toJson(item))
    }
  }

  def search(name: String): Action[AnyContent] = Action.async { implicit request =>
    fileListService.search(name).map { item =>
      Ok(Json.toJson(item))
    }
  }

  def downloadFile(id: String): Action[AnyContent] = Action.async { request =>
    fileListService.getFileItem(id).map {
      case Some(fileItem) =>
        Ok(googleBucketRepository.download(fileItem.bucketItemId))
          .as(fileItem.contentType)
          .withHeaders("Content-Disposition" -> s"attachment; filename=${fileItem.fileName}")
      case None =>
        NotFound("File not found")
    }
  }

  def delete(id: String): Action[AnyContent] = Action.async { request =>
    fileListService.deleteFileItem(id).map {
      case Some(value) => Ok(s"Item $value deleted")
      case None        => NotFound(s"Could not find $id")
    }
  }
