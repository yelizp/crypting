package com.example.model

import com.influxdb.query.FluxRecord
import java.sql.Timestamp


case class FluxResult(exchange_id:String,
                      asset_id:String,
                      value:BigDecimal,
                      start:Timestamp,
                      stop:Timestamp,
                      time:Timestamp) extends Entity

object FluxResult {
  def apply(record:FluxRecord) : FluxResult = {
    val exchange_id:String = record.getValueByKey("exchange_id").asInstanceOf[String]
    val base_asset:String = record.getValueByKey("base_asset").asInstanceOf[String]
    val value:BigDecimal = BigDecimal(record.getValue().asInstanceOf[Double])
    val start:Timestamp = Timestamp.from(record.getStart)
    val stop:Timestamp = Timestamp.from(record.getStop)
    val time:Timestamp = Timestamp.from(record.getTime)
    FluxResult(exchange_id,base_asset, value, start, stop, time)
  }
}
