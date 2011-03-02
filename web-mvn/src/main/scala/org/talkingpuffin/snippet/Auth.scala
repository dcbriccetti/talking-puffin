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
import twitter4j.{Status, Twitter, TwitterFactory}
import org.talkingpuffin.util.{EscapeHtml, Loggable}
import net.liftweb.http.js.JsCmd
import org.talkingpuffin.apix.PartitionedTweets

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
    val tw = twitterS.is.get
    val accessToken = tw.getOAuthAccessToken(token, verifier)
    val twitterUser = tw.verifyCredentials
    info("Verified credentials of " + twitterUser.getScreenName)
    S.redirectTo("analyze")
    Text("token: " + token + ", verifier: " + verifier + ", access token: " + accessToken)
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
            "screenName" -> <span><a href={"http://twitter.com/" + u.getScreenName}>{u.getScreenName}</a></span>,
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

    def setUser(screenName: String): JsCmd = {
      info(tw.getScreenName + " is analyzing " + screenName)
      user(screenName)
      val pt = PartitionedTweets(twitterS.is.get, user.is)

      def newSer(heading: String, times: Seq[Status]) =
        new FlotSerie () {
          override def label = Full(heading)
          override val points = Full (new FlotPointsOptions () {
            override val show = Full(true)
          })
          override val data = times.map(_.getCreatedAt.getTime).sorted.map(t => Pair(t.toDouble,1.toDouble)).toList
        }

      Flot.renderJs("ph_graph",
        newSer("Tweets", pt.notReplies) :: newSer("Replies", pt.replies) :: newSer("Retweets", pt.newStyleRetweets
          ) :: newSer("OldRetweets", pt.oldStyleRetweets) :: Nil,
        new FlotOptions {
          override def legend = Full(new FlotLegendOptions () {
            override def container = Full("#legend")
          })
          override val grid = Full (new FlotGridOptions () {
            override def hoverable = Full(true)
          })
          override def xaxis = Full(new FlotAxisOptions {
            override def mode = Full("time")
          })
          override def yaxis = Full(new FlotAxisOptions {
            override def ticks = List(0d)
          })
        }, Flot.script(xhtml)) & JsRaw("""
        var tweets = {
        """ + pt.tweets.map(st => st.getCreatedAt.getTime.toString + ": \"" +
        EscapeHtml(st.text.replaceAll("\n", " ")) + "\"").mkString(",\n") + "}")
    }

    SHtml.ajaxText(tw.getScreenName, setUser _)
  }
}

object Auth extends Loggable {
  object loggedIn extends SessionVar[Boolean](false)
}
