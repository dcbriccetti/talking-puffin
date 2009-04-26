package com.davebsoft.sctw.twitter

class RateLimitStatusProvider(username: String, password: String) extends DataProvider {
  def getUrl = urlHost + "account/rate_limit_status.xml"
}
