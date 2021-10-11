package com.example.model

import com.example.common.DateTimeHelper
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import org.mongodb.scala.bson.ObjectId

import java.sql.Timestamp
import java.util.Locale

trait Alert { def toPoint() : Point }

case class TenPercentLossAlert(_id:ObjectId, customer_id:String, exchange_id:String, asset_id:String, percentLoss:BigDecimal, duration:String) extends Alert {
  def toPoint() : Point = {
    Point.measurement(classOf[TenPercentLossAlert].getSimpleName.toLowerCase(Locale.ENGLISH))
      .addTag("customer_id",customer_id)
      .addTag("exchange_id",exchange_id)
      .addTag("base_asset",asset_id)
      .addField("percentLoss",percentLoss.bigDecimal)
      .addField("since", duration)
      .time(System.currentTimeMillis(),WritePrecision.MS)
  }
}
object TenPercentLossAlert {
  def apply(customer_id:String, exchange_id:String, asset_id:String, percentLoss:BigDecimal, duration:String): TenPercentLossAlert = {
    TenPercentLossAlert(new ObjectId(), customer_id,exchange_id,asset_id,percentLoss,duration)
  }
}


case class TargetPriceExceededAlert(_id:ObjectId, customer_id:String, exchange_id: String, asset_id:String, target_price:BigDecimal, current_price: BigDecimal) extends Alert {
  def toPoint() : Point = {
    Point.measurement(classOf[TargetPriceExceededAlert].getSimpleName.toLowerCase(Locale.ENGLISH))
      .addTag("customer_id",customer_id)
      .addTag("exchange_id",exchange_id)
      .addTag("base_asset",asset_id)
      .addField("target_price",target_price.bigDecimal)
      .addField("current_price", current_price.bigDecimal)
      .time(System.currentTimeMillis(),WritePrecision.MS)
  }
}
object TargetPriceExceededAlert {
  def apply(customer_id:String, exchange_id: String, asset_id:String, target_price:BigDecimal, current_price: BigDecimal): TargetPriceExceededAlert = {
    TargetPriceExceededAlert(new ObjectId(), customer_id,exchange_id, asset_id,target_price, current_price)
  }
}


case class DevaluationAlert(_id:ObjectId, customer_id:String, exchange_id: String, asset_id: String, devalutationRate:BigDecimal, event_ts:Timestamp) extends Alert {
  def toPoint() : Point = {
    Point.measurement(classOf[DevaluationAlert].getSimpleName.toLowerCase(Locale.ENGLISH))
      .addTag("customer_id",customer_id)
      .addTag("exchange_id",exchange_id)
      .addTag("base_asset",asset_id)
      .addField("devaluation_rate",devalutationRate.bigDecimal)
      .addField("event_ts", DateTimeHelper.asString(event_ts))
      .time(System.currentTimeMillis(),WritePrecision.MS)
  }
}
object DevaluationAlert {
  def apply(customer_id:String, exchange_id: String, asset_id: String, devalutationRate:BigDecimal, event_ts:Timestamp): DevaluationAlert = {
    DevaluationAlert(new ObjectId(), customer_id,exchange_id, asset_id, devalutationRate, event_ts)
  }
}


case class RateOfChangeAlert(_id:ObjectId, customer_id:String, exchange_id: String, asset_id: String, rateOfChange:BigDecimal, event_ts:Timestamp) extends Alert {
  def toPoint() : Point = {
    Point.measurement(classOf[RateOfChangeAlert].getSimpleName.toLowerCase(Locale.ENGLISH))
      .addTag("customer_id",customer_id)
      .addTag("exchange_id",exchange_id)
      .addTag("base_asset",asset_id)
      .addField("rate_of_change",rateOfChange.bigDecimal)
      .addField("event_ts", DateTimeHelper.asString(event_ts))
      .time(System.currentTimeMillis(),WritePrecision.MS)
  }
}
object RateOfChangeAlert {
  def apply(customer_id:String, exchange_id: String, asset_id: String, rateOfChange:BigDecimal, event_ts:Timestamp): RateOfChangeAlert = {
    RateOfChangeAlert(new ObjectId(), customer_id,exchange_id, asset_id, rateOfChange, event_ts)
  }
}


case class FluctuationAlert(_id:ObjectId, customer_id:String, exchange_id:String, asset_id:String, rate:BigDecimal, min_value:BigDecimal, max_value:BigDecimal, event_ts:Timestamp) extends Alert {
  def toPoint() : Point = {
    Point.measurement(classOf[FluctuationAlert].getSimpleName.toLowerCase(Locale.ENGLISH))
      .addTag("customer_id",customer_id)
      .addTag("exchange_id",exchange_id)
      .addTag("base_asset",asset_id)
      .addField("fluctuation_rate",rate.bigDecimal)
      .addField("min_value", min_value.bigDecimal)
      .addField("max_value", max_value.bigDecimal)
      .addField("event_ts", DateTimeHelper.asString(event_ts))
      .time(System.currentTimeMillis(),WritePrecision.MS)
  }
}
object FluctuationAlert {
  def apply(customer_id:String, exchange_id:String, asset_id:String, rate:BigDecimal, min_value:BigDecimal, max_value:BigDecimal, event_ts:Timestamp): FluctuationAlert = {
    FluctuationAlert(new ObjectId(), customer_id,exchange_id, asset_id, rate, min_value, max_value, event_ts)
  }
}