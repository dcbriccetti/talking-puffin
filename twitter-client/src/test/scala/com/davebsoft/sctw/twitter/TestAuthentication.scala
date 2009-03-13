package com.davebsoft.sctw.twitter
/**
 * 
 * @author Dave Briccetti
 */

object TestAuthentication {
  def main(args: Array[String]): Unit = {
    val ap = new AuthenticationProvider
    ap.userAuthenticates("x", "y")
  }
}