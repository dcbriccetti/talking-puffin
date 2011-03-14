package org.talkingpuffin.snippet

import scala.collection.JavaConversions._
import twitter4j.TwitterException
import net.liftweb.widgets.flot.Flot
import net.liftweb.common.Full
import net.liftweb.util.Helpers._
import net.liftweb.http.{RequestVar, SHtml, S}
import net.liftweb.http.js.JsCmds._
import org.talkingpuffin.snippet.GeneralUserInfo.ScreenNames
import org.talkingpuffin.apix.PartitionedTweets
import org.talkingpuffin.apix.RichStatus._
import xml.{Elem, NodeSeq, Text}
import org.talkingpuffin.user.UserAnalysis
import org.talkingpuffin.snippet.LineCollector.InfoLine
import org.talkingpuffin.util._
import java.text.NumberFormat

/**
 * Snippets for user analysis
 */
class UserAnalyzer extends Loggable {

  object user extends RequestVar[Option[String]](None)
  object partitionedTweets extends RequestVar[Option[PartitionedTweets]](None)

  def nameForm(content: NodeSeq) = {
    val tw = Auth.twitterS.is.get
    S.param("user") match {
      case Full(u) => setUserIfValid(u)
      case _ =>
        if (! user.is.isDefined) {
          user(Some(tw.getScreenName))
        }
    }
    bind("nameForm", content,
      "name"   -> SHtml.text(user.is.getOrElse(""), setUserIfValid(_)),
      "submit" -> SHtml.submit("Analyze", () => {}))
  }

  def headForGraph (xhtml: NodeSeq) = Flot.renderHead()

  def followUnfollow = {
    val tw = Auth.twitterS.is.get

    def check(screenName: String)(on: Boolean) = {
      try {
        if (on) tw.createFriendship(screenName) else tw.destroyFriendship(screenName)
        S.notice(if (on) "Followed" else "Unfollowed")
      } catch {
        case te: TwitterException =>
          S.warning(te.getMessage)
          Text("")
      }
      Noop
    }
    
    user.is match {
      case Some(screenName) if screenName != tw.getScreenName =>
        try {
          val rel = tw.showFriendship(tw.getScreenName, screenName)
          List(SHtml.ajaxCheckbox(rel.isTargetFollowedBySource, check(screenName)), Text("following"))
        } catch {
          case te: TwitterException =>
            // Error will appear to user from elsewhere
            Text("")
        }
      case _ => Text("")
    }
  }

  def generalInfo = {
    val tw = Auth.twitterS.is.get
    val emptyRows = List[Elem]()
    val emptyImage = Text("")

    val (rows, image) = user.is match {
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
          Auth.userAnalysis(ua)
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

  def links = "id=item" #> (Auth.userAnalysis.is match {
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
      case Some(pt) => pt.tweets.toList.map(tw => <tr><td>{TimeUtil2.formatAge(tw.createdAt, false)}</td>
        <td>{tw.text}</td></tr>)
      case _ => List[Elem]()
    }
    "id=tweetRow" #> rows
  }

  def plot(xhtml: NodeSeq): NodeSeq =
    if (user.is.isDefined && partitionedTweets.is.isDefined && ! partitionedTweets.is.get.tweets.isEmpty) // partitionedTweets.is not defined if error fetching user
      Script(UserTimelinePlotRenderer.render(partitionedTweets.is.get, user.is.get))
    else
      Text("")

  private def setUserIfValid(u: String) {
    user(u.trim match {
      case screenName if screenName.length > 0 => Some(screenName)
      case _ => None
    })
  }

  private def createNameLinks(names: List[String]) =
    names.flatMap(name =>
      <span class="screenName"><a href={Links.linkForAnalyze(name)}>{name}</a></span> ++ Text(", ")
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
    "id=row" #> (Auth.userAnalysis.is match {
      case Some(ua) => makeGnlRows(freqProvider(ua))
      case _ => List[Elem]()
    })
  }

  private def expandLink(l: GeneralUserInfo.Link) = {
    val url = l.url.toString
    try {UrlExpander.expand(url)} catch {case _ => url}
  }

}
