package models

import play.api.libs.json.{Format, Json}
import slick.jdbc.PostgresProfile.api.*

import scala.beans.BeanProperty

case class FileItemDto(id: String, itemName: String, fileName: String, contentType: String)

object FileItemDto:
  implicit val format: Format[FileItemDto] = Json.format[FileItemDto]

  def from(fileItem: FileItem): FileItemDto = FileItemDto(
    id = fileItem.id,
    itemName = fileItem.itemName,
    fileName = fileItem.fileName,
    contentType = fileItem.contentType
  )

/** Using @BeanProperty for creating java getter and setter.
  */
case class FileItem(
    @BeanProperty var id: String,
    @BeanProperty var itemName: String,
    @BeanProperty var fileName: String,
    @BeanProperty var contentType: String,
    @BeanProperty var bucketItemId: String
):
  def this() = this("", "", "", "", "")

object FileItem:
  implicit val format: Format[FileItem] = Json.format[FileItem]

  def apply(id: String, fileItem: FileItem): FileItem =
    FileItem(id, fileItem.itemName, fileItem.fileName, fileItem.contentType, fileItem.bucketItemId)

class FileItemTable(tag: Tag) extends Table[FileItem](tag, "file_item"):
  def id = column[String]("id", O.PrimaryKey, O.AutoInc)

  def itemName = column[String]("item_name")

  def fileName = column[String]("file_name")

  def contentType = column[String]("content_type")

  def bucketItemId = column[String]("bucket_item_id")

  override def * = ???
