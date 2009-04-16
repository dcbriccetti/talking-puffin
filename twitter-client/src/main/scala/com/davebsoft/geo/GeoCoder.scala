package com.davebsoft.sctw.geo

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

  /**
   * Returns the reverse-geocoded location if the location provided contains
   * latitude and longitude and a location can be located, otherwise returns
   * the original location string.
   */
  def decode(location: String): String = {
    try {
      val latLongRegex(text, lat, long) = location
      val url = new URL("http://maps.google.com/maps/geo?ll=" + lat +"," + long + "&output=xml&oe=utf-8")
      val resp = XML.loadString(StreamUtil.streamToString(url.openConnection.getInputStream))
      return ((resp \ "Response" \ "Placemark")(0) \ "address").text
    } catch {
      case e: MatchError => // No lat/long present, so do nothing
      case e => println(e)  // Unlikely error occurred
    }
    location
  }
  
}