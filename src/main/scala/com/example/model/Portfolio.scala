package com.example.model

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import java.util.Locale

case class Portfolio(customer_id:String,
                     exchange_id:String,
                     asset_id:String,
                     lot:BigDecimal,
                     value:BigDecimal,
                     percentChange24h:BigDecimal) {
  def toPoint() : Point = {
    Point.measurement(classOf[Portfolio].getSimpleName.toLowerCase(Locale.ENGLISH))
      .addTag("customer_id",customer_id)
      .addTag("exchange_id",exchange_id)
      .addTag("asset_id",asset_id)
      .addField("lot",lot.bigDecimal)
      .addField("value", value.bigDecimal)
      .addField("percentChange24h", percentChange24h.bigDecimal)
      .time(System.currentTimeMillis(),WritePrecision.MS)
  }
}