package org.talkingpuffin.twitter

import java.net.{URLConnection,URL,HttpURLConnection,URLEncoder}
import scala.xml._
import org.apache.commons.codec.binary.Base64
import java.io.{DataOutputStream,DataInputStream,IOException,BufferedReader,InputStreamReader}

// end session http://twitter.com/account/end_session
// help http://twitter.com/help/test.format 
// downtime http://twitter.com/help/downtime_schedule.format

/**
* Utility class that connects to a URL and returns an XML object.
* If both a user and password are specified, HTTP Basic authentication is used in the connection.
* This class currently does no error checking, but in the future will also check against valid Twitter error codes
*/
class XMLFetcher(user: String, password: String){
  
  /** the encoded authentication string.  This is null if user or password is null. */
  val encoding = if(user != null && password != null) new String(Base64.encodeBase64((user + ":" + password).getBytes())) else null
  
  /**
  * Fetch an XML document from the given URL
  */
  def doGet(url: URL) :Node = {
    val conn: HttpURLConnection = (url.openConnection).asInstanceOf[HttpURLConnection]
    if(encoding != null){
      conn.setRequestProperty ("Authorization", "Basic " + encoding);
    }
    getXML(conn)
  }
  
  /*
  * post to the specified URL with the given params, return an XML node built from the response
  * @param url the URL to post to
  * @param params a List of String tuples, the first entry being the param, the second being the value
  */
  def doPost(url: URL, params: List[(String,String)]) :Node = {
    val conn: HttpURLConnection = (url.openConnection).asInstanceOf[HttpURLConnection]
    if(encoding != null){
      conn.setRequestProperty ("Authorization", "Basic " + encoding);
    }
    conn.setDoInput(true)
    conn.setRequestMethod("POST")
    val content = buildParams(params)

    if(content != null){
      conn.setUseCaches(false)
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
  
  /*
  * take an opened (and posted to, if applicable) connection, read the response code, and take appropriate action.
  * If the response code is 200, return an XML node built on the response.
  * If the response code is anything else, throw a new TwitterException based on the code. 
  * This path also reads from conn.getErrorStream() to populate the twitterMessage field
  * in the thrown exception.
  */
  def getXML(conn: HttpURLConnection) :Node = {
    val response = conn.getResponseCode()
    response match {
      case 200 => XML.load(conn.getInputStream())
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

  def buildParams(params: List[(String,String)]) :String = {
    params match {
      case Nil => null
      case (param,value) :: rest => {
        val end = buildParams(rest)
        if(end != null)
          return param + "=" + URLEncoder.encode(value) + "&" + end
        else
          return param + "=" + URLEncoder.encode(value)
      }
    }
  }
}

/**
* Provides generalized processing of a Twitter XML response.
* Under the covers this uses the specified fetcher and URL to get an XML document, 
* and then processes the document with the specified factory, building a list (or single instance) 
* of whatever the factory returns.
*/
class Parser[T](url: URL, fetcher :XMLFetcher, factory: (Node) => T){  
  /**
  * build a list of instances of T from the returned XML document
  */
  def parseXMLList(selector: String): List[T] = {
    var list = List[T]()
    fetcher.doGet(url)\selector foreach {(entry) =>
      list = factory(entry) :: list
    }
    return list
  }
  /**
  * build a single instance of T from the returned XML document
  */
  def parseXMLElement(): T = {
    return factory(fetcher.doGet(url))
  }
}

trait Validated{
  def isValid(): Boolean
}
