package org.talkingpuffin.twitter

import scala.xml._
import org.apache.log4j._
import org.joda.time._

/**
* Represents a twitter status update.
* This object is represented in several API calls.
*/
class TwitterStatus() extends Validated{
  var text: String = null
  var user: TwitterUser = null
  var id: Long = 0L
  var createdAt: DateTime = null
  @Deprecated var source: String = ""
  var sourceName: String = ""
  var sourceUrl: Option[String] = None
  var truncated: Boolean = false
  var inReplyToStatusId: Option[Long] = None
  var inReplyToUserId: Option[Long] = None
  var favorited: Boolean = false
  var retweeted: Option[TwitterStatus] = None
  var location: Option[(Double, Double)] = None

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

  /**
   * From the “source” string, which oddly may contain either a simple string, such as “web,”
   * or an anchor tag with an href and a source name, extract:
   * <ol>
   * <li>the entire contents into {@link #source}, for backward compatibility
   * <li>a URL, if found, into {@link #sourceUrl}
   * <li>the source name into {@link #sourceName}
   * </ol>
   * 
   */
  private def extractSource(text: String) {
    source = text
    // XML.loadString might have been used instead of this regex, but it throws exceptions because of the contents
    val anchorRegex = """<a.*href=["'](.*?)["'].*?>(.*?)</a>""".r
    val fields = text match {
      case anchorRegex(u,s) => (Some(u), s)
      case _ => (None, text) 
    }
    sourceUrl = fields._1
    sourceName = fields._2
  }

  /**
   * Location looks like this:
   * <pre>
   * &lt;geo>
   *    &lt;georss:Point>37.780300 -122.396900&lt;/georss:Point>
   * &lt;/geo>
   * </pre>
   * 
   * <code>node</code> contains <code>geo</code>
   */
  private def extractLocation(node: NodeSeq) {
    node \ "point" match {
      case p if p.length > 0 => 
        val loc = p.text.split(" ")
        if (loc.length == 2)
          location = Some((loc(0).toDouble, loc(1).toDouble)) 
      case _ =>
    }
  }
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
          case <id>{Text(text)}</id> => status.id = text.toLong
          case <created_at>{Text(text)}</created_at> => status.createdAt = fmt.parseDateTime(text)
          case <text>{Text(text)}</text> => status.text = text
          case <source>{Text(text)}</source> => status.extractSource(text)
          case <truncated>{Text(text)}</truncated> => status.truncated = java.lang.Boolean.valueOf(text).booleanValue
          case <in_reply_to_status_id>{Text(text)}</in_reply_to_status_id> => status.inReplyToStatusId = 
              Some(text.toLong)
          case <in_reply_to_user_id>{Text(text)}</in_reply_to_user_id> => status.inReplyToUserId = 
              Some(text.toLong)
          case <favorited>{Text(text)}</favorited> => status.favorited = java.lang.Boolean.valueOf(text).booleanValue
          case <user>{ _* }</user> => status.user = TwitterUser(sub)
          case <retweeted_status>{ _* }</retweeted_status> => status.retweeted = Some(TwitterStatus(sub))
          case <geo>{ _* }</geo> => status.extractLocation(sub)
          case _ => Nil
        }
      } catch {
        case e: NumberFormatException => logger.error(e + " " + sub + " " + n)
      }
    }
    status
  }
  
}

