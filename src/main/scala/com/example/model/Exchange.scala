package com.example.model

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._
import play.api.libs.functional.syntax._

object Exchange {
  def apply(exchange_id:String, name:String): Exchange =
    Exchange(new ObjectId, exchange_id, name)

  implicit val jsonReads: Reads[Exchange] = (
    (JsPath \ "exchange_id").read[String] and
      (JsPath \ "name").read[String]
    )(Exchange.apply(_:String,_:String))
}

case class Exchange(_id:ObjectId,
                    exchange_id:String,
                    name:String) extends Entity