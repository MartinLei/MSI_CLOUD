package controllers
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc.*
import repositories.GoogleBucketRepository
import service.FileListService

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
class FileListController @Inject() (
    val controllerComponents: ControllerComponents,
    val fileListService: FileListService,
    val googleBucketRepository: GoogleBucketRepository
) extends BaseController:

  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def getAllItemMetadata(projectId : String): Action[AnyContent] = Action.async { implicit request =>
    fileListService.getAllItemMetadata(projectId).map { item =>
      Ok(Json.toJson(item))
    }
  }

  def search(projectId: String, fileName : String): Action[AnyContent] = Action.async { implicit request =>
    fileListService.search(projectId, fileName).map { item =>
      Ok(Json.toJson(item))
    }
  }

  def download(projectId: String, documentId: String): Action[AnyContent] = Action.async { request =>
    fileListService.getFileItem(projectId, documentId).map {
      case Some(fileId) =>
        Ok(googleBucketRepository.download(projectId, fileId.bucketItemId))
          .as(fileId.contentType)
          .withHeaders("Content-Disposition" -> s"attachment; filename=${fileId.fileName}")
      case None =>
        NotFound("File not found")
    }
  }

  def delete(projectId: String, documentId: String): Action[AnyContent] = Action.async { request =>
    fileListService.deleteFileItem(projectId, documentId).map {
      case Some(value) => Ok(s"Item $value deleted")
      case None        => NotFound(s"Could not find $documentId")
    }
  }

  def deleteAll(): Action[AnyContent] = Action.async { request =>
    fileListService.deleteAll()

    Future.successful(Ok("Deleted all files"))
  }

  def upload(projectId: String): Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) {
    request =>
      request.body
        .file("file")
        .map { file =>
          fileListService.addFileItem(projectId, file)
          // TODO REMOVE
          Redirect(routes.FileListController.index())
        }
        .getOrElse {
          BadRequest("File upload failed")
        }
  }
