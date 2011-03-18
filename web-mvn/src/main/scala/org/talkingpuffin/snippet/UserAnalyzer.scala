package org.talkingpuffin.snippet

import java.text.NumberFormat
import scala.collection.JavaConversions._
import xml.{Elem, NodeSeq, Text}
import twitter4j.TwitterException
import net.liftweb.widgets.flot.Flot
import net.liftweb.common.Full
import net.liftweb.util.Helpers._
import net.liftweb.http.{RequestVar, SHtml, S}
import net.liftweb.http.js.JsCmds._
import org.talkingpuffin.snippet.GeneralUserInfo.ScreenNames
import org.talkingpuffin.apix.PartitionedTweets
import org.talkingpuffin.apix.RichStatus._
import org.talkingpuffin.user.UserAnalysis
import org.talkingpuffin.snippet.LineCollector.InfoLine
import org.talkingpuffin.util._
import org.apache.commons.lang.StringEscapeUtils

/**
 * Snippets for user analysis
 */
class UserAnalyzer extends RedirectorWithRequestParms with Loggable {

  private object screenName        extends RequestVar[Option[String]](None)
  private object partitionedTweets extends RequestVar[Option[PartitionedTweets]](None)

  def nameForm(content: NodeSeq) = {
    SessionState.twitter.is match {
      case Some(tw) =>
        S.param("user") match {
          case Full(u) => setScreenName(u)
          case _ =>
            if (! screenName.is.isDefined) {
              screenName(Some(tw.getScreenName))
            }
        }
        bind("nameForm", content,
          "name"   -> SHtml.text(screenName.is.getOrElse(""), setScreenName(_)),
          "submit" -> SHtml.submit("Analyze", () => {}))
      case _ => S.redirectTo("index" + makeUserParm)
    }
  }

  def headForGraph (xhtml: NodeSeq) = Flot.renderHead()

  def followUnfollow = {
    val tw = SessionState.twitter.is.get

    def check(screenName: String)(on: Boolean) = {
      try {
        if (on) tw.createFriendship(screenName) else tw.destroyFriendship(screenName)
        S.notice(if (on) "Followed" else "Unfollowed")
      } catch {
        case te: TwitterException =>
          S.warning(te.getMessage)
          Nil
      }
      Noop
    }
    
    screenName.is match {
      case Some(screenName) if screenName != tw.getScreenName =>
        try {
          val rel = tw.showFriendship(tw.getScreenName, screenName)
          List(SHtml.ajaxCheckbox(rel.isTargetFollowedBySource, check(screenName)), Text("following"))
        } catch {
          case te: TwitterException =>
            // Error will appear to user from elsewhere
            Nil
        }
      case _ => Nil
    }
  }

  def generalInfo = {
    val tw = SessionState.twitter.is.get
    val emptyRows = List[Elem]()
    val emptyImage = Nil

    val (rows, image) = screenName.is match {
      case Some(screenName) =>
        info(tw.getScreenName + " is analyzing " + screenName)
        try {
          val pt = PartitionedTweets(tw, screenName)
          partitionedTweets(Some(pt))
          val uinfo = tw.lookupUsers(Array(screenName)).get(0)
          val (gnlLines, ua) = if (pt.tweets.isEmpty)
            (List(InfoLine("Number of tweets", "0")), None)
          else {
            val uan = UserAnalysis(pt)
            (GeneralUserInfo.create(uinfo, screenName, pt, uan), Some(uan))
          }
          SessionState.userAnalysis(ua)
          val image = <img src={Picture.getFullSizeUrl(uinfo.getProfileImageURL.toString)}
                alt="Profile Image"/>
          (makeGnlRows(gnlLines), image)
        } catch {
          case te: TwitterException =>
            val failureMsg = "Unable to fetch data for that user (may be temporary Twitter problem). Status code: " +
              te.getStatusCode
            info(failureMsg)
            S.warning(failureMsg)
            (emptyRows, emptyImage)
        }
      case _ => (emptyRows, emptyImage)
    }
    "id=row" #> rows &
    "id=image" #> image
  }

