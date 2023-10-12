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

  def findAll: Future[Seq[FileItem]] =
    dbConfig.db.run(fileItemTable.result)

  def save(fileItem: FileItem): Future[Int] =
    val insertQuery = fileItemTable += fileItem
    dbConfig.db.run(insertQuery)

  def findById(id: Int): Future[Option[FileItem]] =
    db.run(fileItemTable.filter(_.id === id).result.headOption)
