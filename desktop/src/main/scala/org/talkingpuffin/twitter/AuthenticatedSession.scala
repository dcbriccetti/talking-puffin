package org.talkingpuffin.twitter

import org.talkingpuffin.ui.util.DesktopUtil
import swing.Dialog
import org.talkingpuffin.util.Loggable
import twitter4j.{Twitter, TwitterException, TwitterFactory}
import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}
import twitter4j.conf.ConfigurationBuilder

object AuthenticatedSession extends Loggable {
  def logIn(): Twitter = {
    val prefs = GlobalPrefs.prefs
    val accessToken = prefs.get(PrefKeys.ACCESS_TOKEN, "")
    val accessTokenSecret = prefs.get(PrefKeys.ACCESS_TOKEN_SECRET, "")
    val tw: Twitter = createTwitter(accessToken, accessTokenSecret)

    if (accessToken == "" || accessTokenSecret == "") {
      val requestToken = tw.getOAuthRequestToken
      DesktopUtil.browse(requestToken.getAuthorizationURL)
      val accessToken = Dialog.showInput(null, "Enter the PIN from the Twitter authorization page in your browser",
        "Enter PIN", Dialog.Message.Question, null, List[String](), "") match {
        case Some(pin: String) =>
          val token = tw.getOAuthAccessToken(requestToken, pin)
          prefs.put(PrefKeys.ACCESS_TOKEN, token.getToken)
          prefs.put(PrefKeys.ACCESS_TOKEN_SECRET, token.getTokenSecret)
          token
        case _ => System.exit(-1)
      }
    }

    try {
        val twitterUser = tw.verifyCredentials
        info("Successfully verified credentials of " + twitterUser.getScreenName)
    } catch {
      case te: TwitterException =>
        te.printStackTrace
        error("Failed to verify credentials: " + te.getMessage)
        prefs.remove(PrefKeys.ACCESS_TOKEN)
        prefs.remove(PrefKeys.ACCESS_TOKEN_SECRET)
        System.exit(-1)
    }

    tw
  }

  private def createTwitter(accessToken: String, accessTokenSecret: String): Twitter = {
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey("Uep9qT3lJ9fHFESZXf9g")
      .setOAuthConsumerSecret("10odNkkH4ZGLkx7oFUS7dsV3VvnwYcC6ZxEriTQ53ps")
    if (accessToken != "" && accessTokenSecret != "") {
      cb.setOAuthAccessToken(accessToken)
        .setOAuthAccessTokenSecret(accessTokenSecret)
    }
    new TwitterFactory(cb.build()).getInstance()
  }
}