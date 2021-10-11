package com.example

import com.example.common.ScheduledTask
import com.example.model.{Asset, Customer, CustomerInvestment, CustomerMarket, Exchange}
import com.example.facade.{MarketFacade,AlertsFacade, ConfigurationFacade, CryptingUpFacade, DashboardFacade, InfluxDBFacade, MongoDBFacade}

object CryptoAssetsMain {
  def main(args: Array[String]): Unit = {
    ConfigurationFacade.init()
    createCustomerInfoAndPersist()

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
      CustomerMarket("yeliz","COINBASE", "ETH", 0),
      CustomerMarket("yeliz","COINBASE","DOGE", 0),
      CustomerMarket("yeliz","BITFINEX", "IOT", 0),
      CustomerMarket("yeliz","KRAKEN", "NANO", 0)
    )

    val customerInvestments = Seq[CustomerInvestment](
      CustomerInvestment("yeliz","COINBASE","BTC", 10),
      CustomerInvestment("yeliz","COINBASE","ETH", 20),
      CustomerInvestment("yeliz","COINBASE", "DOGE", 10),
      CustomerInvestment("yeliz","BITFINEX", "IOT", 5),
      CustomerInvestment("yeliz","KRAKEN", "LUNA", 30)
    )

    val customer = Customer("yeliz","Yeliz Pehlivanoglu")
    MongoDBFacade.insertOne(customer)
    MongoDBFacade.insertMany(customerMarkets)
    MongoDBFacade.insertMany(customerInvestments)
  }

}
