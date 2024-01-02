package models

import repositories.kafka.model.DetectedObject
import io.circe
import io.circe.*
import io.circe.syntax.*
import io.circe.generic.auto.*
import io.circe.parser.*
import scala.beans.BeanProperty

case class ItemDto(id: String, fileName: String, contentType: String)

object ItemDto:
  def from(item: Item): ItemDto = ItemDto(
    id = item.id,
    fileName = item.name,
    contentType = item.contentType
  )

/** Using @BeanProperty for creating java getter and setter.
  */
case class Item(
                 @BeanProperty var id: String,
                 @BeanProperty var name: String,
                 @BeanProperty var contentType: String,
                 @BeanProperty var bucketId: String,
                 @BeanProperty var detectedObjectSerialized: String

):
  def this() = this("", "", "", "", "")

  def detectedObject : Option[DetectedObject] = decode[DetectedObject](this.detectedObjectSerialized) match
    case Left(df: DecodingFailure) => None
    case Left(pf: ParsingFailure) => None
    case Right(value) => Some(value)

object Item:
  def apply(id: String, item: Item): Item =
    Item(id, item.name, item.contentType, item.bucketId, item.detectedObjectSerialized)

