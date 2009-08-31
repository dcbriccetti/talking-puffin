package org.talkingpuffin.twitter

import scala.xml._
import org.apache.log4j._

/**
* 
* Represents a twitter user ID XML fragment.
* This object is represented in several API calls
*/
case class TwitterUserId(val id: Long) 

/**
*
* The Twitter object is used to construct TwitterUser instances from XML fragments
* The only method available is apply, which allows you to use the object as follows
* <tt><pre>
* val xml = getXML()
* val id = TwitterUserId(xml)
* </pre></tt>
*/ 
object TwitterUserId{
  /**
  * construct a TwitterUser object from an XML node
  */
  def apply(n: Node):TwitterUserId = new TwitterUserId(Integer.parseInt(n.text))
}
