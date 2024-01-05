package controllers
import play.api.libs.Files
import play.api.mvc.*
import repositories.GoogleBucketRepository
import service.ItemService
import io.circe.generic.auto.*
import io.circe.syntax.*
import play.api.libs.circe.Circe

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
class ItemController @Inject() (
    val controllerComponents: ControllerComponents,
    val itemService: ItemService,
    val googleBucketRepository: GoogleBucketRepository
) extends BaseController
    with Circe:

  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def getAllItemMetadata(projectId: String): Action[AnyContent] = Action.async { implicit request =>
    itemService.getAllItemMetadata(projectId).map { itemDto =>
      Ok(itemDto.asJson)
    }
  }

  def download(projectId: String, itemId: String): Action[AnyContent] = Action.async { request =>
    itemService.getItem(projectId, itemId).map {
      case Some(item) =>
        Ok(googleBucketRepository.download(projectId, item.bucketId))
          .as(item.contentType)
          .withHeaders("Content-Disposition" -> s"attachment; filename=${item.itemId}_${item.captureTime}")
      case None =>
        NotFound("File not found")
    }
  }

  def delete(projectId: String, itemId: String): Action[AnyContent] = Action.async { request =>
    itemService.deleteItem(projectId, itemId).map {
      case Some(value) => Ok(s"Item $value deleted")
      case None        => NotFound(s"Could not find $itemId")
    }
  }

  def deleteProject(projectId: String): Action[AnyContent] = Action.async { request =>
    itemService.deleteProject(projectId)

    Future.successful(Ok("Deleted all files"))
  }

  def upload(projectId: String): Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) {
    request =>
      request.body
        .file("file")
        .map { file =>
          itemService.addItem(projectId, file)
          // TODO REMOVE
          Redirect(routes.ItemController.index())
        }
        .getOrElse {
          BadRequest("File upload failed")
        }
  }
