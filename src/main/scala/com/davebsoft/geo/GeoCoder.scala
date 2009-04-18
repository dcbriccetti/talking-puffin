package com.davebsoft.sctw.geo

import google.common.collect.MapMaker
import java.net.URL
import _root_.com.davebsoft.sctw.twitter.StreamUtil
import _root_.scala.xml.XML
import ui.util.{ResourceReady, BackgroundResourceFetcher}

/**
 * Geocoding.
 * 
 * @author Dave Briccetti
 */
class GeoCoder(processResults: (ResourceReady[String,String]) => Unit) 
    extends BackgroundResourceFetcher[String, String](processResults) {
  private val num = """(-?\d+\.\d*)"""
  private val latLongRegex = ("""\D*""" + num + """,\s*""" + num).r
  private val locationCache: java.util.Map[String, String] = new MapMaker().softValues().makeMap()

  def extractLatLong(location: String): Option[String] = {
    try {
      val latLongRegex(lat, long) = location
      Some(lat + "," + long)
    } catch {
      case e: MatchError => None
    }
  }
  
  protected def getResourceFromSource(latLong: String): String = {
    val url = new URL("http://maps.google.com/maps/geo?ll=" + latLong + "&output=xml&oe=utf-8")
    val resp = XML.loadString(StreamUtil.streamToString(url.openConnection.getInputStream))
    ((resp \ "Response" \ "Placemark")(0) \ "address").text
  }
  
}