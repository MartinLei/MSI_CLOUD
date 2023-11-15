package models

import play.api.libs.json.{Format, Json}

case class FileItemsDto(items: Seq[FileItemDto])

object FileItemsDto:
  implicit val format: Format[FileItemsDto] = Json.format[FileItemsDto]
