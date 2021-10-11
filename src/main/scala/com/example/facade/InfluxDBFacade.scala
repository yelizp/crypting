package com.example.facade
import com.example.model.{FluxResult, Tag}
import com.influxdb.client.write.Point
import com.influxdb.client.{InfluxDBClient, InfluxDBClientFactory, WriteApiBlocking}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object InfluxDBFacade {

  def bulkWriteInsert(points:Seq[Point]) = {
    var influxDBClient: InfluxDBClient = null
    try {
      influxDBClient = InfluxDBClientFactory.create().enableGzip()
      val writeApi: WriteApiBlocking = influxDBClient.getWriteApiBlocking()
      writeApi.writePoints(points.asJava)
    } catch {
      case e:Exception => {
        println(e.getMessage)
        e.printStackTrace()
      }
    } finally {
      safeClose(influxDBClient)
    }
  }

  def buildMarketFilter(rangeStart:String, rangeStop:String, tags:Seq[Tag]) : String = {
    val buffer = new StringBuffer()
    val stop = if(rangeStop == null || rangeStop.isEmpty) "now()" else rangeStop

    buffer.append("from(bucket: \"grafana\")\n")
      .append("|> range(start: -").append(rangeStart).append(", stop: ").append(stop).append(")\n")
      .append("|> filter(fn: (r) => r[\"_measurement\"] == \"market\")\n")
      .append("|> filter(fn: (r) => r[\"_field\"] == \"price\")\n")

    var count=0
    tags.foreach(tag => {
      if(count==0) {
        buffer.append("|> filter(fn: (r) => (r.base_asset == \"").append(tag.asset_id)
          .append("\" and r.exchange_id == \"").append(tag.exchange_id).append("\")\n")
        count += 1
      } else {
        buffer.append(" or (r.base_asset == \"").append(tag.asset_id)
          .append("\" and r.exchange_id == \"").append(tag.exchange_id).append("\")\n")
      }
    })
    buffer.append(")\n")
    buffer.toString()
  }

  def getAverageRateOfChange(rangeStart:String, rangeStop:String, tags:Seq[Tag]) : Seq[FluxResult] = {
    var influxDBClient: InfluxDBClient = null
    var results = new ListBuffer[FluxResult]()
    if(tags == null || tags.size == 0 ) {
      return results.toSeq
    }
    try {
      influxDBClient = InfluxDBClientFactory.create().enableGzip()
      val buffer = new StringBuffer()
      buffer.append("import \"experimental/aggregate\"\n")
      buffer.append("from(bucket: \"grafana\")\n")
        .append("|> range(start: ").append(rangeStart).append(", stop: ").append(rangeStop).append(")\n")
        .append("|> filter(fn: (r) => r[\"_measurement\"] == \"market\")\n")
        .append("|> filter(fn: (r) => r[\"_field\"] == \"price\")\n")

      var count=0
      tags.foreach(tag => {
        if(count==0) {
          buffer.append("|> filter(fn: (r) => (r.base_asset == \"").append(tag.asset_id)
            .append("\" and r.exchange_id == \"").append(tag.exchange_id).append("\")\n")
          count += 1
        } else {
          buffer.append(" or (r.base_asset == \"").append(tag.asset_id)
            .append("\" and r.exchange_id == \"").append(tag.exchange_id).append("\")\n")
        }
      })
      buffer.append(")\n")
      buffer.append("|> aggregate.rate(\n every: 1h,\n unit: 2m,\n groupColumns: [\"exchange_id\", \"base_asset\"]\n  )")
      buffer.append("|> last()")
      val queryApi = influxDBClient.getQueryApi
      val tables = queryApi.query(buffer.toString)
      tables.forEach(fluxTable => {
        val records = fluxTable.getRecords()
        records.forEach(record => {
          results += FluxResult(record)})
      })
      results.toSeq
    } catch {
      case e:Exception => {
        println(e.getMessage)
        e.printStackTrace()
        throw e
      }
    } finally {
      safeClose(influxDBClient)
    }
  }

  def getMarketDerivatives(rangeStart:String, tags:Seq[Tag])  : Seq[FluxResult] = {
    var influxDBClient: InfluxDBClient = null
    var results = new ListBuffer[FluxResult]()
    if(tags == null || tags.size == 0 ) {
      return results.toSeq
    }
    try {
      influxDBClient = InfluxDBClientFactory.create().enableGzip()
      val buffer = new StringBuffer()
      buffer.append(buildMarketFilter(rangeStart,null, tags))
        .append("|> derivative(unit: 2m, nonNegative: false)\n")
        .append("|> yield(name: \"derivative\")")

      val queryApi = influxDBClient.getQueryApi
      val tables = queryApi.query(buffer.toString)
      tables.forEach(fluxTable => {
        val records = fluxTable.getRecords()
        records.forEach(record => {
          results += FluxResult(record)})
      })
      results.toSeq
    } catch {
      case e:Exception => {
        println(e.getMessage)
        e.printStackTrace()
        throw e
      }
    } finally {
      safeClose(influxDBClient)
    }
  }

  def safeClose(influxDBClient: InfluxDBClient) : Unit = {
    try {
      if(influxDBClient != null) influxDBClient.close()
    } catch {
      case e:Exception => {
        println(e.getMessage)
        e.printStackTrace()
      }
    }
  }

  def getMarketsMinAndMax(rangeStart:String, aggregateFunction: String, tags:Seq[Tag])  : Seq[FluxResult] = {
    var influxDBClient: InfluxDBClient = null
    var results = new ListBuffer[FluxResult]()
    if(tags == null || tags.size == 0 ) {
      return results.toSeq
    }

    try {
      influxDBClient = InfluxDBClientFactory.create().enableGzip()

      val buffer = new StringBuffer()
      buffer.append(buildMarketFilter(rangeStart,null, tags))
        .append("|> ").append(aggregateFunction).append("(n:").append(tags.size)
        .append(", column: \"_value\", groupColumns: [\"exchange_id\", \"base_asset\"]\n)")
        .append("|> yield(name: \"").append(aggregateFunction).append("\")")

      val queryApi = influxDBClient.getQueryApi
      val tables = queryApi.query(buffer.toString())
      tables.forEach(fluxTable => {
        val records = fluxTable.getRecords()
        records.forEach(record => {
          results += FluxResult(record)})
      })
      results.toSeq
    } catch {
      case e:Exception => {
        println(e.getMessage)
        e.printStackTrace()
        throw e
      }
    } finally {
      safeClose(influxDBClient)
    }
  }

  def getMarketsFirstAndLast(rangeStart:String, aggregateFunction: String, tags:Seq[Tag]) : Seq[FluxResult] = {
    var influxDBClient: InfluxDBClient = null
    var results = new ListBuffer[FluxResult]()
    if(tags == null || tags.length == 0 ) {
      return results.toSeq
    }

    try {
      influxDBClient = InfluxDBClientFactory.create().enableGzip()
      val buffer = new StringBuffer()
      buffer.append(buildMarketFilter(rangeStart,null, tags))
        .append("|> aggregateWindow(every: ").append(rangeStart).append(", fn: ")
        .append(aggregateFunction).append(", createEmpty: false)\n")
        .append("|> ").append(aggregateFunction).append("()\n")
        .append("|> yield(name: \"").append(aggregateFunction).append("\")")

      val queryApi = influxDBClient.getQueryApi
      val tables = queryApi.query(buffer.toString())
      tables.forEach(fluxTable => {
        val records = fluxTable.getRecords()
        records.forEach(record => {
          results += FluxResult(record)})
      })
      results.toSeq
    } catch {
      case e:Exception => {
        println(e.getMessage)
        e.printStackTrace()
        throw e
      }
    } finally {
      safeClose(influxDBClient)
    }
  }
}
