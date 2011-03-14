package org.talkingpuffin.snippet

import scala.collection.JavaConversions._
import twitter4j.conf.ConfigurationBuilder
import net.liftweb.http._
import net.liftweb.util.Helpers._
import twitter4j.{TwitterException, TwitterFactory}
import xml.NodeSeq
import org.talkingpuffin.util.{Links, Loggable}

case class Credentials(user: String, token: String, secret: String)

class Auth extends Loggable {

  /**
   * Does first stage of Twitter authentication, by redirecting to Twitter
   */
  def logIn1(in: NodeSeq): NodeSeq = {
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey("o5lqEg5lT19K4xgzwWhjQ")
      .setOAuthConsumerSecret("lSsNHuFhIbVfvvSffuiWWKlvoMd9PkAPtxD47NEG1k")
    val twitter = new TwitterFactory(cb.build()).getInstance()
    SessionState.twitter(Some(twitter))
    val requestToken = twitter.getOAuthRequestToken(Links.getRedirectUrl(S.hostName, "login2"))
    S.redirectTo(requestToken.getAuthenticationURL)
    Nil
  }

  /**
   * Having been redirected here from Twitter, completes the login process
   */
  def logIn2(in: NodeSeq): NodeSeq = {
    val token = S.param("oauth_token").get
    val verifier = S.param("oauth_verifier").get
    SessionState.twitter.is match {
      case Some(tw) =>
        try {
          val accessToken = tw.getOAuthAccessToken(token, verifier)
          val twitterUser = tw.verifyCredentials
          info("Verified credentials of " + twitterUser.getScreenName)
        } catch {
          case e: TwitterException => S.redirectTo("index")
        }
        SessionState.loggedIn(true)
        S.redirectTo("analyze")
      case _ => S.redirectTo("index")
    }
  }
}
