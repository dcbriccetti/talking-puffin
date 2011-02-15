package org.talkingpuffin.twitter

import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}

case class Credentials(service: String, user: String, token: String, secret: String) {
}

object CredentialsRepository {
  val prefs = GlobalPrefs.prefs

  def getAll(): List[Credentials] = {
    val accessToken = prefs.get(PrefKeys.ACCESS_TOKEN, "")
    val accessTokenSecret = prefs.get(PrefKeys.ACCESS_TOKEN_SECRET, "")
    if (accessToken != "" && accessTokenSecret != "")
      List(Credentials(null, null, accessToken, accessTokenSecret))
    else
      Nil
  }

  def save(credentials: Credentials): Credentials = {
    prefs.put(PrefKeys.ACCESS_TOKEN, credentials.token)
    prefs.put(PrefKeys.ACCESS_TOKEN_SECRET, credentials.secret)
    credentials
  }

  def delete(credentials: Credentials): Unit = {
    prefs.remove(PrefKeys.ACCESS_TOKEN)
    prefs.remove(PrefKeys.ACCESS_TOKEN_SECRET)
  }
}