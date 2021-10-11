package com.example.facade

import com.example.model.{CustomerInvestment, CustomerMarket}

object CustomerFacade {
  def update(): Unit = {
    def poolCustomerPortfolioAndPersist():Unit = {
      val customerPortfolios = MongoDBFacade.findAll[CustomerInvestment]()
      InfluxDBFacade.bulkWriteInsert(customerPortfolios.map(_.toPoint()))
    }

    def poolCustomerMarketsAndPersist(): Unit = {
      val customerMarkets = MongoDBFacade.findAll[CustomerMarket]()
      InfluxDBFacade.bulkWriteInsert(customerMarkets.map(_.toPoint()))
    }
  }
}
