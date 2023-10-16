package controllers

import play.api.libs.Files
import play.api.mvc.*
import service.FileListService
import scala.util.{Success, Failure}

import javax.inject.*
import scala.concurrent.ExecutionContext.Implicits.global


class FileDeleteController @Inject()(cc: ControllerComponents, fileListService: FileListService)
  extends AbstractController(cc):

  def delete(id: Int) = Action.async { request =>
    fileListService.removeFileItem(id).map { rowsAffected =>
      if (rowsAffected > 0) {
        Ok("Element erfolgreich gelöscht")
      } else {
        NotFound("Element nicht gefunden")
      }
    }.recover {
      case ex: Throwable =>
        InternalServerError(s"Fehler bei der Datenbankoperation: ${ex.getMessage}")
    }
  }
