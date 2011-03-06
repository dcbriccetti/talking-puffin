package org.talkingpuffin.snippet

import scala.collection.JavaConversions._
import twitter4j.conf.ConfigurationBuilder
import net.liftweb.widgets.tablesorter.{Sorter, Sorting, TableSorter}
import net.liftweb.widgets.flot._
import net.liftweb.http.{RequestVar, SHtml, SessionVar, S}
import net.liftweb.http.js.JsCmds._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import Helpers._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import org.talkingpuffin.apix.PartitionedTweets
import twitter4j.{TwitterException, Twitter, TwitterFactory}
import xml.{Text, NodeSeq}
import org.talkingpuffin.util.{Picture, Loggable}

case class Credentials(user: String, token: String, secret: String)

class Auth extends Loggable {

  object user extends RequestVar[Option[String]](None)
  object ptRv extends RequestVar[PartitionedTweets](null)

  def logIn(in: NodeSeq): NodeSeq = {
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey("o5lqEg5lT19K4xgzwWhjQ")
      .setOAuthConsumerSecret("lSsNHuFhIbVfvvSffuiWWKlvoMd9PkAPtxD47NEG1k")
    val twitter = new TwitterFactory(cb.build()).getInstance()
    Auth.twitterS(Some(twitter))
    Auth.loggedIn(true)
    val requestToken = twitter.getOAuthRequestToken(if (S.hostName == "localhost")
      "http://localhost:8080/login2" else "http://talkingpuffin.org/tpuf/login2")
    S.redirectTo(requestToken.getAuthenticationURL)
    Text("")
  }

  def logIn2(in: NodeSeq): NodeSeq = {
    val token = S.param("oauth_token").get
    val verifier = S.param("oauth_verifier").get
    Auth.twitterS.is match {
      case Some(tw) =>
        val accessToken = tw.getOAuthAccessToken(token, verifier)
        val twitterUser = tw.verifyCredentials
        info("Verified credentials of " + twitterUser.getScreenName)
        S.redirectTo("analyze")
      case _ => S.redirectTo("index")
    }
  }

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

  private def setUserIfValid(u: String): Unit = {
    user(u.trim match {
      case screenName if screenName.length > 0 => Some(screenName)
      case _ => None
    })
  }
  
  def statuses(content: NodeSeq): NodeSeq = {
    val tw = Auth.twitterS.is.get
    bind("statuses", content,
      "statusItems" -> tw.getHomeTimeline.flatMap(st =>
        bind("item", chooseTemplate("statuses", "statusItems", content),
          "status" -> <li>{st.getText}</li>)))
  }

  def tableSorter(content: NodeSeq): NodeSeq = {
    val headers = (2,Sorter("string")) :: Nil
    val sortList = (2,Sorting.ASC) :: Nil

    val options = TableSorter.options(headers,sortList)
    TableSorter("#users", options)
  }

  def user (xhtml: NodeSeq) = bind ("flot", xhtml, "graph" -> Flot.renderHead())

  def generalInfo(xhtml: NodeSeq): NodeSeq = {
    val tw = Auth.twitterS.is.get

    user.is match {
      case Some(screenName) =>
        info(tw.getScreenName + " is analyzing " + screenName)
        try {
          val pt = PartitionedTweets(tw, screenName)
          ptRv(pt)
          val uinfo = tw.lookupUsers(Array(screenName)).get(0)
          <table class="generalUser">
            <tr>
              <td>
                <table>
                  {GeneralUserInfo.create(uinfo, screenName, pt).map(il =>
                    <tr><td class="gnlInfoHead">{il.heading}</td><td class="gnlInfoVal">{il.value}</td></tr>)
                  }
                </table>
              </td>
              <td><img class="profilePic" src={Picture.getFullSizeUrl(uinfo.getProfileImageURL.toString)}
                       alt="Profile Image"/></td>
            </tr>
          </table>
        } catch {
          case te: TwitterException =>
            val failureMsg = "Unable to fetch data for that user. Status code: " + te.getStatusCode
            logger.info(failureMsg)
            S.warning(failureMsg)
            Text("")
        }
      case _ => Text("")
    }
  }

  def plot(xhtml: NodeSeq): NodeSeq =
    user.is match {
      case Some(screenName) =>
        Script(UserTimelinePlotRenderer.render(ptRv.is, screenName))
      case _ => Text("")
    }
}

object Auth extends Loggable {
  object loggedIn extends SessionVar[Boolean](false)
  object twitterS extends SessionVar[Option[Twitter]](None)
}

