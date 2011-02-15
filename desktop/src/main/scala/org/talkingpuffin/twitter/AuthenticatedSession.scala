package org.talkingpuffin.twitter

import org.talkingpuffin.ui.util.DesktopUtil
import swing.Dialog
import org.talkingpuffin.util.Loggable
import twitter4j.{Twitter, TwitterException, TwitterFactory}
import twitter4j.conf.ConfigurationBuilder

object AuthenticatedSession extends Loggable {
  def logIn(credentialsOption: Option[Credentials]): Twitter = {
    var tw: Twitter = null
    val credentials = credentialsOption match {
      case Some(cr) =>
        tw = createTwitter(Some(cr))
        cr
      case None =>
        tw = createTwitter(None)
        val requestToken = tw.getOAuthRequestToken
        DesktopUtil.browse(requestToken.getAuthorizationURL)
        Dialog.showInput(null, "Enter the PIN from the Twitter authorization page in your browser",
          "Enter PIN", Dialog.Message.Question, null, List[String](), "") match {
          case Some(pin: String) =>
            val token = tw.getOAuthAccessToken(requestToken, pin)
            CredentialsRepository.save(Credentials(Constants.ServiceName, token.getScreenName,
                token.getToken, token.getTokenSecret))
          case _ => throw new RuntimeException("No PIN received")
        }
    }

    try {
        val twitterUser = tw.verifyCredentials
        info("Verified credentials of " + twitterUser.getScreenName)
    } catch {
      case te: TwitterException =>
        CredentialsRepository.delete(credentials)
        throw new RuntimeException("Failed to verify credentials: " + te.getMessage)
    }

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
}