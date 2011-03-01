package org.talkingpuffin.snippet

import scala.collection.JavaConversions._
import xml.{Text, NodeSeq}
import org.talkingpuffin.util.Loggable
import twitter4j.conf.ConfigurationBuilder
import twitter4j.{Twitter, TwitterFactory}
import net.liftweb.http.{SessionVar, S}
import net.liftweb.util.Helpers._

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
    val requestToken = twitter.getOAuthRequestToken("http://localhost:8080/login")
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

  def resources(content: NodeSeq): NodeSeq = {
    val tw = twitterS.is.get
    bind("resources", content,
      "resourceItems" -> tw.getHomeTimeline.flatMap(st =>
        bind("item", chooseTemplate("resources", "resourceItems", content),
          "resource" -> <li>{st.getText}</li>)))
  }
}

/*
object AuthenticatedSessionWeb extends Loggable {

  def logIn(credentialsOption: Option[Credentials]): Twitter = {
    val (tw, credentials) = credentialsOption match {
      case Some(cr) => (createTwitter(Some(cr)), cr)
      case None => createAccount()
    }
    verifyCredentials(tw, credentials)
    tw
  }

  private def createTwitter(credentials: Option[Credentials]): Twitter = {
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey("o5lqEg5lT19K4xgzwWhjQ")
      .setOAuthConsumerSecret("lSsNHuFhIbVfvvSffuiWWKlvoMd9PkAPtxD47NEG1k")
    credentials.foreach(c => {
      cb.setOAuthAccessToken(c.token).setOAuthAccessTokenSecret(c.secret)
    })
    new TwitterFactory(cb.build()).getInstance()
  }

  private def createAccount(): (Twitter, Credentials) = {
    val twitter = createTwitter(None)
    val requestToken = twitter.getOAuthRequestToken
    DesktopUtil.browse(requestToken.getAuthorizationURL)
    (twitter, getPin match {
      case Some(pin: String) =>
        val token = twitter.getOAuthAccessToken(requestToken, pin)
        CredentialsRepository.save(Credentials(Constants.ServiceName, token.getScreenName,
          token.getToken, token.getTokenSecret))
      case _ => throw new RuntimeException("No PIN received")
    })
  }

  private def verifyCredentials(tw: Twitter, credentials: Credentials): Unit = {
    try {
      val twitterUser = tw.verifyCredentials
      info("Verified credentials of " + twitterUser.getScreenName)
    } catch {
      case te: TwitterException =>
        CredentialsRepository.delete(credentials)
        throw new RuntimeException("Failed to verify credentials: " + te.getMessage)
    }
  }
}*/