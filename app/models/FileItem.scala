package models

import play.api.libs.json.{Format, Json}
import slick.jdbc.PostgresProfile.api.*

import scala.beans.BeanProperty

case class FileItemDto(id: Int, itemName: String, fileName: String, contentType: String)

object FileItemDto:
  implicit val format: Format[FileItemDto] = Json.format[FileItemDto]

  def from(fileItem: FileItem): FileItemDto = FileItemDto(
    id = fileItem.id,
    itemName = fileItem.itemName,
    fileName = fileItem.fileName,
    contentType = fileItem.contentType
  )

/**
 * Using @BeanProperty for creating java getter and setter.
 */
case class FileItem(
    @BeanProperty var id: Int,
    @BeanProperty var itemName: String,
    @BeanProperty var fileName: String,
    @BeanProperty var contentType: String,
    @BeanProperty var bucketItemId: String
):
  def this() = this(0, "", "", "", "")

object FileItem:
  implicit val format: Format[FileItem] = Json.format[FileItem]

class FileItemTable(tag: Tag) extends Table[FileItem](tag, "file_item"):
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def itemName = column[String]("item_name")

  def fileName = column[String]("file_name")

  def contentType = column[String]("content_type")

  def bucketItemId = column[String]("bucket_item_id")

  override def * = (id, itemName, fileName, contentType, bucketItemId) <> ((FileItem.apply _).tupled, FileItem.unapply)
