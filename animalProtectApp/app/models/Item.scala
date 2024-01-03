package models

import com.github.nscala_time.time.Imports.DateTime
import repositories.kafka.model.DetectedObject
import io.circe
import io.circe.*
import io.circe.syntax.*
import io.circe.generic.auto.*
import io.circe.parser.*

import java.time.LocalDateTime
import java.util.Date
import scala.beans.BeanProperty

case class ItemDto(
    id: String,
    name: String,
    contentType: String,
    captureTime: String,
    detectedObjects: List[DetectedObject]
)

object ItemDto:
  def from(item: Item): ItemDto =
    ItemDto(
      id = item.id,
      name = item.name,
      contentType = item.contentType,
      captureTime = item.captureTime,
      detectedObjects = item.detectedObjects
    )

/** Using @BeanProperty for creating java getter and setter.
  */
case class Item(
    @BeanProperty var id: String,
    @BeanProperty var name: String,
    @BeanProperty var contentType: String,
    @BeanProperty var captureTime: String,
    @BeanProperty var bucketId: String,
    @BeanProperty var detectedObjectsSerialized: String
):
  def this() = this("", "", "", DateTime.now().toString, "", "")

  def detectedObjects: List[DetectedObject] = decode[List[DetectedObject]](this.detectedObjectsSerialized) match
    case Left(df: DecodingFailure) => List.empty
    case Left(pf: ParsingFailure)  => List.empty
    case Right(value)              => value

object Item:

  val paramName_detectedObjectsSerialized = "detectedObjectsSerialized"
  val paramName_bucketId = "bucketId"
  def apply(id: String, item: Item): Item =
    Item(id, item.name, item.contentType, item.captureTime, item.bucketId, item.detectedObjectsSerialized)
