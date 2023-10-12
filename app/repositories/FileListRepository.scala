package repositories

import models.{FileItem, FileItemTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api.*
import slick.lifted.TableQuery

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileListRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit
    executionContext: ExecutionContext
) extends HasDatabaseConfigProvider[PostgresProfile]:

  private val fileItemTable = TableQuery[FileItemTable]

  /** TODO use real db repository.
    */
  def findAll(): List[FileItem] = List(
    FileItem(1L, "Test1", "Datei"),
    FileItem(2L, "Test2", "Datei")
  )

  def findAllDB: Future[Seq[FileItem]] =
    dbConfig.db.run(fileItemTable.result)
