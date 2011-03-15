package org.talkingpuffin.util

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

  case class Link(title: String, link: String)
  type Links = List[Link]

  /**
   * Returns Links built from:
   * <ol>
   * <li>The in_reply_to_status_id
   * <li>@usernames
   * <li>Hyperlinks
   * <li>Twitter lists
   * </ol>
   */
  def getLinks(text: String, inReplyToStatusId: Option[Long], users: Boolean = false, links: Boolean = false,
        lists: Boolean = false): Links = {
    var urls = List[Link]()
    
    if (users) {
      getReplyToInfo(inReplyToStatusId, text) match {
        case Some((user, replyTo)) => 
          urls = urls ::: List(Link("Status " + replyTo + " of " + user, getStatusUrl(replyTo, user)))
        case _ =>
      }
  
      val m = usernamePattern.matcher(text)
      while (m.find) {
        val userName = m.group(1)
        val newItem = Link(userName, "http://twitter.com/" + userName)
        if (! urls.contains(newItem))
          urls = urls ::: List(newItem)
      }
    }
    
    if (links)
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
      newText = newText.replace(item.title, "<a href='" + item.title + "'>" + item.title + "</a>")
    })
    newText
  }
  
  private def wrapUserLists(text: String): String = {
    val items = getListItems(text) // Example: ("dave/scala", "http://twitter.com/dave/scala")
    var newText = text
    items.foreach(item => {
      newText = newText.replace("@" + item.title,
        "<a href='" + item.link + "'>" + atCode + item.title + "</a>")
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
  private def getLinkItems(text: String): Links = {
    var urls = List[Link]()
    val m = hyperlinkPattern.matcher(text)
    while (m.find) {
      val item = stripTrailingPunctuation(m.group(1))
      val newItem = Link(item, item)
      if (! urls.contains(newItem))
        urls = urls ::: List(newItem)
    }
    urls
  }

  /**
   * Finds lists, like @dcbriccetti/scala.
   */
  private def getListItems(text: String): Links = {
    var urls = List[Link]()
    val m = userListPattern.matcher(text)
    while (m.find) {
      val item = stripTrailingPunctuation(m.group(1))
      val newItem = Link(item, "http://twitter.com/" + item)
      if (! urls.contains(newItem))
        urls = urls ::: List(newItem)
    }
    urls
  }
} 