package com.davebsoft.sctw.ui

import _root_.scala.swing._
import _root_.scala.swing.event.ButtonClicked
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.JDialog
import state.StateRepository

/**
 * Collect user name and password for Twitter authentication.
 * @author Dave Briccetti
 */

object LoginDialog extends JDialog(null: java.awt.Frame, "Simple Twitter Client - Log In", true) {
  
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
  
  def storeUserInfoIfSet() {
    if(saveUserInfoCheckBox.peer.isSelected) StateRepository.set((usernameKey, username), (pwdKey, password))
    else StateRepository.remove(usernameKey, pwdKey)
  }

  if (storedUser != null) saveUserInfoCheckBox.peer.setSelected(true)
  
  private val usernameTextField = new TextField(storedUser) {columns=20}
  private val passwordTextField = new PasswordField(storedPwd) {columns=20}

  
  setContentPane(new GridBagPanel {
    border = Swing.EmptyBorder(5, 5, 5, 5)
    add(new Label("User name"), new Constraints {gridx=0; gridy=0})
    add(usernameTextField, new Constraints {gridx=1; gridy=0})
    add(new Label("Password"), new Constraints {gridx=0; gridy=1})
    add(passwordTextField, new Constraints {gridx=1; gridy=1})
    add(new FlowPanel {
      contents += loginButton
      contents += cancelButton
      contents += saveUserInfoCheckBox
    }, new Constraints {gridx=0; gridy=2; gridwidth=2})
  }.peer)

  loginButton.peer.addActionListener(new ActionListener() {
    def actionPerformed(e: ActionEvent) = {ok = true; setVisible(false); storeUserInfoIfSet()}
  })
  cancelButton.peer.addActionListener(new ActionListener() {
    def actionPerformed(e: ActionEvent) = {ok = false; setVisible(false)}
  })
  
  getRootPane.setDefaultButton(loginButton.peer)
  
  def display = {
    pack
    setLocationRelativeTo(null)
    setVisible(true)
    ok
  }
}