package models

import play.api.libs.json.{Format, Json}
import slick.jdbc.PostgresProfile.api.*

case class FileItem(id: Long, name: String, data: Array[Byte])

object FileItem:
  implicit val format: Format[FileItem] = Json.format[FileItem]

class FileItemTable(tag: Tag) extends Table[FileItem](tag, "fileitem"):

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")

  def data = column[Array[Byte]]("data")

  override def * =
    (id, name, data) <> ((FileItem.apply _).tupled, FileItem.unapply)
