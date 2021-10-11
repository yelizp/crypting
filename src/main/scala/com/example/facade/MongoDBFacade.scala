package com.example.facade

import com.example.common.BigDecimalScalaCodec
import com.example.model.{Asset, Customer, CustomerInvestment, CustomerMarket, Exchange, Market}
import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase, _}

import java.util.Locale
import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object MongoDBFacade {
  val codecRegistry = fromRegistries(
    fromProviders(
      classOf[Market],
      classOf[Customer],
      classOf[CustomerInvestment],
      classOf[CustomerMarket],
      classOf[Asset],
      classOf[Exchange]
    ),
    DEFAULT_CODEC_REGISTRY,
    fromCodecs(new BigDecimalScalaCodec()))

  val user: String = ConfigurationFacade.getProperty("mongodb.username")                        // the user name
  val source: String = ConfigurationFacade.getProperty("mongodb.database")                      // the source where the user is defined
  val password: Array[Char] = ConfigurationFacade.getProperty("mongodb.password").toCharArray   // the password as a character array
  val credential = MongoCredential.createCredential(user, source, password)
  lazy val mongoClient: MongoClient = MongoClient(MongoClientSettings.builder().credential(credential).build())
  lazy val database: MongoDatabase = mongoClient.getDatabase(source).withCodecRegistry(codecRegistry)

  def listCollections(): Seq[String] = {
    Await.result(database.listCollectionNames().toFuture(),Duration(10, TimeUnit.SECONDS))
  }

  def createCollection(c: Class[_]) = {
    val collectionName = c.getSimpleName.toLowerCase(Locale.ENGLISH)
    Await.result(database.createCollection(collectionName).toFuture(),Duration(10, TimeUnit.SECONDS))
  }

  def dropCollection(c: Class[_]) = {
    val collectionName = c.getSimpleName.toLowerCase(Locale.ENGLISH)
    Await.result(database.getCollection(collectionName).drop().toFuture(),Duration(10, TimeUnit.SECONDS))
  }

  def insertOne[T](obj:T)(implicit m: Manifest[T]) = {
    val collectionName = m.runtimeClass.getSimpleName.toLowerCase(Locale.ENGLISH)
    val collection: MongoCollection[T] = database.getCollection[T](collectionName)
    Await.result(collection.insertOne(obj).toFuture(), Duration(10, TimeUnit.SECONDS))
  }

  def insertMany[T](objList:Seq[T])(implicit m: Manifest[T]) = {
    val collectionName = m.runtimeClass.getSimpleName.toLowerCase(Locale.ENGLISH)
    val collection: MongoCollection[T] = database.getCollection[T](collectionName)
    val observable = collection.insertMany(objList)

    Await.result(observable.toFuture(), Duration(10, TimeUnit.SECONDS))
    val result = Await.result(collection.countDocuments().toFuture(), Duration(10, TimeUnit.SECONDS))

    println(s"total # of documents : ${result}")
  }

  def findAll[T]() (implicit m: Manifest[T]): Seq[T] = {
    val collectionName = m.runtimeClass.getSimpleName.toLowerCase(Locale.ENGLISH)
    val collection: MongoCollection[T] = database.getCollection(collectionName)
    Await.result(collection.find[T]().toFuture(), Duration(10, TimeUnit.SECONDS))
  }
}
