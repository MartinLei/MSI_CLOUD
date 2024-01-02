package models

import play.api.libs.json.{Format, Json}

case class ItemsDto(items: Seq[ItemDto])
