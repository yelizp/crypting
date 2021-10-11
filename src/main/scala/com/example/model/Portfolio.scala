package com.example.model

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import java.util.Locale

case class Portfolio(customer_id:String,
                     exchange_id:String,
                     asset_id:String,
                     var most_revaluated:Boolean,
                     var most_devaluated:Boolean,
                     lot:BigDecimal,
                     value:BigDecimal,
                     price:BigDecimal,
                     percentChange24h:BigDecimal) {
  def toPoint() : Point = {
    Point.measurement(classOf[Portfolio].getSimpleName.toLowerCase(Locale.ENGLISH))
      .addTag("customer_id",customer_id)
      .addTag("exchange_id",exchange_id)
      .addTag("asset_id",asset_id)
      .addTag("most_revaluated", most_revaluated.toString)
      .addTag("most_devaluated", most_devaluated.toString)
      .addField("lot",lot.bigDecimal)
      .addField("value", value.bigDecimal)
      .addField("price", price.bigDecimal)
      .addField("percentChange24h", percentChange24h.bigDecimal)
      .time(System.currentTimeMillis(),WritePrecision.MS)
  }
}