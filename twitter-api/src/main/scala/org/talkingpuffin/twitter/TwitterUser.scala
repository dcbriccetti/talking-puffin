package org.talkingpuffin.twitter

import java.util.Date
import scala.xml._
import org.apache.log4j._

/**
* 
* Represents a twitter user XML fragment.
* This object is represented in several API calls
*/
class TwitterUser() extends Validated{
  /** the screen name of this user */
  var screenName: String = null
  /** the twitter user id of this user */
  var id: Int = 0
  /** the natural name of this user */
  var name: String = null
  /** the location of this user (if specified) */
  var location: String = null
  /** the user's description (if specified) */
  var description: String = null
  /** the user's profile image URL (if specified) */
  var profileImageURL: String = null
  /** the user's personal site URL (if specified) */
  var url: String = null
  /** a flag indicating whether or not updates from this user are protected */
  var isProtected: boolean = false
  /** the number of people who follow this user */
  var followersCount: Int = 0
  /** this user's last status, if available */
  var status: TwitterStatus = null
  
  def isValid() = {
    screenName != null
  }
  
}

/**
*
* The Twitter object is used to construct TwitterUser instances from XML fragments
* The only method available is apply, which allows you to use the object as follows
* <tt><pre>
* val xml = getXML()
* val user = TwitterUser(xml)
* </pre></tt>
*/ 
object TwitterUser{
  val logger = Logger.getLogger("twitter")

  /**
  * construct a TwitterUser object from an XML node
  */
  def apply(n: Node):TwitterUser = {
    val user = new TwitterUser
    n.child foreach {(sub) => 
      sub match{
        case <id>{Text(text)}</id> => user.id = Integer.parseInt(text)
        case <name>{Text(text)}</name> => user.name = text
        case <screen_name>{Text(text)}</screen_name> => user.screenName = text
        case <location/> => Nil
        case <location>{Text(text)}</location> => user.location = text
        case <description/> => Nil
        case <description>{Text(text)}</description> => user.description = text
        case <profile_image_url>{Text(text)}</profile_image_url> => user.profileImageURL = text
        case <url/> => Nil
        case <url>{Text(text)}</url> => user.url = text
        case <protected>{Text(text)}</protected> => user.isProtected = java.lang.Boolean.valueOf(text).booleanValue
        case <followers_count>{Text(text)}</followers_count> => user.followersCount = Integer.parseInt(text)
        case <status>{ _* }</status> => user.status = TwitterStatus(sub)
        case _ => Nil
      }
    }
    return user
  }
  
}
