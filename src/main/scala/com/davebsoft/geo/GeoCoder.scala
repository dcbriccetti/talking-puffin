package com.davebsoft.sctw.geo

import google.common.collect.MapMaker
import java.net.URL
import _root_.com.davebsoft.sctw.twitter.StreamUtil
import _root_.scala.xml.XML

/**
 * Geocoding.
 * 
 * @author Dave Briccetti
 */

object GeoCoder {
  private val num = """(-?\d+\.\d*)"""
  private val latLongRegex = ("""\D*""" + num + """,\s*""" + num).r
  private val locationCache: java.util.Map[String, String] = new MapMaker().softValues().makeMap()

  /**
   * Returns the reverse-geocoded location for the specified location if it exists in the cache,
   * otherwise None.
   */
  def getCachedObject(latLong: String): Option[String] = {
    val cachedLoc = locationCache.get(latLong)
    if (cachedLoc != null) Some(cachedLoc) else None
  }

  def extractLatLong(location: String): Option[String] = {
    try {
      val latLongRegex(lat, long) = location
      Some(lat + "," + long)
    } catch {
      case e: MatchError => None
    }
  }
  
  /**
   * Returns the reverse-geocoded location if the location provided contains
   * latitude and longitude and a location can be located, otherwise returns
   * the original location string.
   */
  def decode(location: String): String = {
    extractLatLong(location) match {
      case None => location
      case Some(latLong) =>
        try {
          val cachedLoc = locationCache.get(latLong)
          if (cachedLoc != null) return cachedLoc
    
          val url = new URL("http://maps.google.com/maps/geo?ll=" + latLong + "&output=xml&oe=utf-8")
          val resp = XML.loadString(StreamUtil.streamToString(url.openConnection.getInputStream))
          val foundLocation = ((resp \ "Response" \ "Placemark")(0) \ "address").text
          locationCache.put(latLong, foundLocation)
          return foundLocation
        } catch {
          case e: Exception => println(e)  // Unlikely error occurred
        }
        location
    }
  }
  
}