package org.talkingpuffin.geo

import java.net.URL
import _root_.scala.xml.XML
import com.google.common.collect.MapMaker
import org.talkingpuffin.util.BackgroundResourceFetcher

/**
 * Geocoding.
 */
object GeoCoder extends BackgroundResourceFetcher[String, String]("Geo") {
  private val locationCache: java.util.Map[String, String] = new MapMaker().softValues().makeMap()
  private val num = """(-?\d+\.\d*)"""
  private val latLongRegex = ("""[^-\d]*""" + num + """,\s*""" + num).r
  private val apiKey = "ABQIAAAAVOsftmRci5v5FgKzeSDTjRQRy7BtqlAMzRCsHHZRQCk8HnV1mBQ5tPe8d9oZTkHqFfsayPz758T-Mw"

  /**
   * From a (latitude, comma, optional spaces, longitude) alone or contained inside other characters, 
   * produces a (latitude, comma, longitude) String, or None if the pattern does not match.
   */
  def extractLatLong(location: String): Option[String] = location match {
    case latLongRegex(lat, long) => Some(formatLatLongKey(lat, long))
    case _ => None
  }
  
  def formatLatLongKey(lat: String, long: String): String = lat + "," + long 
  
  def formatLatLongKey(location: Tuple2[Double, Double]): String = 
    formatLatLongKey(location._1.toString, location._2.toString) 

  protected def getResourceFromSource(latLong: String): String = {
    val url = new URL("http://maps.google.com/maps/geo?key=" + GeoCoder.apiKey + 
        "&ll=" + latLong + "&output=xml&oe=utf-8")
    (XML.load(url.openConnection.getInputStream) \ "Response" \ "Placemark" \ "address").
        firstOption.map(_.text).getOrElse(latLong)
  }
  
}