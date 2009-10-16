package org.talkingpuffin.twitter

import scala.xml.Node
import java.net.{URLEncoder, URL}

/**
* Provides an interface to Twitter for all non-authorized calls.
* This class provides a limited set of functionality, but should be
* safe to use against the 70 calls/hr rate limit.
*
* If you need access to the authenticated calls, see AuthenticatedSession.
*
* This class should be completely thread safe, allowing multiple simultaneous calls to Twitter via this object.
*
* All methods are fairly direct representations of calls specified in the 
* <a href="http://groups.google.com/group/twitter-development-talk/web/api-documentation">Twitter API Doc</a>
*/
class UnauthenticatedSession(apiURL: String) extends TwitterSession{

  def this() = this(API.defaultURL)
  
  def loadAll[T](f:(Int) => List[T]):List[T] = loadAll(1,f,List[T]())

  private def loadAll[T](page: Int, f:(Int) => List[T], listIn: List[T]):List[T] = {
      f(page) match {
          case Nil => listIn
          case l => loadAll(page+1,f,listIn ::: l)
      }
  }
  
  def loadAllWithCursor[T](f: (Long) => XmlResult[T]): List[T] = loadAllWithCursor(-1,f,List[T]())

  private def loadAllWithCursor[T](cursor: Long, f:(Long) => XmlResult[T], listIn: List[T]): List[T] = {
    val result = f(cursor)
    val newList = listIn ::: result.list
    result.cursor match {
      case Some(cursor) if cursor > 0 => loadAllWithCursor(cursor, f, newList)
      case _ => newList
    }
  }
  
  /** utility class to connect to a URL and fetch XML. */
  private val http = new Http(None, None)
  
  def httpPublisher = http
  
  def getPublicTimeline(): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/public_timeline.xml"),http,
        TwitterStatus.apply).parseXMLList("status").list
  }

  def getPublicTimeline(page: Int): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/public_timeline.xml?page=" + page),http,
        TwitterStatus.apply).parseXMLList("status").list
  }

  def getStatus(id: Long): TwitterStatus = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/show/" + id.toString() + ".xml"),http,
        TwitterStatus.apply).parseXMLElement()
  }
  
  def getFeatured(): List[TwitterUser] = {
    new Parser[TwitterUser](new URL(apiURL + "/statuses/featured.xml"),http,
        TwitterUser.apply).parseXMLList("user").list
  }

  /**
  * @param id the user id <i>or</i> user name to get favorites for
  */
  def getFavorites(id: String): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/favorites/" + urlEncode(id) + ".xml"),http,
        TwitterStatus.apply).parseXMLList("status").list
  }
  
  /**
  * @param id the user id <i>or</i> user name to get favorites for
  * @param page the results page to fetch.
  */
  def getFavorites(id: String, page: Int): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/favorites/" + urlEncode(id) + ".xml?page=" + 
        page.toString()),http,TwitterStatus.apply).parseXMLList("status").list
  }

  protected def urlEncode(value: String) = URLEncoder.encode(value, "UTF-8")

  protected def parse[T](urlPart: String, factory: (Node) => T, selectors: String*): XmlResult[T] = {
    new Parser[T](new URL(apiURL + urlPart), getHttp, factory).parseXMLList(selectors: _*)
  }
  
  protected def getHttp = http
}

