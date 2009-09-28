package org.talkingpuffin.state

object Account {
  val SEP = '&'
}

case class Account(val service: String, val apiUrl: String, val user: String, val password: String) {
  def serialized = List(service, apiUrl, user, password).mkString(Account.SEP.toString) 
}

class Accounts {
  private val usersKey = "users"
  private val prefs = GlobalPrefs.prefs
  private var _data: List[Account] = load
  def data = _data

  /**
   * Saves to the preferences in a string, by serializing each Account and all of them with a tab
   */
  def save() {
    val ser = _data.map(_.serialized).mkString("\t")
    prefs.put(usersKey, ser)
  }

  /**
   * Adds or replaces the data for a user
   */
  def save(service: String, apiUrl: String, username: String, password: String) {
    _data = Account(service, apiUrl, username, password) :: dataWithout(apiUrl, username)
  }
  
  /**
   * Adds or replaces the data for a user
   */
  def remove(apiUrl: String, username: String) = _data = dataWithout(apiUrl, username)
  
  private def dataWithout(apiUrl: String, username: String) = 
    _data.filter(r => ! (r.apiUrl == apiUrl && r.user == username)) 
  
  private def load: List[Account] = 
    for {
      u <- List.fromString(prefs.get(usersKey, ""), '\t')
      uspw = List.fromString(u, Account.SEP)
      if (uspw.length == 4)
    } yield Account(uspw(0), uspw(1), uspw(2), uspw(3))
  
  def userFor(service: String, username: String): Option[Account] = 
    _data.find(u => u.service == service && u.user == username) match {
      case Some(userPassword) =>
        Some(userPassword)
      case _ => None
    }
}