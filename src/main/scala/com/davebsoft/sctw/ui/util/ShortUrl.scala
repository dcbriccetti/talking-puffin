package com.davebsoft.sctw.ui.util

import java.awt.{Rectangle, Point}
import java.net.{HttpURLConnection, URL}
import javax.swing.text.JTextComponent
import javax.swing.{SwingWorker, JScrollPane}

/**
 * URL shortening and expanding.
 * @author Dave Briccetti
 */
object ShortUrl {
  val domains = List("bit.ly", "ff.im", "is.gd", "tinyurl.com", "tr.im")
  val redirectionCodes = List(301, 302)
  
  def expand(url: String): Option[String] = {
    val conn = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("HEAD")
    conn.setInstanceFollowRedirects(false)
    if (redirectionCodes.contains(conn.getResponseCode)) Some(conn.getHeaderField("Location")) else None
  }
  
  def substituteExpandedUrls(text: String, target: JTextComponent) {
    val m = LinkExtractor.hyperlinkPattern.matcher(text)
    while (m.find) {
      val url = m.group(1)
      if (ShortUrl.domains.filter(s => url.contains(s)).length > 0) {
        new SwingWorker[Option[String],Object] {
          def doInBackground = {ShortUrl.expand(url)}
          override def done = {
            get match {
              case Some(location) =>
                val beforeText = target.getText
                val afterText = beforeText.replace(url, location)
                if (beforeText != afterText) {
                  target setText afterText
                  target scrollRectToVisible new Rectangle(0,0,1,1) // TODO get this scroll to top working
                }
              case None =>
            }
          }
        }.execute
      }
    }
  }
}