package com.example.model

import java.sql.Timestamp
import com.example.common.DateTimeHelper
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.influxdb.query.FluxRecord
import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.util.Locale

object Market {
  def apply(exchange_id:String,
            symbol: String,
            base_asset:String,
            quote_asset:String,
            price_unconverted:BigDecimal,
            price:BigDecimal,
            change_24h:BigDecimal,
            spread:BigDecimal,
            volume_24h:BigDecimal,
            status:String,
            created_at:String,
            updated_at:String
           ) : Market =
    Market(new ObjectId(),
      exchange_id,
      symbol,
      base_asset,
      quote_asset,
      price_unconverted,
      price,
      change_24h,
      spread,
      volume_24h,
      status,
      DateTimeHelper.asTimestamp(created_at),
      DateTimeHelper.asTimestamp(updated_at))

  implicit val jsonReads: Reads[Market] = (
    (JsPath \ "exchange_id").read[String] and
      (JsPath \ "symbol").read[String] and
      (JsPath \ "base_asset").read[String] and
      (JsPath \ "quote_asset").read[String] and
      (JsPath \ "price_unconverted").read[BigDecimal] and
      (JsPath \ "price").read[BigDecimal] and
      (JsPath \ "change_24h").read[BigDecimal] and
      (JsPath \ "spread").read[BigDecimal] and
      (JsPath \ "volume_24h").read[BigDecimal] and
      (JsPath \ "status").read[String] and
      (JsPath \ "created_at").read[String] and
      (JsPath \ "updated_at").read[String]
    )(Market.apply(
    _:String,
    _:String,
    _:String,
    _:String,
    _:BigDecimal,
    _:BigDecimal,
    _:BigDecimal,
    _:BigDecimal,
    _:BigDecimal,
    _:String,
    _:String,
    _:String
  ))

  def apply(record: FluxRecord) : Market = {
    val exchange_id:String = record.getValueByKey("exchange_id").asInstanceOf[String]
//    val symbol:String = record.getValueByKey("symbol").asInstanceOf[String]
    val base_asset:String = record.getValueByKey("base_asset").asInstanceOf[String]
//    val quote_asset:String = record.getValueByKey("quote_asset").asInstanceOf[String]
//    val price_unconverted:BigDecimal = record.getValueByKey("price_unconverted").asInstanceOf[BigDecimal]
    val price:BigDecimal = BigDecimal(record.getValue().asInstanceOf[Double])
//    val change_24h:BigDecimal = record.getValueByKey("change_24h").asInstanceOf[BigDecimal]
//    val spread:BigDecimal = record.getValueByKey("spread").asInstanceOf[BigDecimal]
//    val volume_24h:BigDecimal = record.getValueByKey("volume_24h").asInstanceOf[BigDecimal]
//    val status:String = record.getValueByKey("status").asInstanceOf[String]
//    val created_at:Timestamp = record.getValueByKey("created_at").asInstanceOf[Timestamp]
    val updated_at:Timestamp = Timestamp.from(record.getStart)

//    Market(new ObjectId(),
//      exchange_id,
//      symbol,
//      base_asset,
//      quote_asset,
//      price_unconverted,
//      price,
//      change_24h,
//      spread,
//      volume_24h,
//      status,
//      created_at,
//      updated_at)

    Market(
      null,
      exchange_id,
      null,
      base_asset,
      null,
      null,
      price,
      null,
      null,
      null,
      null,
      null,
      updated_at
    )
  }
}

case class Market(_id:ObjectId,
                  exchange_id:String,
                  symbol: String,
                  base_asset:String,
                  quote_asset:String,
                  price_unconverted:BigDecimal,
                  price:BigDecimal,
                  change_24h:BigDecimal,
                  spread:BigDecimal,
                  volume_24h:BigDecimal,
                  status:String,
                  created_at:Timestamp,
                  updated_at:Timestamp) extends Entity {
  def toPoint() : Point = {
    Point.measurement(classOf[Market].getSimpleName.toLowerCase(Locale.ENGLISH))
      .addTag("exchange_id",exchange_id)
      .addTag("base_asset",base_asset)
      .addField("price",price.bigDecimal)
      .addField("volume_24h", volume_24h.bigDecimal)
      .addField("change_24h",change_24h.bigDecimal)
      .time(updated_at.getTime,WritePrecision.MS)
  }
}
