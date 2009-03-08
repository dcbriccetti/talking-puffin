package com.davebsoft.sctw.ui

import _root_.scala.swing._
import _root_.scala.swing.event.{ButtonClicked, EditDone}
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
  startup: (String, String) => Unit) 
  extends JDialog(null: java.awt.Frame, "Simple Twitter Client - Log In", true) {
  
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
  
  setupEnterClick(true)
  
  def storeUserInfoIfSet() {
    if(saveUserInfoCheckBox.peer.isSelected) StateRepository.set((usernameKey, username), (pwdKey, password))
    else StateRepository.remove(usernameKey, pwdKey)
  }

  if (storedUser != null) saveUserInfoCheckBox.peer.setSelected(true)
  
  private def setupEnterClick(enable: Boolean) {
    enterDoesLoginClick(usernameTextField, enable)
    enterDoesLoginClick(passwordTextField, enable)
  }
  
  
  private def enterDoesLoginClick(t: TextField, enable: Boolean) {
    if (enable) t.reactions += enterReaction
    else t.reactions -= enterReaction
  }
  
  setContentPane(new GridBagPanel {
    border = Swing.EmptyBorder(5, 5, 5, 5)
    add(new Label("User name"), new Constraints {gridx=0; gridy=0})
    add(usernameTextField, new Constraints {gridx=1; gridy=0})
    add(new Label("Password"), new Constraints {gridx=0; gridy=1})
    add(passwordTextField, new Constraints {gridx=1; gridy=1})
    add(infoLabel, new Constraints {gridx=0; gridy=2; gridwidth=2; anchor=GridBagPanel.Anchor.West})
    
    add(new FlowPanel {
      contents += loginButton
      contents += cancelButton
      contents += saveUserInfoCheckBox
    }, new Constraints {gridx=0; gridy=3; gridwidth=2})
    
    reactions += {
      case ButtonClicked(b) =>
        ok = (b == loginButton)
        if (ok) {
          handleLogin
        }
        else {
          // Cancel pressed
          setVisible(false)
          cancelPressed
        }
    }
    listenTo(loginButton)
    listenTo(cancelButton)

  }.peer)
  

  private def handleLogin {
    toggleButton(false)
    LongRunningSpinner.run(this, null, 
        { 
          () =>
          if(authenticator.userAuthenticates(username, password)) {
            storeUserInfoIfSet()
            true
          }
          else {
            infoLabel.foreground = Color.RED
            infoLabel.text = "Login failed"
            toggleButton(true)
            false
          }
        }, 
        { 
          () =>
          infoLabel.foreground = Color.BLACK
          infoLabel.text = "Login successful, initializing"
          startup(username, password)
          setVisible(false)
          true
        }
    )
  }
  
  getRootPane.setDefaultButton(loginButton.peer)
  
  private def toggleButton(enable: Boolean) {
    cancelButton.enabled = enable
    loginButton.enabled = enable
    saveUserInfoCheckBox.enabled = enable
    setupEnterClick(enable)
  }
  
  def display = {
    pack
    setLocationRelativeTo(null)
    setVisible(true)
    ok
  }
}