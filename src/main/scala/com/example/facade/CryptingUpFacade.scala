package com.example.facade

import com.example.model.{Asset, Exchange, Market}
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import play.api.libs.json.Json
import scala.io.Source
import scala.util.control.Breaks.{break, breakable}

object CryptingUpFacade {
  private val size = 1000
  lazy private val urlMarkets = ConfigurationFacade.getProperty("cryptingup.api.markets.url") + "?size="
  lazy private val urlAssets = ConfigurationFacade.getProperty("cryptingup.api.assets.url") + "?size="
  lazy private val urlExchanges = ConfigurationFacade.getProperty("cryptingup.api.exchanges.url") + "?size=100000"

  def getAllMarkets() : Seq[Market] = {
    var url = urlMarkets + size
    var content = getRestContent(url)
    var markets:Seq[Market] = Seq()
    if(!content.isEmpty) {
      var json = Json.parse(content)
      markets ++= (json \ "markets").as[Seq[Market]]
      var next = (json \ "next").as[String]
      breakable {
        while (!next.isEmpty) {
          url = urlMarkets + next
          content = getRestContent(url)
          if (content.isEmpty) break
          json = Json.parse(content)
          markets ++= (json \ "markets").as[Seq[Market]]
          next = (json \ "next").as[String]
        }
      }
    }
    markets
  }

  def getAllAssets() : Seq[Asset] = {
    var url = urlAssets + size
    var assets:Seq[Asset] = Seq()
    var content = getRestContent(url)
    if(!content.isEmpty) {
      var json = Json.parse(content)
      assets ++= (json \ "assets").as[Seq[Asset]]
      var next = (json \ "next").as[String]
      breakable {
        while (!next.isEmpty) {
          url = urlMarkets + next
          content = getRestContent(url)
          if (content.isEmpty) break
          json = Json.parse(content)
          assets ++= (json \ "assets").as[Seq[Asset]]
          next = (json \ "next").as[String]
        }
      }
    }
    assets
  }

  def getAllExchanges() : Seq[Exchange] = {
    val content = getRestContent(urlExchanges)
    var exchanges:Seq[Exchange] = null
    val json = Json.parse(content)
    exchanges ++= (json \ "exchanges").as[Seq[Exchange]]
    exchanges
  }

  def getRestContent(url:String) : String = {
    val httpClient = HttpClientBuilder.create.build
    var content = ""
    try {
      val httpResponse = httpClient.execute(new HttpGet(url))
      val entity = httpResponse.getEntity()
      if (entity != null) {
        val inputStream = entity.getContent()
        content = Source.fromInputStream(inputStream).getLines.mkString
        inputStream.close
      }
    } catch {
      case e:Exception => {
        e.printStackTrace()
        println(e.getMessage())
      }
    } finally {
      try {
        if(httpClient!= null) httpClient.close()
      } catch {
        case e: Exception => {
          println(e.getMessage)
          e.printStackTrace()
        }
      }
    }

    content
  }
}
