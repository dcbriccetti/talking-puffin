package org.talkingpuffin.snippet

import xml.{Elem, NodeSeq, Text}
import twitter4j.TwitterException
import net.liftweb.widgets.flot.Flot
import net.liftweb.common.Full
import net.liftweb.util.Helpers._
import net.liftweb.http.{RequestVar, SHtml, S}
import net.liftweb.http.js.JsCmds._
import org.talkingpuffin.snippet.GeneralUserInfo.ScreenNames
import org.talkingpuffin.apix.PartitionedTweets
import org.talkingpuffin.util.{Loggable, Links, Picture}

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
          val rows: List[Elem] = GeneralUserInfo.create(uinfo, screenName, pt).map(il =>
            <tr>
              <td class="gnlInfoHead">{il.heading}</td>
              <td class="gnlInfoVal">{il.value match {
                case ScreenNames(sn) => sn.map(name =>
                  <span class="screenName"><a href={Links.linkForAnalyze(name)}>{name}</a> </span>)
                case s => s
              }}</td>
            </tr>)
          val image = <img src={Picture.getFullSizeUrl(uinfo.getProfileImageURL.toString)}
                alt="Profile Image"/>
          (rows, image)
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

  def plot(xhtml: NodeSeq): NodeSeq =
    if (user.is.isDefined && partitionedTweets.is.isDefined) // partitionedTweets.is not defined if error fetching user
      Script(UserTimelinePlotRenderer.render(partitionedTweets.is.get, user.is.get))
    else
      Text("")

  private def setUserIfValid(u: String): Unit = {
    user(u.trim match {
      case screenName if screenName.length > 0 => Some(screenName)
      case _ => None
    })
  }

}
