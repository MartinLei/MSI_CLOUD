package models

import play.api.libs.json.{Format, Json}

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


