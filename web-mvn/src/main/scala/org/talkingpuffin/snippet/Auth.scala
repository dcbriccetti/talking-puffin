package org.talkingpuffin.snippet

import scala.collection.JavaConversions._
import xml.{Text, NodeSeq}
import org.talkingpuffin.util.Loggable
import twitter4j.conf.ConfigurationBuilder
import twitter4j.{Twitter, TwitterFactory}
import net.liftweb.http.{SessionVar, S}
import net.liftweb.util.Helpers._
import org.talkingpuffin.web.Users
import org.talkingpuffin.apix.RichStatus._
import org.talkingpuffin.apix.RichUser._
import net.liftweb.widgets.tablesorter.{Sorter, Sorting, TableSorter}
import org.talkingpuffin.model.TooManyFriendsFollowers

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
    val requestToken = twitter.getOAuthRequestToken("http://talkingpuffin.org:8080/tpuf/login")
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
    S.redirectTo("statuses")
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
}

object Auth extends Loggable {
  object loggedIn extends SessionVar[Boolean](false)
}
