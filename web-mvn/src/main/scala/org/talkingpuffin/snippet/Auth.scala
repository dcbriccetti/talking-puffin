package org.talkingpuffin.snippet

import scala.collection.JavaConversions._
import xml.{Text, NodeSeq}
import twitter4j.conf.ConfigurationBuilder
import net.liftweb.widgets.tablesorter.{Sorter, Sorting, TableSorter}
import net.liftweb.widgets.flot._
import net.liftweb.http.{RequestVar, SHtml, SessionVar, S}
import net.liftweb.http.js.JsCmds._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import Helpers._
import net.liftweb.http.js.JE.JsRaw
import org.talkingpuffin.web.Users
import org.talkingpuffin.apix.RichStatus._
import org.talkingpuffin.apix.RichUser._
import org.talkingpuffin.model.TooManyFriendsFollowers
import net.liftweb.http.js.JsCmd
import org.talkingpuffin.apix.PartitionedTweets
import twitter4j.{TwitterException, Twitter, TwitterFactory}
import org.joda.time.{Days, DateTime}
import java.text.NumberFormat
import org.talkingpuffin.util.{LinkExtractor, Loggable}

case class Credentials(user: String, token: String, secret: String)

class Auth extends Loggable {
  object twitterS extends SessionVar[Option[Twitter]](None)

  def logIn(in: NodeSeq): NodeSeq = {
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey("o5lqEg5lT19K4xgzwWhjQ")
      .setOAuthConsumerSecret("lSsNHuFhIbVfvvSffuiWWKlvoMd9PkAPtxD47NEG1k")
    val twitter = new TwitterFactory(cb.build()).getInstance()
    twitterS(Some(twitter))
    Auth.loggedIn(true)
    val requestToken = twitter.getOAuthRequestToken(if (S.hostName == "localhost")
      "http://localhost:8080/login" else "http://talkingpuffin.org:8080/tpuf/login")
    S.redirectTo(requestToken.getAuthorizationURL)
    Text("hi")
  }

  def logIn2(in: NodeSeq): NodeSeq = {
    val token = S.param("oauth_token").get
    val verifier = S.param("oauth_verifier").get
    twitterS.is match {
      case Some(tw) =>
        val accessToken = tw.getOAuthAccessToken(token, verifier)
        val twitterUser = tw.verifyCredentials
        info("Verified credentials of " + twitterUser.getScreenName)
        S.redirectTo("analyze")
      case _ => S.redirectTo("index")
    }
  }

  def statuses(content: NodeSeq): NodeSeq = {
    val tw = twitterS.is.get
    bind("statuses", content,
      "statusItems" -> tw.getHomeTimeline.flatMap(st =>
        bind("item", chooseTemplate("statuses", "statusItems", content),
          "status" -> <li>{st.getText}</li>)))
  }

  def users(content: NodeSeq): NodeSeq = {
    try {
      val tw = twitterS.is.get
      val ux = new Users()
      ux.setSession(tw)
      val userRows = ux.getUsers
      bind("resources", content,
        "resourceItems" -> userRows.flatMap(u =>
          bind("item", chooseTemplate("resources", "resourceItems", content),
            "arrows" -> Text(ux.getArrows(u)),
            "img" -> <img alt="Thumbnail" height="48" width="48" src={u.getProfileImageURL.toString}/>,
            "name" -> Text(u.getName),
            "screenName" -> <span><a target="_blank" href={"/analyze?user=" + u.getScreenName}>{u.getScreenName}</a>
              </span>,
            "friends" -> Text(u.getFriendsCount.toString),
            "followers" -> Text(u.getFollowersCount.toString),
            "location" -> Text(u.location),
            "description" -> Text(u.description),
            "status" -> Text(u.status match {case Some(s) => s.getText case _ => " "})
            )))
    } catch {
      case e: TooManyFriendsFollowers =>
        S.error("Can't process that many friends or followers")
        Text("")
    }
  }

  def tableSorter(content: NodeSeq): NodeSeq = {
    val headers = (2,Sorter("string")) :: Nil
    val sortList = (2,Sorting.ASC) :: Nil

    val options = TableSorter.options(headers,sortList)
    TableSorter("#users", options)
  }

  def user (xhtml: NodeSeq) = {
    bind ("flot", xhtml,
      "graph" -> Flot.render("ph_graph", Nil, new FlotOptions {}, Flot.script(xhtml)))
  }

  object user extends RequestVar[String]("")

  def analyzeUser(xhtml: NodeSeq): NodeSeq = {
    val tw = twitterS.is.get
    user(S.param("user") match {
      case Full(u) => u
      case _ => tw.getScreenName
    })

    def setUser(screenName: String): JsCmd = {
      info(tw.getScreenName + " is analyzing " + screenName)
      user(screenName)
      def disp(msg: String) = S.notice(msg)
      S.clearCurrentNotices
      try {
        val pt = PartitionedTweets(tw, screenName)
        val uinfo = tw.lookupUsers(Array(screenName)).get(0)
        val fmt = NumberFormat.getInstance
        disp(uinfo.getName + " (" + uinfo.getScreenName + ")")
        disp(uinfo.getLocation)
        disp(uinfo.getDescription)
        disp("Followers: " + fmt.format(uinfo.getFollowersCount) +
          ", following: " + fmt.format(uinfo.getFriendsCount))
        val times = pt.tweets.map(_.getCreatedAt.getTime)
        val oldestTime = new DateTime(times.min)
        val newestTime = new DateTime(times.max)
        val range = Days.daysBetween(oldestTime, newestTime)
        val numTweets = pt.tweets.size
        val numReplies = pt.replies.size
        disp("The last " + numTweets + " tweets span " + range.getDays + " days, for an average of " +
          fmt.format(numTweets.toDouble / range.getDays) + " tweets/day" +
          (if (numReplies > 0) ", or " +
          fmt.format((numTweets.toDouble - numReplies) / range.getDays) + " tweets/day if you don’t count the " +
          fmt.format(numReplies) + " replies" else "")
        )
        val links = pt.tweets.flatMap(t => LinkExtractor.getLinks(t.getText, None, false, true, false))
        val numLinks = links.size
        if (numLinks > 0)
          disp("Links in tweets: " + numLinks + " (" + links.distinct.size + " unique)")
        val users = pt.tweets.flatMap(t => LinkExtractor.getLinks(t.getText, None, true, false, false))
        val numUsers = users.size
        if (numUsers > 0)
          disp("Users mentioned: " + numUsers + " (" + users.distinct.size + " unique)")
        UserTimelinePlotRenderer.render(pt, user.is)
      } catch {
        case te: TwitterException =>
          logger.info(te.getStackTraceString)
          S.warning("Unable to fetch data for that user. Status code: " + te.getStatusCode)
          Noop
      }
    }

    SHtml.ajaxText(user.is, setUser _)
  }
}

object Auth extends Loggable {
  object loggedIn extends SessionVar[Boolean](false)
}

