package com.example.facade

object MarketFacade {
  def update(): Unit = {
    val markets = CryptingUpFacade.getAllMarkets()
    println("Markets size: " + markets.length)
    markets.map(_.toPoint())
    InfluxDBFacade.bulkWriteInsert(markets.map(_.toPoint()))
  }
}
