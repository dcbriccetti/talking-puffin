package org.talkingpuffin.ui

import java.util.regex.Pattern

/**
 * Extracts links from Twitter status
 */
object LinkExtractor {
  
  val urlCharClass = """[^'"()\[\]\s]"""
  val hyperlinkRegex = "(https?://" + urlCharClass + "+)"
  val userListRegex = """@((\w+)/""" + urlCharClass + "+)"
  val usernameRegex = """@(\w+)"""
  val usernameUrl = "http://twitter.com/$1"
  val userLinkUrl = "http://twitter.com/$1"
  val hyperlinkPattern = Pattern.compile(hyperlinkRegex)
  val userListPattern = Pattern.compile(userListRegex)
  val usernamePattern = Pattern.compile(usernameRegex)

  /**
   * Returns a list of tuples of (title, hyperlink) built from:
   * <ol>
   * <li>The in_reply_to_status_id
   * <li>@usernames
   * <li>Hyperlinks
   * </ol> 
   */
  def getLinks(text: String, inReplyToStatusId: Option[Long], users: Boolean, pages: Boolean, 
        lists: Boolean): List[(String,String)] = {
    var urls: List[(String,String)] = List()
    
    if (users) {
      getReplyToInfo(inReplyToStatusId, text) match {
        case Some((user, replyTo)) => 
          urls = urls ::: List(("Status " + replyTo + " of " + user, getStatusUrl(replyTo, user)))
        case _ =>
      }
  
      val m = usernamePattern.matcher(text)
      while (m.find) {
        val userName = m.group(1)
        val newItem = (userName, "http://twitter.com/" + userName)
        if (! urls.contains(newItem))
          urls = urls ::: List(newItem)
      }
    }
    
    if (pages) 
      urls = urls ::: getLinkItems(text)
    
    if (lists)
      urls = urls ::: getListItems(text)
    
    urls 
  }
  
  def getStatusUrl(replyTo: Long, replyToUser: String) = 
    "http://twitter.com/" + replyToUser + "/statuses/" + replyTo
  
  def createLinks(text: String) = LinkExtractor.replaceAtCode(
      LinkExtractor.wrapUserLinks(LinkExtractor.wrapUserLists(
        LinkExtractor.wrapLinks(text)))) 

  private val replyToUserRegex = (".*?" + usernameRegex + ".*").r
  
  /**
   * Returns the Twitter handle of the user whose @handle appears at the beginning of 
   * the tweet, or None, and the status ID.
   */
  def getReplyToInfo(inReplyToStatusId: Option[Long], text: String): Option[(String,Long)] = 
    inReplyToStatusId match {
      case Some(id) => text match {
        case replyToUserRegex(username) => Some((username, id))
        case _ => None
      }
      case _ => None
    }

  val withoutUserPattern = Pattern.compile("""^@\S+ (.*)""")
  
  /**
   * Returns a string with any @user at the beginning removed.
   */
  def getWithoutUser(text: String): String = {
    val m = withoutUserPattern.matcher(text)
    if (m.find) m.group(1) else text
  }

  private val atCode = "##xX##"
  
  private def wrapUserLinks(text: String) = text.replaceAll(LinkExtractor.usernameRegex, 
      "<a href='" + LinkExtractor.usernameUrl + "'>" + atCode + "$1</a>")

  private def wrapLinks(text: String): String = {
    val items = getLinkItems(text) // Example: ("url", "url")
    var newText = text
    items.foreach(item => {
      newText = newText.replace(item._1, "<a href='" + item._1 + "'>" + item._1 + "</a>")
    })
    newText
  }
  
  private def wrapUserLists(text: String): String = {
    val items = getListItems(text) // Example: ("dave/scala", "http://twitter.com/dave/scala")
    var newText = text
    items.foreach(item => {
      newText = newText.replace("@" + item._1, 
        "<a href='" + item._2 + "'>" + atCode + item._1 + "</a>")
    })
    newText
  }
  
  private def replaceAtCode(text: String) = text.replaceAll(atCode, "@")

  private val trailingPunctRe = "^(.*?)[,.\"”]?$".r

  private def stripTrailingPunctuation(text: String) = text match {
    case trailingPunctRe(result) => result
  }

  /**
   * Finds hyperlinks.
   */
  private def getLinkItems(text: String): List[Tuple2[String,String]] = {
    var urls: List[(String,String)] = List()
    val m = hyperlinkPattern.matcher(text)
    while (m.find) {
      val item = stripTrailingPunctuation(m.group(1))
      val newItem = (item, item)
      if (! urls.contains(newItem))
        urls = urls ::: List(newItem)
    }
    urls
  }

  /**
   * Finds lists, like @dcbriccetti/scala.
   */
  private def getListItems(text: String): List[Tuple2[String,String]] = {
    var urls: List[(String,String)] = List()
    val m = userListPattern.matcher(text)
    while (m.find) {
      val item = stripTrailingPunctuation(m.group(1))
      val newItem = (item, "http://twitter.com/" + item)
      if (! urls.contains(newItem))
        urls = urls ::: List(newItem)
    }
    urls
  }
} 