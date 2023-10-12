package models

import play.api.libs.json.{Format, Json}
import slick.jdbc.PostgresProfile.api.*

case class FileItem(id: Int, itemName: String, fileName: String, contentType: String, data: Array[Byte])

object FileItem:
  implicit val format: Format[FileItem] = Json.format[FileItem]

class FileItemTable(tag: Tag) extends Table[FileItem](tag, "file_item"):
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  override def * = (id, itemName, fileName, contentType, data) <> ((FileItem.apply _).tupled, FileItem.unapply)

  def itemName = column[String]("item_name")

  def fileName = column[String]("file_name")

  def contentType = column[String]("content_type")

  def data = column[Array[Byte]]("data")
