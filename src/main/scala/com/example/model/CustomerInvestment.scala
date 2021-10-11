package com.example.model

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import org.mongodb.scala.bson.ObjectId
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}
import java.util.Locale

object CustomerInvestment {
  def apply(customer_id: String,
            exchange_id:String,
            base_asset:String,
            lot:BigDecimal
           ) : CustomerInvestment =
    CustomerInvestment(new ObjectId(),
      customer_id,
      exchange_id,
      base_asset,
      lot)

  implicit val jsonReads: Reads[CustomerInvestment] = (
    (JsPath \ "customer_id").read[String] and
    (JsPath \ "exchange_id").read[String] and
      (JsPath \ "base_asset").read[String] and
      (JsPath \ "lot").read[BigDecimal]
    )(CustomerInvestment.apply(
    _:String,
    _:String,
    _:String,
    _:BigDecimal
  ))
}

case class CustomerInvestment(_id:ObjectId,
                              customer_id:String,
                              exchange_id:String,
                              base_asset:String,
                              lot:BigDecimal) extends Entity {
  def toPoint() : Point = {
    Point.measurement(classOf[CustomerInvestment].getSimpleName.toLowerCase(Locale.ENGLISH))
      .addTag("customer_id",customer_id)
      .addTag("exchange_id",exchange_id)
      .addTag("asset_id",base_asset)
      .addField("lot",lot.bigDecimal)
      .time(System.currentTimeMillis(),WritePrecision.MS)
  }
}