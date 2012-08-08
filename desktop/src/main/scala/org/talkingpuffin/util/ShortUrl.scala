package org.talkingpuffin.util

/**
 * URL shortening and expanding.
 */
object ShortUrl extends Loggable {
  private val shortenerRegexStrings = List("""http://digg\.com/""" + LinkExtractor.urlCharClass + "{4,10}")
  private val shortenerRegexes = shortenerRegexStrings.map(_.r)
  private val wrapperBypassableWithSimpleRedirectionHosts = List("su.pr", "ow.ly")
  private val shortenerHosts = List("bit.ly", "dzone.com", "ff.im", "is.gd", "j.mp", "ping.fm",
    "r2.ly", "short.ie", "tinyurl.com", "tr.im", "goo.gl", "t.co", "huff.to", "scoble.it", "oreil.ly",
    "wapo.st") ::: wrapperBypassableWithSimpleRedirectionHosts
  private val regex = "http://(" + shortenerHosts.map(_.replace(".","""\.""")).mkString("|") + ")/" +
      LinkExtractor.urlCharClass + "*"
  private type LongUrlReady = ResourceReady[String]
  
  private val fetcher = new BackgroundResourceFetcher[String]("URL", numThreads = 20) {
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
  def wrapperBypassableWithSimpleRedirection(host: String) =
    wrapperBypassableWithSimpleRedirectionHosts contains host
  
  def substituteShortenedUrlWith(text: String, replacement: String) = {
    (regex :: shortenerRegexStrings).foldLeft(text)(_.replaceAll(_, replacement))
  }

  /**
   * Gets the long form, if there is one, for the specified URL.
   */
  def expandUrl(url: String, expandedUrlCallback: String => Unit) {
    if (urlIsShortened(url))
      fetcher.get(expandedUrlCallback)(url)
  }

  /**
   * Gets the long forms, if they exist, for all cached shortened URLs found in text.
   */
  def expandUrls(text: String, provideSourceAndTargetUrl: (String, String) => Unit) {
    val matcher = LinkExtractor.hyperlinkPattern.matcher(text)
    while (matcher.find) {
      val sourceUrl = matcher.group(1)
      expandUrl(sourceUrl, (targetUrl: String) => provideSourceAndTargetUrl(sourceUrl, targetUrl))
    }
  }

  private def urlIsShortened(url: String) = shortenerHosts.exists(url.contains(_)) ||
    shortenerRegexes.exists(r => url match {case r() => true case _ => false})
}
