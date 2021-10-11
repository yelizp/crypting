package com.example.model

import com.example.common.DateTimeHelper
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.sql.Timestamp
import java.util.Locale

object Asset {
  def apply(asset_id:String,
            name:String,
            price: BigDecimal,
            volume_24h:BigDecimal,
            change_1h:BigDecimal,
            change_24h:BigDecimal,
            change_7d:BigDecimal,
            status:String,
            created_at:String,
            updated_at:String): Asset =
    Asset(new ObjectId,
      asset_id,
      name,
      price,
      volume_24h,
      change_1h,
      change_24h,
      change_7d,
      status,
      DateTimeHelper.asTimestamp(created_at),
      DateTimeHelper.asTimestamp(updated_at))

  implicit val jsonReads: Reads[Asset] = (
    (JsPath \ "asset_id").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "price").read[BigDecimal] and
      (JsPath \ "volume_24h").read[BigDecimal] and
      (JsPath \ "change_1h").read[BigDecimal] and
      (JsPath \ "change_24h").read[BigDecimal] and
      (JsPath \ "change_7d").read[BigDecimal] and
      (JsPath \ "status").read[String] and
      (JsPath \ "created_at").read[String] and
      (JsPath \ "updated_at").read[String]
    )(Asset.apply(
    _:String,
    _:String,
    _:BigDecimal,
    _:BigDecimal,
    _:BigDecimal,
    _:BigDecimal,
    _:BigDecimal,
    _:String,
    _:String,
    _:String))
}

case class Asset(_id:ObjectId,
                 asset_id:String,
                 name:String,
                 price:BigDecimal,
                 volume_24h:BigDecimal,
                 change_1h:BigDecimal,
                 change_24h:BigDecimal,
                 change_7d:BigDecimal,
                 status:String,
                 created_at:Timestamp,
                 updated_at:Timestamp
                ) extends Entity {
  def toPoint() : Point = {
    Point.measurement(classOf[Asset].getSimpleName.toLowerCase(Locale.ENGLISH))
      .addTag("asset_id",asset_id)
      .addTag("name",name)
      .addField("price",price.bigDecimal)
      .addField("volume_24h", volume_24h.bigDecimal)
      .addField("change_1h",change_1h.bigDecimal)
      .addField("change_24h",change_24h.bigDecimal)
      .time(updated_at.getTime,WritePrecision.MS)
  }
}
