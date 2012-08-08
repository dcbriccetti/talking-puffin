package org.talkingpuffin.geo

import java.net.URL
import xml.XML
import org.talkingpuffin.util.BackgroundResourceFetcher

/**
 * Geocoding.
 */
object GeoCoder extends BackgroundResourceFetcher[String]("Geo", numThreads = 2) {
  private val latLongRegex = {
    val num = """(-?\d+\.\d*)"""
    (num + """,\s*""" + num).r
  }
  private val apiKey = "ABQIAAAAVOsftmRci5v5FgKzeSDTjRQRy7BtqlAMzRCsHHZRQCk8HnV1mBQ5tPe8d9oZTkHqFfsayPz758T-Mw"

  /**
   * From a (latitude, comma, optional spaces, longitude) alone or contained inside other characters, 
   * produces a (latitude, comma, longitude) String, or None if the pattern does not match.
   */
  def extractLatLong(location: String): Option[String] = {
    val locWithoutPrefix = location.replaceAll(".*: ?", "") // Such as iPhone:
    locWithoutPrefix match {
      case latLongRegex(lat, long) =>
        Some(formatLatLongKey(lat, long))
      case _ =>
        None
    }
  }

  def formatLatLongKey(lat: String, long: String): String = lat + "," + long 
  
  protected def getResourceFromSource(latLong: String): String = {
    val url = new URL("http://maps.google.com/maps/geo?key=" + GeoCoder.apiKey + 
        "&ll=" + latLong + "&output=xml&oe=utf-8")
    (XML.load(url.openConnection.getInputStream) \ "Response" \ "Placemark" \ "address").
        headOption.map(_.text).getOrElse(latLong)
  }
}