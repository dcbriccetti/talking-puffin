package org.talkingpuffin.twitter

import scala.xml._
import org.apache.log4j._
import org.joda.time._

/**
* Represents a twitter status update.
* This object is represented in several API calls.
*/
class TwitterStatus() extends Validated {
  private val logger = Logger.getLogger("twitter")
  private var _text: String = null
  def text = retweetOrTweet._text
  def text_=(t: String) = _text = t
  var user: TwitterUser = null
  var id: Long = 0L
  var createdAt: DateTime = null
  @Deprecated var source: String = ""
  var sourceName: String = ""
  var sourceUrl: Option[String] = None
  var truncated: Boolean = false
  var inReplyToStatusId: Option[Long] = None
  var inReplyToUserId: Option[Long] = None
  var inReplyToScreenName: Option[String] = None
  var favorited: Boolean = false
  var retweet: Option[TwitterRetweet] = None
  var location: Option[(Double, Double)] = None

  private val fmt = DateTimeFormats.fmt1

  def this(n: Node) {
    this()
    assignFromNode(n)
  }
  
  private[twitter] def assignFromNode(n: Node) {
    n.child foreach {(sub) =>
      try {
        sub match {
          case <id>{Text(t)}</id> => id = t.toLong
          case <created_at>{Text(t)}</created_at> => createdAt = fmt.parseDateTime(t)
          case <text>{Text(t)}</text> => this._text = t
          case <source>{Text(t)}</source> => extractSource(t)
          case <truncated>{Text(t)}</truncated> => truncated = java.lang.Boolean.valueOf(t).booleanValue
          case <in_reply_to_status_id>{Text(t)}</in_reply_to_status_id> => inReplyToStatusId = 
              Some(t.toLong)
          case <in_reply_to_user_id>{Text(t)}</in_reply_to_user_id> => inReplyToUserId = Some(t.toLong)
          case <in_reply_to_screen_name>{Text(t)}</in_reply_to_screen_name> => 
              inReplyToScreenName = Some(t)
          case <favorited>{Text(t)}</favorited> => favorited = java.lang.Boolean.valueOf(t).booleanValue
          case <user>{ _* }</user> => addUser(TwitterUser(sub))
          case <retweeted_status>{ _* }</retweeted_status> => retweet = Some(TwitterRetweet(sub))
          case <geo>{ _* }</geo> => extractLocation(sub)
          case _ => Nil
        }
      } catch {
        case e: NumberFormatException => logger.error(e + " " + sub + " " + n)
      }
    }
  }
  
  def isValid() = {
    text != null && user != null
  }
  
  def retweetOrTweet = if (retweet.isDefined) retweet.get else this
  
  protected def addUser(user: TwitterUser) = this.user = user

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
  /**
  * construct a TwitterStatus object from an XML node
  */
  
  def apply(n: Node): TwitterStatus = new TwitterStatus(n)
}

class TwitterRetweet extends TwitterStatus {

  def this(n: Node) = {
    this()
    assignFromNode(n)
  }
}

object TwitterRetweet extends TwitterStatus {
  def apply(n: Node): TwitterRetweet = new TwitterRetweet(n)
}
