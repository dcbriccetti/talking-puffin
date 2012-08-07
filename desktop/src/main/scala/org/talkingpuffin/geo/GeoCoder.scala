package org.talkingpuffin.geo

import java.net.URL
import xml.XML
import org.talkingpuffin.util.{Loggable, BackgroundResourceFetcher}

/**
 * Geocoding.
 */
object GeoCoder extends BackgroundResourceFetcher[String]("Geo", numThreads = 2) with Loggable {
  private val latLongRegex = {
    val num = """(-?\d+\.\d*)"""
    val latLongRexexSource = num + """,\s*""" + num
    info(latLongRexexSource)
    latLongRexexSource.r
  }
  private val apiKey = "ABQIAAAAVOsftmRci5v5FgKzeSDTjRQRy7BtqlAMzRCsHHZRQCk8HnV1mBQ5tPe8d9oZTkHqFfsayPz758T-Mw"

  /**
   * From a (latitude, comma, optional spaces, longitude) alone or contained inside other characters, 
   * produces a (latitude, comma, longitude) String, or None if the pattern does not match.
   */
  def extractLatLong(location: String): Option[String] = {
    info(location)
    val lr = location.replaceAll(".*: ?", "")
    info(lr)
    lr match {
      case latLongRegex(lat, long) =>
        info("match %s %s".format(lat,long))
        Some(formatLatLongKey(lat, long))
      case _ =>
        info("no match")
        None
    }
  }

  def formatLatLongKey(lat: String, long: String): String = lat + "," + long 
  
  def formatLatLongKey(location: Tuple2[Double, Double]): String = 
    formatLatLongKey(location._1.toString, location._2.toString) 

  protected def getResourceFromSource(latLong: String): String = {
    val url = new URL("http://maps.google.com/maps/geo?key=" + GeoCoder.apiKey + 
        "&ll=" + latLong + "&output=xml&oe=utf-8")
    (XML.load(url.openConnection.getInputStream) \ "Response" \ "Placemark" \ "address").
        headOption.map(_.text).getOrElse(latLong)
  }
}