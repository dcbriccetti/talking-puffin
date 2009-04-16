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
  private val latLongRegex = ("""(.*\s)?""" + num + """,\s*""" + num).r
  private val locationCache: java.util.Map[String, String] = new MapMaker().softValues().makeMap()

  /**
   * Returns the reverse-geocoded location if the location provided contains
   * latitude and longitude and a location can be located, otherwise returns
   * the original location string.
   */
  def decode(location: String): String = {
    try {
      val latLongRegex(text, lat, long) = location
      val latLong = lat + "," + long
      val cachedLoc = locationCache.get(latLong)
      if (cachedLoc != null) return cachedLoc

      val url = new URL("http://maps.google.com/maps/geo?ll=" + latLong + "&output=xml&oe=utf-8")
      val resp = XML.loadString(StreamUtil.streamToString(url.openConnection.getInputStream))
      val foundLocation = ((resp \ "Response" \ "Placemark")(0) \ "address").text
      locationCache.put(latLong, foundLocation)
      return foundLocation
    } catch {
      case e: MatchError => // No lat/long present, so do nothing
      case e: Exception => println(e)  // Unlikely error occurred
    }
    location
  }
  
}