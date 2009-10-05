package org.talkingpuffin.ui

import java.awt.Dimension
import java.awt.event.KeyEvent
import java.util.prefs.Preferences
import javax.swing.border.EmptyBorder
import scala.swing.GridBagPanel._
import org.talkingpuffin.twitter.{TwitterUser, TwitterStatus}
import util.Cancelable
import swing.{TextField, Label, GridBagPanel, Frame, TextArea, FlowPanel, Action, Button}

class UserPropertiesDialog(userPrefs: Preferences, status: TwitterStatus) extends Frame with Cancelable {
  private val screenName = status.user.screenName
  private val props = new UserProperties(userPrefs, screenName)
  title = screenName + " - User Properties"
  contents = new GridBagPanel {
    border = new EmptyBorder(5, 5, 0, 5)
    add(new Label("Name:"), new Constraints {grid=(0,0)})
    val nameField = new TextField(props.getName(status.user.name)) {columns=40}
    add(nameField, new Constraints {grid=(1,0)})
    add(new Label("Notes:"), new Constraints {grid=(0,1)})
    val notesField = new TextArea(props.getNotes) {
      preferredSize = new Dimension(400, 80)
      wordWrap = true
      lineWrap = true
    }
    add(notesField, new Constraints {grid=(1,1); fill=Fill.Both; weightx=1})
    add(new FlowPanel(FlowPanel.Alignment.Left) {
      val saveAction = new Action("Save") {
        mnemonic = KeyEvent.VK_S
        def apply = {
          if (nameField.text == status.user.name) props.removeName else props.putName(nameField.text)
          if (notesField.text == "") props.removeNotes else props.putNotes(notesField.text)
          UserPropertiesDialog.this.visible = false
        }
      }
      contents += new Button(saveAction);
      val cancelAction = new Action("Cancel") {
        def apply = UserPropertiesDialog.this.visible = false
      }
      contents += new Button(cancelAction)
    }, new Constraints {grid=(0,2); gridwidth=2; anchor=Anchor.West})
  }
  peer.setLocationRelativeTo(null)
}

class UserProperties(userPrefs: Preferences, screenName: String) {
  private val userNotes = userPrefs.node("userNotes").node(screenName)
  private val NAME_KEY = "name"
  private val NOTES_KEY = "notes"
  def removeName = userNotes.remove(NAME_KEY)
  def removeNotes = userNotes.remove(NOTES_KEY)
  def putName(text: String) = userNotes.put(NAME_KEY, text)
  def putNotes(text: String) = userNotes.put(NOTES_KEY, text)
  def getName(default: String) = userNotes.get(NAME_KEY, default) 
  def getNotes = userNotes.get(NOTES_KEY, "") 
}

object UserProperties {
  def exists(userPrefs: Preferences, screenName: String) = userPrefs.node("userNotes").nodeExists(screenName)

  def overriddenUserName(userPrefs: Preferences, user: TwitterUser) =
    if (UserProperties.exists(userPrefs, user.screenName)) 
      new UserProperties(userPrefs, user.screenName).getName(user.name)
    else user.name

}