package org.talkingpuffin.snippet

import xml.NodeSeq
import twitter4j.conf.ConfigurationBuilder
import twitter4j.{TwitterException, TwitterFactory}
import net.liftweb.http._
import org.talkingpuffin.util.{Links, Loggable}

class Auth extends RedirectorWithRequestParms with Loggable {

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
    val requestToken = twitter.getOAuthRequestToken(Links.getRedirectUrl(S.hostName, "login2" + makeUserParm))
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
        S.redirectTo("analyze" + makeUserParm)
      case _ => S.redirectTo("index" + makeUserParm)
    }
  }

}
