package org.talkingpuffin.ui

import java.awt.Color
import scala.swing.{ComboBox, CheckBox, Label, Button, Frame, PasswordField, Publisher,
FlowPanel, GridBagPanel, TextField}
import scala.swing.GridBagPanel.Anchor.West
import swing.event.{SelectionChanged, ButtonClicked, EditDone, Event}
import org.talkingpuffin.twitter.{TwitterSession,AuthenticatedSession,API}
import org.talkingpuffin.state.{Account, Accounts}
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.ui.util.Cancelable

class LoginDialog(cancelPressed: => Unit, startup: (String, AuthenticatedSession) => Unit)
    extends Frame with Cancelable with Loggable {
  
  title = "TalkingPuffin - Log In"
  def username = usernameTextField.text
  def password = new String(passwordTextField.password)
  def accountName = accountNameTextField.text
  def apiUrl = apiUrlTextField.text
  private var ok = false

  private val logInButton = new Button("Log In")
  private val logInAllButton = new Button("Log In All")
  private val cancelButton = new Button("Cancel")
  private val saveUserInfoCheckBox = new CheckBox("Remember me (saves password unencrypted)")
  private val infoLabel = new Label(" ")
  private val up = new Accounts()

  private val comboBox = if (up.data != Nil) new ComboBox(up.data map ComboDisplay) else null
  private val usernameTextField = new TextField() {columns=20}
  private val passwordTextField = new PasswordField() {columns=20}
  private val accountNameTextField = new TextField(API.defaultService) {
    columns=20; tooltip="A short name you associate with this account"}
  private val apiUrlTextField = new TextField(API.defaultURL) {columns=40}
  
  private val enterReaction: PartialFunction[Event, Unit] = { case EditDone(f) => logInButton.peer.doClick() }
  
  setUpEnterClick(true)
  
  def storeUserInfoIfSet() {
    if(saveUserInfoCheckBox.peer.isSelected) up.save(accountName, apiUrl, username, password)
    else up.remove(apiUrl, username)
    up.save()
  }

  saveUserInfoCheckBox.peer.setSelected(true)
  
  private def setUpEnterClick(enable: Boolean) {
    var fields = List[Publisher](usernameTextField, passwordTextField, accountNameTextField, apiUrlTextField)
    if (comboBox != null) fields ::= comboBox
    fields foreach(t => if (enable) t.reactions += enterReaction else t.reactions -= enterReaction)
  }
  
  contents = new GridBagPanel {
    border = scala.swing.Swing.EmptyBorder(5, 5, 5, 5)
    add(if (comboBox != null) comboBox else usernameTextField, 
      new Constraints {grid = (0, 0); gridwidth=2; anchor=West})
    add(new Label("Service Name"),
                                new Constraints {grid=(0,1); anchor=West})
    add(accountNameTextField,   new Constraints {grid=(1,1); anchor=West})
    add(new Label("User Name"), new Constraints {grid=(0,2); anchor=West})
    add(usernameTextField,      new Constraints {grid=(1,2); anchor=West})
    add(new Label("Password"),  new Constraints {grid=(0,3); anchor=West})
    add(passwordTextField,      new Constraints {grid=(1,3); anchor=West})
    add(new Label("URL"),       new Constraints {grid=(0,4); anchor=West})
    add(apiUrlTextField,        new Constraints {grid=(1,4); anchor=West})
    
    add(new FlowPanel {
      contents += logInButton
      // TODO  if (up.users.length > 1) contents += logInAllButton
      contents += cancelButton
      contents += saveUserInfoCheckBox
    }, new Constraints {grid=(0,5); gridwidth=2})
    
    add(infoLabel, new Constraints {grid=(0,6); gridwidth=2; anchor=West})

    reactions += {
      case ButtonClicked(`logInButton`) => {storeUserInfoIfSet(); handleLogin}
      case ButtonClicked(`logInAllButton`) => {storeUserInfoIfSet(); handleAllLogins}
      case ButtonClicked(`cancelButton`) =>
        LoginDialog.this.visible = false
        cancelPressed
      case SelectionChanged(`comboBox`) => showSelectedUser
    }
    if (comboBox != null) listenTo(comboBox.selection)
    listenTo(logInButton, logInAllButton, cancelButton)
  }

  private def showSelectedUser {
    if (comboBox != null) {
      val account = comboBox.selection.item.asInstanceOf[ComboDisplay].account
      accountNameTextField.text = account.service
      usernameTextField.text = account.user 
      passwordTextField.peer.setText(account.password)
      apiUrlTextField.peer.setText(account.apiUrl)
    }
  }
  
  showSelectedUser

  override def notifyOfCancel = cancelPressed
  
  private def handleLogin {
    enableButtons(false)
    var loggedInUser: AuthenticatedSession = null
    
    def showFailure(msg: String) {
      infoLabel.foreground = Color.RED
      infoLabel.text = msg
      enableButtons(true)
    }
    
    LongRunningSpinner.run(this, null, 
      { 
        () =>
        try {
          val sess = TwitterSession(username,password,apiUrl)
          if(sess.verifyCredentials){
            loggedInUser = sess
            true
          }else{
            showFailure("Login failed")
            false
          }
        } catch {
          case e: Exception => {
            showFailure(e.getMessage)
            false
          }
        }
      }, 
      { 
        () =>
        infoLabel.foreground = Color.BLACK
        infoLabel.text = "Login successful. Initializing…"
        startup(accountName, loggedInUser)
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

  private case class ComboDisplay(val account: Account) {
    override def toString = account.service + " " + account.user
  }
}


