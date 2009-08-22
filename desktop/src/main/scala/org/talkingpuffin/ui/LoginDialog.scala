package org.talkingpuffin.ui

import _root_.scala.swing.{ComboBox, CheckBox, Label, Button, Frame, PasswordField, Publisher, 
FlowPanel, GridBagPanel, TextField}
import ComboBox._
import _root_.scala.xml.Node
import collection.mutable.Subscriber
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.Color
import java.util.prefs.Preferences
import javax.swing.JDialog
import javax.swing.SpringLayout.Constraints
import org.talkingpuffin.twitter.{TwitterSession,AuthenticatedSession,API}
import state.{GlobalPrefs, PreferencesFactory}
import LongRunningSpinner._
import swing.event.{SelectionChanged, ButtonClicked, EditDone, Event}
import talkingpuffin.util.Loggable
import util.Cancelable

/**
 * Collect user name and password for Twitter authentication.
 */
class LoginDialog(cancelPressed: => Unit, 
    startup: (String, String, AuthenticatedSession) => Unit)
    extends Frame with Cancelable with Loggable {
  
  title = "TalkingPuffin - Log In"
  def username = usernameTextField.text
  def password = new String(passwordTextField.password)
  def apiURL = apiURLTextField.text
  private var ok = false

  private val logInButton = new Button("Log In")
  private val logInAllButton = new Button("Log In All")
  private val cancelButton = new Button("Cancel")
  private val saveUserInfoCheckBox = new CheckBox("Remember me (saves password unencrypted)")
  private val infoLabel = new Label(" ")
  private val up = new UsersPasswords()

  private val comboBox = if (up.users.length > 0) new ComboBox(up.users) else null
  private val usernameTextField = new TextField() {columns=20}
  private val passwordTextField = new PasswordField() {columns=20}
  private val apiURLTextField = new TextField() {columns=40; text=API.defaultURL}
  
  private val enterReaction: PartialFunction[Event, Unit] = { case EditDone(f) => logInButton.peer.doClick() }
  
  setUpEnterClick(true)
  
  def storeUserInfoIfSet() {
    if(saveUserInfoCheckBox.peer.isSelected) up.save(username, password)
    else up.remove(username)
    up.save()
  }

  saveUserInfoCheckBox.peer.setSelected(true)
  
  private def setUpEnterClick(enable: Boolean) {
    if (comboBox != null) enterDoesLoginClick(comboBox, enable)
    enterDoesLoginClick(usernameTextField, enable)
    enterDoesLoginClick(passwordTextField, enable)
    enterDoesLoginClick(apiURLTextField, enable)
  }
  
  private def enterDoesLoginClick(t: Publisher, enable: Boolean) {
    if (enable) t.reactions += enterReaction
    else t.reactions -= enterReaction
  }
  
  contents = new GridBagPanel {
    border = scala.swing.Swing.EmptyBorder(5, 5, 5, 5)
    add(if (comboBox != null ) comboBox else usernameTextField, 
      new Constraints {grid = (0, 0); gridwidth=2; anchor=GridBagPanel.Anchor.West})
    add(new Label("User name"), new Constraints {grid=(0,1)})
    add(usernameTextField, new Constraints {grid = (1, 1); anchor=GridBagPanel.Anchor.West})
    add(new Label("Password"),  new Constraints {grid=(0,2); anchor=GridBagPanel.Anchor.West})
    add(passwordTextField,      new Constraints {grid=(1,2); anchor=GridBagPanel.Anchor.West})
    add(new Label("API Server"),  new Constraints {grid=(0,3); anchor=GridBagPanel.Anchor.West})
    add(apiURLTextField,      new Constraints {grid=(1,3); anchor=GridBagPanel.Anchor.West})
    add(infoLabel,              new Constraints {grid=(0,5); gridwidth=2; anchor=GridBagPanel.Anchor.West})
    
    add(new FlowPanel {
      contents += logInButton
      // TODO  if (up.users.length > 1) contents += logInAllButton
      contents += cancelButton
      contents += saveUserInfoCheckBox
    }, new Constraints {grid=(0,4); gridwidth=2})
    
    reactions += {
      case ButtonClicked(`logInButton`) => {storeUserInfoIfSet(); handleLogin}
      case ButtonClicked(`logInAllButton`) => {storeUserInfoIfSet(); handleAllLogins}
      case ButtonClicked(`cancelButton`) =>
        LoginDialog.this.visible = false
        cancelPressed
      case SelectionChanged(`comboBox`) => showSelectedUser
    }
    if (comboBox != null) listenTo(comboBox.selection)
    listenTo(logInButton)
    listenTo(logInAllButton)
    listenTo(cancelButton)
  }

  private def showSelectedUser {
    if (comboBox != null) {
      val item = comboBox.selection.item
      usernameTextField.text = item
      up.passwordFor(item) match {
        case Some(pw) =>
          passwordTextField.peer.setText(pw)
        case _ =>
      }
    }
  }
  
  showSelectedUser

  override def notifyOfCancel = cancelPressed
  
  private def handleLogin {
    enableButtons(false)
    var loggedInUser: AuthenticatedSession = null
    LongRunningSpinner.run(this, null, 
      { 
        () =>
        val sess = TwitterSession(username,password,apiURL)
        if(sess.verifyCredentials){
            loggedInUser = sess
            true
        }else{
            infoLabel.foreground = Color.RED
            infoLabel.text = "Login failed"
            enableButtons(true)
            false
        }
      }, 
      { 
        () =>
        infoLabel.foreground = Color.BLACK
        infoLabel.text = "Login successful. Initializing…"
        startup(username, password, loggedInUser)
        visible = false
        true
      }
    )
  }
  
  private def handleAllLogins = { /* TODO */ }
  
  defaultButton = logInButton
  
  private def enableButtons(enable: Boolean) {
    cancelButton.enabled = enable
    logInButton.enabled = enable
    saveUserInfoCheckBox.enabled = enable
    setUpEnterClick(enable)
  }
  
  def display = {
    pack
    peer.setLocationRelativeTo(null)
    visible = true
  }
}

object UserPassword {
  val SEP = '&'
}

case class UserPassword(var user: String, var password: String) {
  def serialized = user + UserPassword.SEP + password
}

class UsersPasswords extends Loggable {
  val usersKey = "users"
  private val prefs = GlobalPrefs.prefs
  
  var data: List[UserPassword] = load
  
  def save() {
    val ser = data.map(_.serialized).mkString("\t")
    prefs.put(usersKey, ser)
  }
  def save(username: String, password: String) {
    data = UserPassword(username, password) :: data.filter(_.user != username)
  }
  def remove(username: String) = data = data.filter(_.user != username)
  
  def load: List[UserPassword] = 
    for (u <- List.fromString(prefs.get(usersKey, ""), '\t'); uspw = List.fromString(u, UserPassword.SEP)) 
      yield UserPassword(uspw(0), uspw(1))
  
  def users: List[String] = data.map(_.user)
  
  def passwordFor(username: String): Option[String] = 
    data.find(_.user == username) match {
      case Some(userPassword) =>
        Some(userPassword.password)
      case _ => None
    }
}