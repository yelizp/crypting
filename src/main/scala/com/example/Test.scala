package com.example

import com.example.facade.{AlertsFacade, ConfigurationFacade, CryptingUpFacade, DashboardFacade, InfluxDBFacade, MongoDBFacade}
import com.example.model.{Asset, Customer, CustomerInvestment, CustomerMarket, Exchange}

object Test {
  def main(args: Array[String]): Unit = {
    ConfigurationFacade.init()
    AlertsFacade.update()
    DashboardFacade.update()
    System.exit(0)
  }

  def getAllAssetsAndPersist() = {
    MongoDBFacade.dropCollection(classOf[Asset])
    val assets = CryptingUpFacade.getAllAssets()
    assets.foreach(println)
    //    val distinct = assets.groupBy(asset => (asset.asset_id, asset.name)).map(asset=> Asset.apply(_:String,_:String)).toSeq
    MongoDBFacade.insertMany(assets)
    InfluxDBFacade.bulkWriteInsert(assets.map(_.toPoint()))
  }

  def getAllExchangesAndPersist() = {
    val exchanges = CryptingUpFacade.getAllExchanges()
    exchanges.foreach(println)
    MongoDBFacade.dropCollection(classOf[Exchange])
    MongoDBFacade.insertMany(exchanges)
  }
}
