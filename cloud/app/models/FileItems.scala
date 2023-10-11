package models

import play.api.libs.json.{Format, Json}

case class FileItems (items: List[FileItem])

object FileItems {
  implicit val format: Format[FileItems] = Json.format[FileItems]
}

