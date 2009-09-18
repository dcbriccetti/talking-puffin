package org.talkingpuffin.twitter

import java.util.Date
import scala.xml._
import org.apache.log4j._
import org.joda.time._
import org.joda.time.format._

/**
* Represents a twitter status update.
* This object is represented in several API calls.
*/
class TwitterStatus() extends Validated{
  var text: String = null
  var user: TwitterUser = null
  var id: Long = 0L
  var createdAt: DateTime = null
  var source: String = null
  var truncated: Boolean = false
  var inReplyToStatusId: Long = 0L
  var inReplyToUserId: Long = 0L
  var favorited: Boolean = false
  var retweeted: Retweet = null

  def isValid() = {
    text != null && user != null
  }

  override def equals(obj: Any) = {
    if(obj.isInstanceOf[TwitterStatus]){
      obj.asInstanceOf[TwitterStatus].id == id
    }else{
      false
    }
  }

  override def hashCode() = id.hashCode

}

class Retweet extends TwitterUser{
  var retweetedAt: DateTime = null
}

/**
*
* The TwitterStatus object is used to construct TwitterStatus instances from XML fragments
* The only method available is apply, which allows you to use the object as follows
* <tt><pre>
* val xml = getXML()
* val status = TwitterStatus(xml)
* </pre></tt>
*/ 
object TwitterStatus{
  val logger = Logger.getLogger("twitter")
  val fmt = DateTimeFormats.fmt1

  /**
  * construct a TwitterStatus object from an XML node
  */
  def apply(n: Node):TwitterStatus = {
    val status = new TwitterStatus
    n.child foreach {(sub) =>
      try {
        sub match {
          case <id>{Text(text)}</id> => status.id = java.lang.Long.parseLong(text)
          case <created_at>{Text(text)}</created_at> => status.createdAt = fmt.parseDateTime(text)
          case <text>{Text(text)}</text> => status.text = text
          case <source>{Text(text)}</source> => status.source = text
          case <truncated>{Text(text)}</truncated> => status.truncated = java.lang.Boolean.valueOf(text).booleanValue
          case <in_reply_to_status_id/> => Nil
          case <in_reply_to_status_id>{Text(text)}</in_reply_to_status_id> => status.inReplyToStatusId = java.lang.Long.parseLong(text)
          case <in_reply_to_user_id/> => Nil
          case <in_reply_to_user_id>{Text(text)}</in_reply_to_user_id> => status.inReplyToUserId = java.lang.Long.parseLong(text)
          case <favorited>{Text(text)}</favorited> => status.favorited = java.lang.Boolean.valueOf(text).booleanValue
          case <user>{ _* }</user> => status.user = TwitterUser(sub)
          case <retweet_details>{ _* }</retweet_details> => status.retweeted = Retweet(sub)
          case _ => Nil
        }
      } catch {
        case e: NumberFormatException => logger.error(e + " " + sub + " " + n)
      }
    }
    return status
  }
}

/**
*
* The Retweet object is used to construct Retweet instances from XML fragments
* The only method available is apply, which allows you to use the object as follows
* <tt><pre>
* val xml = getXML()
* val retweet = Retweet(xml)
* </pre></tt>
*/
object Retweet{
  val logger = Logger.getLogger("twitter")
  val fmt = DateTimeFormats.fmt1

  /**
  * construct a TwitterStatus object from an XML node
  */
  def apply(n: Node):Retweet = {
    val retweet = new Retweet()
    n.child foreach {(sub) =>
      try {
        sub match {
          case <retweeting_user>{ _* }</retweeting_user> => {
            sub.child foreach {(child) =>
              child match {
                case <id>{Text(text)}</id> => retweet.id = Integer.parseInt(text)
                case <name>{Text(text)}</name> => retweet.name = text
                case <screen_name>{Text(text)}</screen_name> => retweet.screenName = text
                case <location/> => Nil
                case <location>{Text(text)}</location> => retweet.location = text
                case <description/> => Nil
                case <description>{Text(text)}</description> => retweet.description = text
                case <profile_image_url>{Text(text)}</profile_image_url> => retweet.profileImageURL = text
                case <url/> => Nil
                case <url>{Text(text)}</url> => retweet.url = text
                case <protected>{Text(text)}</protected> => retweet.isProtected = java.lang.Boolean.valueOf(text).booleanValue
                case <followers_count>{Text(text)}</followers_count> => retweet.followersCount = Integer.parseInt(text)
                case <friends_count>{Text(text)}</friends_count> => retweet.friendsCount = Integer.parseInt(text)
                case <status>{ _* }</status> => retweet.status = Some(TwitterStatus(sub))
                case _ => Nil
              }
            }
          }
          case <retweeted_at>{Text(text)}</retweeted_at> => retweet.retweetedAt = fmt.parseDateTime(text)
          case _ => Nil
        }
      } catch {
        case e: NumberFormatException => logger.error(e + " " + sub + " " + n)
        case e: IllegalArgumentException => logger.error(e + " " + sub + " " + n)
      }
    }
    return retweet
  }
}