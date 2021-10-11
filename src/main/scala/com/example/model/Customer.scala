package com.example.model
import org.mongodb.scala.bson.ObjectId


case class Customer(_id: ObjectId, customer_id: String, name: String) extends Entity

object Customer {
  def apply(customer_id: String, name: String): Customer = {
    Customer(new ObjectId(), customer_id, name)
  }
}
