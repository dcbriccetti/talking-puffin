package org.talkingpuffin.util

object Links {
  def getRedirectUrl(hostName: String, path: String) = (
    if (hostName == "localhost")
      "http://localhost:8080/"
    else if (hostName.contains("vcloudlabs"))
      "http://tpuf.vcloudlabs.com/"
    else "http://talkingpuffin.org/tpuf/"
    ) + path

  def linkForAnalyze(screenName: String, hostName: String = System.getProperty("webHost", "talkingpuffin.org")) =
    getRedirectUrl(hostName, "analyze?user=" + screenName)
}
