package org.talkingpuffin.snippet

import scala.collection.JavaConversions._
import twitter4j.conf.ConfigurationBuilder
import net.liftweb.widgets.tablesorter.{Sorter, Sorting, TableSorter}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import twitter4j.{TwitterException, Twitter, TwitterFactory}
import xml.{Text, NodeSeq}
import org.talkingpuffin.util.Loggable

case class Credentials(user: String, token: String, secret: String)

class Auth extends Loggable {

  def logIn(in: NodeSeq): NodeSeq = {
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey("o5lqEg5lT19K4xgzwWhjQ")
      .setOAuthConsumerSecret("lSsNHuFhIbVfvvSffuiWWKlvoMd9PkAPtxD47NEG1k")
    val twitter = new TwitterFactory(cb.build()).getInstance()
    Auth.twitterS(Some(twitter))
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
        try {
          val accessToken = tw.getOAuthAccessToken(token, verifier)
          val twitterUser = tw.verifyCredentials
          info("Verified credentials of " + twitterUser.getScreenName)
        } catch {
          case e: TwitterException => S.redirectTo("index")
        }
        Auth.loggedIn(true)
        S.redirectTo("analyze")
      case _ => S.redirectTo("index")
    }
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
}

object Auth extends Loggable {
  object loggedIn extends SessionVar[Boolean](false)
  object twitterS extends SessionVar[Option[Twitter]](None)
}
