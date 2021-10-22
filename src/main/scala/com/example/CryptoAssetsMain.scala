package com.example

import com.example.common.ScheduledTask
import com.example.model.{Asset, Customer, CustomerInvestment, CustomerMarket, Exchange}
import com.example.facade.{AlertsFacade, ConfigurationFacade, CryptingUpFacade, DashboardFacade, InfluxDBFacade, MarketFacade, MongoDBFacade}

import java.util.Locale

object CryptoAssetsMain {
  def main(args: Array[String]): Unit = {
    ConfigurationFacade.init()
    if(args.length > 0 && args(0).toLowerCase(Locale.ENGLISH).endsWith("createcustomer")) {
      createCustomerInfoAndPersist()
    }

    val poolPeriodInMinutes = ConfigurationFacade.getProperty("pool.period.in.minutes").toInt
    new ScheduledTask().run(MarketFacade.update, poolPeriodInMinutes)
    new ScheduledTask().run(AlertsFacade.update, poolPeriodInMinutes)
    new ScheduledTask().run(DashboardFacade.update, poolPeriodInMinutes)
  }

  def createCustomerInfoAndPersist(): Unit = {
    MongoDBFacade.dropCollection(classOf[Customer])
    MongoDBFacade.dropCollection(classOf[CustomerMarket])
    MongoDBFacade.dropCollection(classOf[CustomerInvestment])
    val customerMarkets = Seq(
      CustomerMarket("yeliz", "COINBASE", "BTC", 0),
      CustomerMarket("yeliz","COINBASE", "ETH", 3.8),
      CustomerMarket("yeliz","COINBASE","DOGE", 0),
      CustomerMarket("yeliz","BITTREX", "ADA", 2.15),
      CustomerMarket("yeliz","KRAKEN", "USDT", 0)
    )

    val customerInvestments = Seq[CustomerInvestment](
      CustomerInvestment("yeliz","COINBASE","BTC", 1),
      CustomerInvestment("yeliz","COINBASE","ETH", 200),
      CustomerInvestment("yeliz","COINBASE", "DOGE", 100000),
      CustomerInvestment("yeliz","BITFINEX", "IOT", 50000),
      CustomerInvestment("yeliz","KRAKEN", "NANO", 30000)
    )

    val customer = Customer("yeliz","Yeliz Pehlivanoglu")
    MongoDBFacade.insertOne(customer)
    MongoDBFacade.insertMany(customerMarkets)
    MongoDBFacade.insertMany(customerInvestments)
  }

}
