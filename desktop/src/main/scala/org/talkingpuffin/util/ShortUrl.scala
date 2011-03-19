package org.talkingpuffin.util

/**
 * URL shortening and expanding.
 */
object ShortUrl extends Loggable {
  private val shortenerRegexStrings = List("""http://digg\.com/""" + LinkExtractor.urlCharClass + "{4,10}")
  private val shortenerRegexes = shortenerRegexStrings.map(_.r)
  private val redirBypassesWrapperHosts = List("su.pr", "ow.ly")
  private val shortenerDomains = List("bit.ly", "dzone.com", "ff.im", "is.gd", "j.mp", "ping.fm",
    "r2.ly", "short.ie", "su.pr", 
    "tinyurl.com", "tr.im", "goo.gl", "t.co", "huff.to", "scoble.it", "oreil.ly",
    "wapo.st") ::: redirBypassesWrapperHosts
  private val regex = "http://(" + shortenerDomains.map(_.replace(".","""\.""")).mkString("|") + ")/" +
      LinkExtractor.urlCharClass + "*"
  private type LongUrlReady = ResourceReady[String,String]
  
  private val fetcher = new BackgroundResourceFetcher[String, String]("URL") {
    override def getResourceFromSource(urlString: String): String = {
      try {
        UrlExpander.expand(urlString)
      } catch {
        case ex: UrlExpander.NoRedirection => throw new NoSuchResource(urlString)
      }
    }
  }

  /**
   * If simply doing HTTP HEAD to get Location suffices to bypass the wrapper
   */
  def redirectionBypassesWrapper(host: String) = redirBypassesWrapperHosts contains host
  
  def substituteShortenedUrlWith(text: String, replacement: String) = {
    (regex :: shortenerRegexStrings).foldLeft(text)(_.replaceAll(_, replacement))
  }

  /**
   * Gets the long form, if there is one, for the specified URL.
   */
  def getExpandedUrl(url: String, provideExpandedUrl: (String) => Unit) = {
    if (urlIsShortened(url)) {
      fetcher.get(provideExpandedUrl)(url)
    }
  }
  
  /**
   * Gets the long forms, if they exist, for all cached shortened URLs found in text.
   */
  def getExpandedUrls(text: String, provideSourceAndTargetUrl: (String, String) => Unit) = {
    val matcher = LinkExtractor.hyperlinkPattern.matcher(text)
    while (matcher.find) {
      val sourceUrl = matcher.group(1)
      getExpandedUrl(sourceUrl, (targetUrl: String) => {provideSourceAndTargetUrl(sourceUrl, targetUrl)})
    }
  }
  
  private def urlIsShortened(url: String) = shortenerDomains.exists(url.contains(_)) ||
    shortenerRegexes.exists(r => url match {case r() => true case _ => false})
  
}
