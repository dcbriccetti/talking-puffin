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
  val fmt = DateTimeFormat.forPattern("EE MMM dd HH:mm:ss Z yyyy")

  /**
  * construct a TwitterStatus object from an XML node
  */
  def apply(n: Node):TwitterStatus = {
    val status = new TwitterStatus
    n.child foreach {(sub) => 
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
        case _ => Nil
      }      
    }
    return status
  }
}