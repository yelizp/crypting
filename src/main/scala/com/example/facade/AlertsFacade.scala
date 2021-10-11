package com.example.facade

import com.example.common.DateTimeHelper
import com.example.model.{Alert, CustomerMarket, DevaluationAlert, FluctuationAlert, FluxResult, RateOfChangeAlert, Tag, TargetPriceExceededAlert, TenPercentLossAlert}

import java.sql.Timestamp
import scala.collection.mutable.ListBuffer

object AlertsFacade {

  def update(): Unit = {
    val customerMarkets = MongoDBFacade.findAll[CustomerMarket]()
    val alerts = getAlerts(customerMarkets)
    if(alerts != null) {
      alerts.map(_.toPoint().toLineProtocol()).foreach(println)
      InfluxDBFacade.bulkWriteInsert(alerts.map(_.toPoint()))
    }
  }

  def getAlerts(customerMarkets:Seq[CustomerMarket]): Seq[Alert] = {
    val tags = getTags(customerMarkets)
    val first1hList = InfluxDBFacade.getMarketsFirstAndLast("1h", "first",tags)
    val first24hList = InfluxDBFacade.getMarketsFirstAndLast("24h", "first", tags)
    val lastList = InfluxDBFacade.getMarketsFirstAndLast("1h", "last", tags)

    val lowestMinList = InfluxDBFacade.getMarketsMinAndMax("24h", "lowestMin",tags)
    val highestMaxList = InfluxDBFacade.getMarketsMinAndMax("24h", "highestMax", tags)

    var first1h:BigDecimal = null
    var first24h:BigDecimal = null
    var last:BigDecimal = null
    var marketMin:BigDecimal = null
    var marketMax:BigDecimal = null
    var fluctuationRate:BigDecimal = null
    var alerts = ListBuffer[Alert]()
    var mostDevaluated:Tuple2[CustomerMarket,BigDecimal] = null
    var mostFluctuated:Tuple4[CustomerMarket,BigDecimal, BigDecimal, BigDecimal] = null

    customerMarkets.foreach(customerMarket => {
      first1h = tryGetOrDefault(customerMarket,first1hList)
      first24h = tryGetOrDefault(customerMarket,first24hList)
      last = tryGetOrDefault(customerMarket,lastList)

      marketMin = tryGetOrDefault(customerMarket,lowestMinList)
      marketMax = tryGetOrDefault(customerMarket,highestMaxList)
      fluctuationRate = if(first24h != 0) (marketMax-marketMin)/first24h else 0

      val percentChange1h:BigDecimal = if(first1h != 0) (last-first1h)/first1h else 0
      val percentChange24h:BigDecimal = if(first24h != null) (last-first24h)/first24h else 0

      if(percentChange1h <= -10) {
        alerts += TenPercentLossAlert(customerMarket.customer_id,customerMarket.exchange_id,customerMarket.base_asset,percentChange1h, "1h")
      }

      if(percentChange24h <= -10) {
        alerts += TenPercentLossAlert(customerMarket.customer_id,customerMarket.exchange_id,customerMarket.base_asset,percentChange24h, "24h")
      }

      if(customerMarket.targetPrice < last) {
        alerts += TargetPriceExceededAlert(customerMarket.customer_id,customerMarket.exchange_id,customerMarket.base_asset, customerMarket.targetPrice, last)
      }

      if(percentChange24h < 0 && (mostDevaluated == null || mostDevaluated._2 > percentChange24h)) {
        mostDevaluated = Tuple2(customerMarket, percentChange24h)
      }

      if(mostFluctuated == null || mostFluctuated._2 < fluctuationRate) {
        mostFluctuated = Tuple4(customerMarket, fluctuationRate, marketMin, marketMax)
      }
    })

    if(mostDevaluated != null) {
      val derivatives = InfluxDBFacade.getMarketDerivatives("24h", getTags(Seq(mostDevaluated._1)))
      val fluxResult = derivatives.foldLeft(null:FluxResult)((A:FluxResult,B:FluxResult) => {
        if(A == null && B!= null) B
        else if(B == null && A!= null) A
        else if(A.value < B.value) A
        else B
      })

      if(fluxResult != null) {
        alerts += DevaluationAlert(
          mostDevaluated._1.customer_id,
          mostDevaluated._1.exchange_id,
          mostDevaluated._1.base_asset,
          fluxResult.value,
          fluxResult.start)
      }

      val start = DateTimeHelper.asString(new Timestamp(fluxResult.start.getTime - 3600000))
      val end = DateTimeHelper.asString(fluxResult.start)
      val results = InfluxDBFacade.getAverageRateOfChange(start,end,tags)
      results.foreach(result => {
        alerts += RateOfChangeAlert(
          mostDevaluated._1.customer_id,
          mostDevaluated._1.exchange_id,
          mostDevaluated._1.base_asset,
          result.value,
          result.start
        )
      })
    }

    if(mostFluctuated != null) {
      val derivatives = InfluxDBFacade.getMarketDerivatives("24h", getTags(Seq(mostFluctuated._1)))
      val fluxResult = derivatives.foldLeft(null: FluxResult)((A: FluxResult, B: FluxResult) => {
        if (A == null && B != null) B
        else if (B == null && A != null) A
        else if (Math.abs(A.value.toDouble) < Math.abs(B.value.toDouble)) A
        else B
      })

      if (fluxResult != null) {
        alerts += FluctuationAlert(
          mostFluctuated._1.customer_id,
          mostFluctuated._1.exchange_id,
          mostFluctuated._1.base_asset,
          mostFluctuated._2,
          mostFluctuated._3,
          mostFluctuated._4,
          fluxResult.start)
      }
    }

    alerts.toSeq
  }

  def tryGetOrDefault(m:CustomerMarket, results:Seq[FluxResult], default:BigDecimal = 0) : BigDecimal = {
    var value:BigDecimal = null
    if(m !=null && results != null && results.size > 0) {
      val seq = results.filter(r => {r.asset_id == m.base_asset && m.exchange_id == m.exchange_id})
      if(seq != null && seq.size > 0) {
        value = seq(0).value
      }
    }
    if(value == null ) default
    else value
  }

  def getTags(customerMarkets:Seq[CustomerMarket]) : Seq[Tag] = {
    customerMarkets.map(m=> { Tag(m.customer_id, m.exchange_id, m.base_asset) }).toSeq
  }
}
