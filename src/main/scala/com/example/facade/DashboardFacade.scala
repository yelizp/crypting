package com.example.facade

import com.example.model.{CustomerInvestment, FluxResult, Portfolio, Tag}
import scala.collection.mutable.ListBuffer

object DashboardFacade {
  def update(): Unit = {
    val customerInvestments = MongoDBFacade.findAll[CustomerInvestment]()
    val portfolio = calculateStatistics(customerInvestments)
    if(portfolio != null) {
      portfolio.map(_.toPoint().toLineProtocol()).foreach(println)
      InfluxDBFacade.bulkWriteInsert(portfolio.map(_.toPoint()))
    }
  }

  def calculateStatistics(customerInvestments:Seq[CustomerInvestment]): Seq[Portfolio] = {
    val tags = getTags(customerInvestments)
    val first24hList = InfluxDBFacade.getMarketsFirstAndLast("24h", "first", tags)
    val lastList = InfluxDBFacade.getMarketsFirstAndLast("1h", "last", tags)

    var first24h:BigDecimal = null
    var last:BigDecimal = null
    var portfolios = ListBuffer[Portfolio]()
    var worstPerformant:Tuple2[Portfolio,BigDecimal] = null
    var topPerformant:Tuple2[Portfolio,BigDecimal] = null
    var portfolio:Portfolio = null

    customerInvestments.foreach(investment => {
      first24h = getOrDefault(investment,first24hList)
      last = getOrDefault(investment,lastList)

      if(last != null && first24h != null) {
        val percentChange24h:BigDecimal = if(first24h != null) {
          (last-first24h)/first24h
        } else {
          BigDecimal(0)
        }

        portfolio = Portfolio(investment.customer_id,
          investment.exchange_id,
          investment.base_asset,
          false,
          false,
          investment.lot,
          (last * investment.lot).bigDecimal,
          last,
          percentChange24h)

        if(topPerformant == null && percentChange24h > 0) {
          topPerformant = Tuple2(portfolio, percentChange24h)
        } else if(topPerformant != null && percentChange24h > topPerformant._2) {
          topPerformant = Tuple2(portfolio, percentChange24h)
        }

        if(worstPerformant == null && percentChange24h < 0) {
          worstPerformant = Tuple2(portfolio, percentChange24h)
        } else if(worstPerformant != null && percentChange24h < worstPerformant._2) {
          worstPerformant = Tuple2(portfolio, percentChange24h)
        }

        portfolios += portfolio
      }
    })

    if(topPerformant != null) {
      topPerformant._1.most_revaluated = true
    }

    if(worstPerformant != null) {
      worstPerformant._1.most_devaluated = true
    }

    portfolios.toSeq
  }

  def getOrDefault(investment:CustomerInvestment, results:Seq[FluxResult]) : BigDecimal = {
    var value:BigDecimal = null
    if(investment !=null && results != null && results.size > 0) {
      val seq = results.filter(r => {r.asset_id == investment.base_asset && investment.exchange_id == investment.exchange_id})
      if(seq != null && seq.size > 0) {
        value = seq(0).value
      }
    }
    value
  }

  def getTags(customerInvestments:Seq[CustomerInvestment]) : Seq[Tag] = {
    customerInvestments.map(i => { Tag(i.customer_id, i.exchange_id, i.base_asset) }).toSeq
  }
}
