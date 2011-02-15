package org.talkingpuffin.twitter

import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}

case class Credentials(service: String, user: String, token: String, secret: String)

object CredentialsRepository {
  private val TWITTER = Constants.ServiceName
  private val TOKEN = PrefKeys.ACCESS_TOKEN
  private val SECRET = PrefKeys.ACCESS_TOKEN_SECRET
  private val prefs = GlobalPrefs.prefsForService(TWITTER)

  def getAll(): List[Credentials] = {
    for {username <- prefs.childrenNames.toList
      userNode = prefs.node(username)
      token = userNode.get(TOKEN, "")
      secret = userNode.get(SECRET, "")
      if token != "" && secret != ""
    } yield Credentials(TWITTER, username, token, secret)
  }

  def save(credentials: Credentials): Credentials = {
    val userPrefs = GlobalPrefs.prefsForUser(TWITTER, credentials.user)
    userPrefs.put(TOKEN, credentials.token)
    userPrefs.put(SECRET, credentials.secret)
    credentials
  }

  def delete(credentials: Credentials): Unit = {
    val userPrefs = GlobalPrefs.prefsForUser(TWITTER, credentials.user)
    userPrefs.remove(TOKEN)
    userPrefs.remove(SECRET)
  }

}