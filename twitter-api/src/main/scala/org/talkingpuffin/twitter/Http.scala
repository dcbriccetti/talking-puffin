package org.talkingpuffin.twitter

import scala.xml.{Node, XML}
import swing.Publisher
import java.net.{URL, HttpURLConnection, URLEncoder}
import java.io.{DataOutputStream, BufferedReader, InputStreamReader}
import org.apache.commons.codec.binary.Base64
import org.apache.log4j.Logger
import swing.event.Event

/**
* Handles HTTP requests.
*/
class Http(user: Option[String], password: Option[String]) extends Publisher {
  private val log = Logger.getLogger("Http")

  /** the encoded authentication string.  This is null if user or password is null. */
  private val encoding = if(user.isDefined && password.isDefined) 
    Some(new String(Base64.encodeBase64((user.get + ":" + password.get).getBytes())))
  else 
    None
  
  /**
  * Fetch an XML document from the given URL
  */
  def get(url: URL): Node = Http.run {
    logAction("GET", url)
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    setAuth(conn)
    getXML(conn)
  }

  def delete(url: URL) = Http.run {
    logAction("DELETE", url)
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    setAuth(conn)
    conn.setRequestMethod("DELETE")
    getXML(conn)
  }
  /*
  * post to the specified URL with the given params, return an XML node built from the response
  * @param url the URL to post to
  * @param params a List of String tuples, the first entry being the param, the second being the value
  */
  def post(url: URL, params: List[(String,String)]): Node = Http.run {
    logAction("POST", url, params.map(kv => kv._1.trim + "=" + kv._2.trim).mkString(" "))
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    setAuth(conn)
    conn.setDoInput(true)
    conn.setRequestMethod("POST")
    val content = buildParams(params)

    if(content != null){
      conn.setUseCaches(false)
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
      conn.setDoOutput(true)
      val printout = new DataOutputStream(conn.getOutputStream())
      if(content != null){
        printout.writeBytes (content)
        printout.flush ()
        printout.close ()
      }
    }
    getXML(conn)
  }
  
  def post(url: URL): Node = post(url, Nil)
  
  private def actionAndUrl(action: String, url: URL) = user.getOrElse("") + " " + action + " " + url
  private def logAction(action: String, url: URL) = log.debug(actionAndUrl(action, url))
  private def logAction(action: String, url: URL, params: String) = 
      log.debug(actionAndUrl(action, url) + " " + params)

  private def setAuth(conn: HttpURLConnection) {
    if (encoding.isDefined) {
      conn.setRequestProperty ("Authorization", "Basic " + encoding.get)
    }
  }

  /*
  * take an opened (and posted to, if applicable) connection, read the response code, and take appropriate action.
  * If the response code is 200, return an XML node built on the response.
  * If the response code is anything else, throw a new TwitterException based on the code. 
  * This path also reads from conn.getErrorStream() to populate the twitterMessage field
  * in the thrown exception.
  */
  private def getXML(conn: HttpURLConnection): Node = {
    val response = conn.getResponseCode()
    response match {
      case 200 => {
        publishRateLimitInfo(conn)
        XML.load(conn.getInputStream())
      }
      case _ => throw TwitterException({
          var errMsg = ""
          val reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))
          var line = reader.readLine()
          while(line != null){
            errMsg += line
            line = reader.readLine()
          }
          errMsg  
        },response)
    }
  }
  
  private def publishRateLimitInfo(conn: HttpURLConnection) {
    val rl = List("X-RateLimit-Remaining", "X-RateLimit-Limit", "X-RateLimit-Reset").
        map(conn.getHeaderField).filter(_ != null).map(_.toInt)

    if (rl.length == 3) {
      publish(RateLimitStatusEvent(new TwitterRateLimitStatus {
        remainingHits      = rl(0)
        hourlyLimit        = rl(1)
        resetTimeInSeconds = rl(2)
      }))
    }
  }

  private def buildParams(params: List[(String,String)]): String = {
    params.map(pv => pv._1 + "=" + URLEncoder.encode(pv._2, "UTF-8")).mkString("&")
  }
}

object Http {
  val retryAfterFailureDelays = List(0, 250, 2000, 5000, 10000, 30000, 60000)
  val runner = new HttpRunner(retryAfterFailureDelays)
  def run[T](f: => T) = runner.run(f)
}

case class RateLimitStatusEvent(status: TwitterRateLimitStatus) extends Event
