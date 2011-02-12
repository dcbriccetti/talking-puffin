package org.talkingpuffin.twitter

import org.talkingpuffin.ui.util.DesktopUtil
import swing.Dialog
import org.talkingpuffin.util.Loggable
import twitter4j.{Twitter, User, TwitterException, TwitterFactory}

object AuthenticatedSession extends Loggable {
  def logIn(): Twitter ={
    val tw = new TwitterFactory().getInstance
    tw.setOAuthConsumer("Uep9qT3lJ9fHFESZXf9g", "10odNkkH4ZGLkx7oFUS7dsV3VvnwYcC6ZxEriTQ53ps")
    val requestToken = tw.getOAuthRequestToken
    info("Request token: "+ requestToken.getToken)
    info("Request token secret: "+ requestToken.getTokenSecret)
    DesktopUtil.browse(requestToken.getAuthorizationURL)
    val accessToken = Dialog.showInput(null, "Enter the PIN from the Twitter authorization page in your browser",
      "Enter PIN", Dialog.Message.Question, null, List[String](), "") match {
      case Some(pin: String) => {
        val token = tw.getOAuthAccessToken(requestToken, pin)
        info("Access token: " + token.getToken)
        info("Access token secret: " + token.getTokenSecret)
        token
      }
      case _ => System.exit(-1)
    }

    try {
        val twitterUser = tw.verifyCredentials
        info("Successfully verified credentials of " + twitterUser.getScreenName)
    } catch {
      case te: TwitterException =>
        te.printStackTrace
        error("Failed to verify credentials: " + te.getMessage)
        System.exit(-1)
    }

    tw
  }
}