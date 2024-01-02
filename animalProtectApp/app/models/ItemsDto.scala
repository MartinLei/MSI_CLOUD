package models

import play.api.libs.json.{Format, Json}

case class ItemsDto(items: Seq[ItemDto])

object ItemsDto:
  implicit val format: Format[ItemsDto] = Json.format[ItemsDto]
