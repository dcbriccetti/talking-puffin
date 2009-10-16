package org.talkingpuffin.twitter


/**
* The TwitterSession object acts as a factory for both authenticated and unauthenticated sessions.
* There are two apply methods.
* <tt><pre>
* val session = TwitterSession()
* </pre></tt>
* will return an UnauthenticatedSession instance, which only supports methods which do not require logon.
* <tt><pre>
* val session = TwitterSession(userid,password)
* </pre></tt>
* will return an AuthenticatedSession.  This extends UnauthenticatedSession, and provides additional
* calls that require authentication
*/
object TwitterSession {
  /**
  * get an AuthenticatedSession instance with the provided user and password
  * Note that this should always succeed.  The userid and password are not (currently) checked
  */
  def apply(user: String, password: String): AuthenticatedSession = {
    new AuthenticatedSession(user,password)
  }

  def apply(user: String, password: String, apiURL: String): AuthenticatedSession = {
    new AuthenticatedSession(user,password,apiURL)
  }
  
  /**
  * get an UnauthenticatedSession instance
  */
  def apply(): UnauthenticatedSession = {
    new UnauthenticatedSession()
  }

  def apply(apiURL: String): UnauthenticatedSession = {
    new UnauthenticatedSession(apiURL)
  }
}

/**
* The base class of both TwitterSession objects
*/
abstract class TwitterSession

