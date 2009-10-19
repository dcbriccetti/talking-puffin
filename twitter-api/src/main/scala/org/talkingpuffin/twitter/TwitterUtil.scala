package org.talkingpuffin.twitter

import java.net.{URL}
import java.util.Locale
import org.joda.time.format.DateTimeFormat
import scala.xml._

/**
* Provides generalized processing of a Twitter XML response.
* Under the covers this uses the specified HTTP object and URL to get an XML document, 
* and then processes the document with the specified factory, building a list (or single instance) 
* of whatever the factory returns.
*/
class Parser[T](url: URL, http: Http, factory: (Node) => T){
  /**
  * build a list of instances of T from the returned XML document
  */
  def parseXMLList(selectors: String*): XmlResult[T] = {
    var list = List[T]()
    val xml = http.get(url)
    applySelectors(xml, selectors.toList) foreach {(entry) =>
      list = factory(entry) :: list
    }
    XmlResult(list, (xml\"next_cursor").firstOption.map(_.text.toLong))
  }
  
  private def applySelectors(n: NodeSeq, sels: List[String]): NodeSeq = {
    sels match {
      case s :: rest => applySelectors(n \ s, rest)
      case Nil => n
    }
  }

  /**
  * build a single instance of T from the returned XML document
  */
  def parseXMLElement(): T = {
    return factory(http.get(url))
  }
}

trait Validated{
  def isValid(): Boolean
}

object DateTimeFormats {
  val fmt1 = DateTimeFormat.forPattern("EE MMM dd HH:mm:ss Z yyyy").withLocale(Locale.US)
}

case class XmlResult[T](val list: List[T], val cursor: Option[Long])
