package com.example.common

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

object DateTimeHelper {
  val utcWithTimeZoneFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.of ( "UTC" ))
  val localWithTimeZoneFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.systemDefault())
  def asTimestamp(value:String) : Timestamp = {
    var result:Timestamp = null
    if(value != null && value.length >= 19) {
      result = Timestamp.from(Instant.parse(value.substring(0,19)+"Z"))
    }
    result
  }
  def asString(value:Timestamp) : String = {
    utcWithTimeZoneFormatter.format(value.toInstant)+"Z"
  }
}
