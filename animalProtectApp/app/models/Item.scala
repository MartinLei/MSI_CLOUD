package models

import play.api.libs.json.{Format, Json}

import scala.beans.BeanProperty

case class ItemDto(id: String, fileName: String, contentType: String)

object ItemDto:
  implicit val format: Format[ItemDto] = Json.format[ItemDto]

  def from(fileItem: Item): ItemDto = ItemDto(
    id = fileItem.id,
    fileName = fileItem.fileName,
    contentType = fileItem.contentType
  )

/** Using @BeanProperty for creating java getter and setter.
  */
case class Item(
    @BeanProperty var id: String,
    @BeanProperty var fileName: String,
    @BeanProperty var contentType: String,
    @BeanProperty var bucketItemId: String
):
  def this() = this("", "", "", "")

object Item:
  implicit val format: Format[Item] = Json.format[Item]

  def apply(id: String, fileItem: Item): Item =
    Item(id, fileItem.fileName, fileItem.contentType, fileItem.bucketItemId)
