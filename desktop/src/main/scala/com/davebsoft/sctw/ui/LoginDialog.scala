package com.davebsoft.sctw.ui

import _root_.scala.swing._
import _root_.scala.swing.event.{ButtonClicked, EditDone}
import _root_.scala.xml.Node
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.Color
import javax.swing.JDialog
import state.StateRepository
import twitter.AuthenticationProvider
import LongRunningSpinner._
import event.Event

/**
 * Collect user name and password for Twitter authentication.
 * @author Dave Briccetti
 * @author Alf Kristian Støyle  
 */

class LoginDialog(authenticator: AuthenticationProvider, cancelPressed: => Unit, 
    startup: (String, String, Node) => Unit) 
    extends Frame {
  
  title = "TalkingPuffin - Log In"
  def username = usernameTextField.text
  def password = new String(passwordTextField.password)
  private var ok = false

  val usernameKey = "username"
  val pwdKey = "password"
  private var storedUser = StateRepository.get(usernameKey)
  private var storedPwd = StateRepository.get(pwdKey)
  
  private val loginButton = new Button("Log In")
  private val cancelButton = new Button("Cancel")
  private val saveUserInfoCheckBox = new CheckBox("Remember me")
  private val infoLabel = new Label(" ")

  private val usernameTextField = new TextField(storedUser) {columns=20}
  private val passwordTextField = new PasswordField(storedPwd) {columns=20}
  
  private val enterReaction: PartialFunction[Event, Unit] = { case EditDone(f) => loginButton.peer.doClick() }
  
  setUpEnterClick(true)
  
  def storeUserInfoIfSet() {
    if(saveUserInfoCheckBox.peer.isSelected) StateRepository.set((usernameKey, username), (pwdKey, password))
    else StateRepository.remove(usernameKey, pwdKey)
  }

  if (storedUser != null) saveUserInfoCheckBox.peer.setSelected(true)
  
  private def setUpEnterClick(enable: Boolean) {
    enterDoesLoginClick(usernameTextField, enable)
    enterDoesLoginClick(passwordTextField, enable)
  }
  
  private def enterDoesLoginClick(t: TextField, enable: Boolean) {
    if (enable) t.reactions += enterReaction
    else t.reactions -= enterReaction
  }
  
  contents = new GridBagPanel {
    border = Swing.EmptyBorder(5, 5, 5, 5)
    add(new Label("User name"), new Constraints {grid=(0,0)})
    add(usernameTextField,      new Constraints {grid=(1,0)})
    add(new Label("Password"),  new Constraints {grid=(0,1)})
    add(passwordTextField,      new Constraints {grid=(1,1)})
    add(infoLabel,              new Constraints {grid=(0,2); gridwidth=2; anchor=GridBagPanel.Anchor.West})
    
    add(new FlowPanel {
      contents += loginButton
      contents += cancelButton
      contents += saveUserInfoCheckBox
    }, new Constraints {grid=(0,3); gridwidth=2})
    
    reactions += {
      case ButtonClicked(b) =>
        ok = b == loginButton
        if (ok) {
          handleLogin
        } else {
          // Cancel pressed
          visible = false
          cancelPressed
        }
    }
    listenTo(loginButton)
    listenTo(cancelButton)
  }
  
  private def handleLogin {
    enableButtons(false)
    var loggedInUser: Node = null
    LongRunningSpinner.run(this, null, 
      { 
        () =>
        authenticator.userAuthenticates(username, password) match {
          case Some(user) =>
            loggedInUser = user
            storeUserInfoIfSet()
            true
          case None =>
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
  
  defaultButton = loginButton
  
  private def enableButtons(enable: Boolean) {
    cancelButton.enabled = enable
    loginButton.enabled = enable
    saveUserInfoCheckBox.enabled = enable
    setUpEnterClick(enable)
  }
  
  def display = {
    pack
    peer.setLocationRelativeTo(null)
    visible = true
  }
}