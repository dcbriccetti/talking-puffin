package org.talkingpuffin.twitter

import org.talkingpuffin.ui.util.DesktopUtil
import swing.Dialog
import org.talkingpuffin.util.Loggable
import twitter4j.{Twitter, TwitterException, TwitterFactory}
import twitter4j.conf.ConfigurationBuilder
import org.talkingpuffin.apix.Constants

object AuthenticatedSession extends Loggable {

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
      .setOAuthConsumerKey("Uep9qT3lJ9fHFESZXf9g")
      .setOAuthConsumerSecret("10odNkkH4ZGLkx7oFUS7dsV3VvnwYcC6ZxEriTQ53ps")
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

  private def getPin: Option[String] = Dialog.showInput(null,
      "Enter the PIN from the Twitter authorization page in your browser",
      "Enter PIN", Dialog.Message.Question, null, List[String](), "")

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
}