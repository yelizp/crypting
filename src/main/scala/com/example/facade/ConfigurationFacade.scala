package com.example.facade

import java.io.InputStream
import java.util.Properties

object ConfigurationFacade {
  private val properties = new Properties()
  private val resourceName = "application.properties"

  def init(): Unit = {
    val loader:ClassLoader = Thread.currentThread().getContextClassLoader()
      try {
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2")
        val resourceStream:InputStream = loader.getResourceAsStream(resourceName)
        properties.load(resourceStream)
      } catch {
        case e:Exception => {
          println(e.getMessage)
          e.printStackTrace()
          throw e
        }
      }
  }

  def getProperty(key:String) : String = {
    properties.getProperty(key)
  }
}
