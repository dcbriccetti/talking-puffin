
package org.talkingpuffin.twitter

import java.util.Date
import scala.xml._
import org.apache.log4j._
import org.joda.time._
import org.joda.time.format._
import java.util.Locale
/**
* Represents a twitter direct message.
*/

class TwitterMessage() extends Validated{
  var text: String = ""
  var senderScreenName: String = ""
  var id: Long = 0
  var senderId: Long = 0
  var recipientId: Long = 0
  var recipientScreenName: String = ""
  var createdAt: DateTime = null
  var sender: TwitterUser = null
  var recipient: TwitterUser = null
  
  def isValid() = {
    text != null && senderScreenName != null
  }

  override def equals(obj: Any):Boolean = {
    if(obj.isInstanceOf[TwitterMessage]){
      obj.asInstanceOf[TwitterMessage].id == id
    }else{
      false
    }
  }

  override def hashCode() = id.hashCode

}

/**
*
* The TwitterMessage object is used to construct TwitterMessage instances from XML fragments
* The only method available is apply, which allows you to use the object as follows
* <tt><pre>
* val xml = getXML()
* val message = TwitterMessage(xml)
* </pre></tt>
*/ 

object TwitterMessage{
  val logger = Logger.getLogger("twitter")
  val fmt = DateTimeFormats.fmt1

  /**
  * construct a TwitterMessage object from an XML node
  */
  def apply(n: Node):TwitterMessage = {
    val message = new TwitterMessage
    n.child foreach {(sub) => 
      sub match{
        case <id>{Text(text)}</id> => message.id = java.lang.Long.parseLong(text)
        case <text>{Text(text)}</text> => message.text = text
        case <sender_id>{Text(text)}</sender_id> => message.senderId = java.lang.Long.parseLong(text)
        case <recipient_id>{Text(text)}</recipient_id> => message.recipientId = java.lang.Long.parseLong(text)
        case <sender_screen_name>{Text(text)}</sender_screen_name> => message.senderScreenName = text
        case <recipient_screen_name>{Text(text)}</recipient_screen_name> => message.recipientScreenName = text
        case <created_at>{Text(text)}</created_at> => message.createdAt = fmt.parseDateTime(text)
        case <sender>{ _* }</sender> => message.sender = TwitterUser(sub)
        case <recipient>{ _* }</recipient> => message.recipient = TwitterUser(sub)
        case _ => Nil
      }
    }
    return message
  }
}