  def generalScreenNameFreq = fillFreqs(GeneralUserInfo.createScreenNameFreq)

  def generalWordFreq = fillFreqs(GeneralUserInfo.createWordFreq)

  def generalHashtagFreq = fillFreqs(GeneralUserInfo.createHashtagFreq)

  def links = "id=item" #> (SessionState.userAnalysis.is match {
      case Some(ua) => {
        val guiLinks = GeneralUserInfo.links(ua)
        val start = System.currentTimeMillis
        val spans = Parallelizer.run(30, guiLinks, expandLink).map(expanded =>
          GeneralUserInfo.Link(expanded)).sortBy(_.toString.toLowerCase).map(_.url).map(url =>
          <span><a href={url}>{GeneralUserInfo.Link.stripFront(url)}</a><br/></span>
        )
        info("Processed " + guiLinks.size + " links in " +
          NumberFormat.getInstance.format(System.currentTimeMillis - start) + " ms")
        spans
      }
      case _ => List[Elem]()
    })

  def tweets = {
    val rows: List[Elem] = partitionedTweets.is match {
      case Some(pt) => pt.tweets.toList.map(tw =>
        <tr>
          <td>{xml.Unparsed(TimeUtil2.formatAge(tw.createdAt, false).replace(" ", "&nbsp;"))}</td>
          <td>{tw.retweet match {
            case Some(status) => hyperlinkScreenName(status.getUser.getScreenName)
            case None => Text("")
          }}</td>
          <td>{tw.inReplyToScreenName match {
            case Some(screenName) => hyperlinkScreenName(screenName)
            case None => Text("")
          }
          }</td>
          <td>{xml.Unparsed(LinkExtractor.createLinks(StringEscapeUtils.escapeHtml(
            LinkExtractor.getWithoutUser(tw.text))).replaceAll("\n", "<br/>"))}</td>
        </tr>)
      case _ => List[Elem]()
    }
    "id=tweetRow" #> rows
  }

  def plot(xhtml: NodeSeq): NodeSeq =
    if (tweetsAvailable)
      Script(UserTimelinePlotRenderer.render(partitionedTweets.is.get, screenName.is.get))
    else
      Nil

  private def setScreenName(sn: String) {
    screenName(sn.trim match {
      case screenName if screenName.length > 0 => Some(screenName)
      case _ => None
    })
  }

  private def hyperlinkScreenName(screenName: String) =
    <a href={Links.linkForAnalyze(screenName, hostName = S.hostName)}>{screenName}</a>

  private def createNameLinks(names: List[String]) =
    names.flatMap(name =>
      <span class="screenName"><a href={Links.linkForAnalyze(name, hostName = S.hostName)}>{name}</a></span> ++ Text(", ")
    ).dropRight(1)

  private def makeGnlRows(gnlLines: scala.List[InfoLine]) =
    gnlLines.map(il =>
      <tr>
        <td class="gnlInfoHead">{il.heading}</td>
        <td class="gnlInfoVal">
          {il.value match {
          case ScreenNames(sn) => createNameLinks(sn)
          case s => s
        }}
        </td>
      </tr>)

  private def fillFreqs(freqProvider: (UserAnalysis) => List[InfoLine]) = {
    "id=row" #> (SessionState.userAnalysis.is match {
      case Some(ua) => makeGnlRows(freqProvider(ua))
      case _ => List[Elem]()
    })
  }

  private def expandLink(l: GeneralUserInfo.Link) = {
    val url = l.url.toString
    try {UrlExpander.expand(url)} catch {case _ => url}
  }

  private def tweetsAvailable = // partitionedTweets.is is not defined if error fetching user
    screenName.is.isDefined && partitionedTweets.is.isDefined && ! partitionedTweets.is.get.tweets.isEmpty
}
