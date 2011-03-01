package org.talkingpuffin.apix

case class SourceDetails(raw: String, url: Option[String], name: String)

object SourceDetails {

  /**
   * From the “source” string, which oddly may contain either a simple string, such as “web,”
   * or an anchor tag with an href and a source name, extract:
   * <ol>
   * <li>the entire contents into {@link #source}, for backward compatibility
   * <li>a URL, if found, into {@link #sourceUrl}
   * <li>the source name into {@link #sourceName}
   * </ol>
   *
   */
  def apply(text: String): SourceDetails = {
    // XML.loadString might have been used instead of this regex, but it throws exceptions because of the contents
    val anchorRegex = """<a.*href=["'](.*?)["'].*?>(.*?)</a>""".r
    val (url, name) = text match {
      case anchorRegex(u,s) => (Some(u), s)
      case _ => (None, text)
    }
    SourceDetails(text, url, name)
  }

}