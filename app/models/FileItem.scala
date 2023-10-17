package models

import play.api.libs.json.{Format, Json}
import slick.jdbc.PostgresProfile.api.*

import java.sql.Timestamp

case class FileItemDto(id: Int, itemName: String, fileName: String, contentType: String, date: String)

object FileItemDto:
  implicit val format: Format[FileItemDto] = Json.format[FileItemDto]

  def from(fileItem: FileItem): FileItemDto = FileItemDto(
    id = fileItem.id,
    itemName = fileItem.itemName,
    fileName = fileItem.fileName,
    contentType = fileItem.contentType,
    date = fileItem.date
  )

case class FileItem(id: Int, itemName: String, fileName: String, contentType: String, data: Array[Byte], date: String)

object FileItem:
  implicit val format: Format[FileItem] = Json.format[FileItem]

class FileItemTable(tag: Tag) extends Table[FileItem](tag, "file_item"):
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  override def * = (id, itemName, fileName, contentType, data, date) <> ((FileItem.apply _).tupled, FileItem.unapply)

  def itemName = column[String]("item_name")

  def fileName = column[String]("file_name")

  def contentType = column[String]("content_type")

  def data = column[Array[Byte]]("data")

  def date = column[String]("date")
