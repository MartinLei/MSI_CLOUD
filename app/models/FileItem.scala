package models

import play.api.libs.json.{Format, Json}
import slick.jdbc.PostgresProfile.api.*

case class FileItem(id: Int, name: String, data: Array[Byte])

object FileItem:
  implicit val format: Format[FileItem] = Json.format[FileItem]

class FileItemTable(tag: Tag) extends Table[FileItem](tag, "fileitem"):

  override def * = (id, name, data) <> ((FileItem.apply _).tupled, FileItem.unapply)
  def name = column[String]("name")

  def data = column[Array[Byte]]("data")

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
