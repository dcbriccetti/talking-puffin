package org.talkingpuffin.twitter

import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}

/**
 * Twitter login credentials
 */
case class Credentials(service: String, user: String, token: String, secret: String)

object CredentialsRepository {
  private val SERVICE = Constants.ServiceName
  private val TOKEN = PrefKeys.ACCESS_TOKEN
  private val SECRET = PrefKeys.ACCESS_TOKEN_SECRET
  private val prefs = GlobalPrefs.prefsForService(SERVICE)

  /**
   * Creates Credentials for all saved users with an access token and secret.
   */
  def getAll(): List[Credentials] =
    for {username <- prefs.childrenNames.toList
      userNode = prefs.node(username)
      token = userNode.get(TOKEN, "")
      secret = userNode.get(SECRET, "")
      if token != "" && secret != ""
    } yield Credentials(SERVICE, username, token, secret)

  /**
   * Saves the token and secret with the user.
   */
  def save(credentials: Credentials): Credentials = {
    val userPrefs = GlobalPrefs.prefsForUser(SERVICE, credentials.user)
    userPrefs.put(TOKEN, credentials.token)
    userPrefs.put(SECRET, credentials.secret)
    credentials
  }

  /**
   * Deletes the token and secret for the user whose credentials are supplied.
   */
  def delete(credentials: Credentials): Unit = {
    val userPrefs = GlobalPrefs.prefsForUser(SERVICE, credentials.user)
    userPrefs.remove(TOKEN)
    userPrefs.remove(SECRET)
  }

}