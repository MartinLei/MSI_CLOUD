package models

import play.api.libs.json.{Format, Json}

case class FileItem (name: String, data: String)

object FileItem {
  implicit val format: Format[FileItem] = Json.format[FileItem]
}
