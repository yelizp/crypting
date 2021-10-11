package com.example.model

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import org.mongodb.scala.bson.ObjectId

import java.util.Locale

object CustomerMarket {
  def apply(customer_id:String,
            exchange_id:String,
            base_asset:String,
            targetPrice:BigDecimal) : CustomerMarket = {
    CustomerMarket(new ObjectId(), customer_id, exchange_id, base_asset,targetPrice)
  }
}

case class CustomerMarket(_id:ObjectId,
                          customer_id:String,
                          exchange_id:String,
                          base_asset:String,
                          targetPrice:BigDecimal
                          ) extends Entity {
  def toPoint() : Point = {
    Point.measurement(classOf[CustomerMarket].getSimpleName.toLowerCase(Locale.ENGLISH))
      .addTag("customer_id", customer_id)
      .addTag("exchange_id",exchange_id)
      .addTag("asset_id",base_asset)
      .addField("targetPrice",targetPrice)
      .time(System.currentTimeMillis(),WritePrecision.MS)
  }
}