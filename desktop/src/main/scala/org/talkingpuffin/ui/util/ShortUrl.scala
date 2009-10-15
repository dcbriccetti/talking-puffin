package org.talkingpuffin.ui.util

import java.net.{HttpURLConnection, URL}
import javax.swing.text.JTextComponent
import org.talkingpuffin.ui.LinkExtractor

/**
 * URL shortening and expanding.
 */
object ShortUrl {
  val shortenerDomains = List("bit.ly", "ff.im", "is.gd", "ping.fm", "short.ie", "su.pr", "tinyurl.com", "tr.im")
  val regex = "http://(" + shortenerDomains.map(_.replace(".","""\.""")).mkString("|") + ")/" + 
      LinkExtractor.urlCharClass + "*"
  private val redirectionCodes = List(301, 302)
  private type LongUrlReady = ResourceReady[String,String]
  
  private val fetcher = new BackgroundResourceFetcher[String, String](processResult) {
    override def getResourceFromSource(url: String): String = {
      val conn = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
      conn.setRequestMethod("HEAD")
      conn.setInstanceFollowRedirects(false)
      if (redirectionCodes.contains(conn.getResponseCode)) conn.getHeaderField("Location") else 
        throw new NoSuchResource(url)
    }
  }

  /**
   * Substitutes long forms for all cached shortened URLs found in text, and issues requests for any 
   * not in the cache.
   */
  def substituteExpandedUrls(text: String, textComponent: JTextComponent) {
    val matcher = LinkExtractor.hyperlinkPattern.matcher(text)
    while (matcher.find) {
      val url = matcher.group(1)
      if (urlIsShortened(url)) {
        fetcher.getCachedObject(url) match {
          case Some(longUrl) => replaceUrl(textComponent, url, longUrl)
          case None => fetcher.requestItem(new FetchRequest(url, textComponent))
        }
      }
    }
  }
  
  private def urlIsShortened(url: String) = shortenerDomains.exists(url.contains(_))
  
  private def processResult(urlReady: LongUrlReady) = 
    replaceUrl(urlReady.userData.asInstanceOf[JTextComponent], urlReady.key, urlReady.resource)
  
  private def replaceUrl(textComponent: JTextComponent, shortUrl: String, location: String) = {
    val beforeText = textComponent.getText
    val afterText = beforeText.replace(shortUrl, location)
    if (beforeText != afterText) {
      textComponent setText afterText
      textComponent setCaretPosition 0
    }
  }
  
}