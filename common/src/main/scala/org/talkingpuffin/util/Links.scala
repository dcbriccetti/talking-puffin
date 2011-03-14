package org.talkingpuffin.util

object Links {
  def getRedirectUrl(hostName: String, path: String) =
    (if (hostName == "localhost")
      "http://localhost:8080/"
    else "http://talkingpuffin.org/tpuf/") + path

  def linkForAnalyze(hostName: String, screenName: String) = getRedirectUrl(hostName, "analyze?user=" + screenName)
}
