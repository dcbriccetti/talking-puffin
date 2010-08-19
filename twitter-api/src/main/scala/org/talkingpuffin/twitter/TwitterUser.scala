package org.talkingpuffin.twitter

import scala.xml._
import org.apache.log4j._

/**
* 
* Represents a twitter user XML fragment.
* This object is represented in several API calls
*/
class TwitterUser() extends Validated{
  /** the screen name of this user */
  var screenName = ""
  /** the twitter user id of this user */
  var id = 0L
  /** the natural name of this user */
  var name = ""
  /** the location of this user (if specified) */
  var location = ""
  /** the user's description (if specified) */
  var description = ""
  /** the user's profile image URL (if specified) */
  var profileImageURL = ""
  /** the user's personal site URL (if specified) */
  var url = ""
  /** a flag indicating whether or not updates from this user are protected */
  var isProtected = false
  /** the number of people who follow this user */
  var followersCount = 0
  /** the number of people this user follows */
  var friendsCount = 0
  /** this user's last status, if available */
  var status: Option[TwitterStatus] = None
  
  def isValid() = {
    screenName != null
  }

  override def equals(obj: Any) = {
    if(obj.isInstanceOf[TwitterUser]){
      obj.asInstanceOf[TwitterUser].id == id
    }else{
      false
    }
  }

  override def hashCode() = id
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
        case <name>{Text(text)}</name> => user.name = text.trim // Trimming because of a user with a NL (0x0a)
        case <screen_name>{Text(text)}</screen_name> => user.screenName = text.trim
        case <location/> => Nil
        case <location>{Text(text)}</location> => user.location = text
        case <description/> => Nil
        case <description>{Text(text)}</description> => user.description = text
        case <profile_image_url>{Text(text)}</profile_image_url> => user.profileImageURL = text
        case <url/> => Nil
        case <url>{Text(text)}</url> => user.url = text
        case <protected>{Text(text)}</protected> => user.isProtected = java.lang.Boolean.valueOf(text).booleanValue
        case <followers_count>{Text(text)}</followers_count> => user.followersCount = Integer.parseInt(text)
        case <friends_count>{Text(text)}</friends_count> => user.friendsCount = Integer.parseInt(text)
        case <status>{ _* }</status> => user.status = Some(TwitterStatus(sub))
        case _ => Nil
      }
    }
    return user
  }
  
}
