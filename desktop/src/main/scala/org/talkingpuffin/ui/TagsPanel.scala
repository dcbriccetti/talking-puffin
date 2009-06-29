package org.talkingpuffin.ui

import filter.TagUsers
import java.awt.Dimension
import java.awt.event.KeyEvent
import javax.swing.{BorderFactory, KeyStroke, JCheckBox}
import swing._

/**
 * A panel displaying and allowing selection of tags.
 */
class TagsPanel(showTitle: Boolean, showNew: Boolean, tagUsers: TagUsers, checkedValues: List[String]) 
    extends BorderPanel {
  border = BorderFactory.createEmptyBorder(5,5,5,5)
  val checkBoxView = new CheckBoxView(tagUsers.getTags, checkedValues)
  if (showTitle) add(new Label("Tags"), BorderPanel.Position.North)
  
  add(new ScrollPane {
    contents = checkBoxView
  }, BorderPanel.Position.Center)

  var newTag: TextField = _
  
  if (showNew) {
    add(new BoxPanel(Orientation.Horizontal) {
      contents += new Label("New: ")
      newTag = new TextField(15)
      contents += newTag
    }, BorderPanel.Position.South)
  }
      
  def selectedTags: List[String] = {
    var selectedTags = List[String]()
    for (tag <- checkBoxView.getSelectedValues) {
      selectedTags ::= tag.asInstanceOf[String].split(CheckBoxView.mnemonicSep)(1)
    }
    if (showNew) {
      val newTagVal = newTag.text.trim
      if (newTagVal.length > 0) selectedTags ::= newTagVal
    }
    selectedTags        
  }
}

object CheckBoxView {
  val mnemonicSep = ": "
}

class CheckBoxView(values: List[String], checkedValues: List[String]) extends BoxPanel(Orientation.Vertical) {
  var i = 0
  for (value <- values) {
    val keyVal = KeyEvent.VK_A + i
    contents += new CheckBox("" + keyVal.toChar + CheckBoxView.mnemonicSep + value) {
      peer.setMnemonic(keyVal)
      i += 1
      selected = checkedValues contains value
    }
  }
  
  def getSelectedValues: Seq[String] = {
    var values = List[String]()
    for (child <- peer.getComponents) {
      child match {
        case cb: JCheckBox => if (cb.isSelected) values = cb.getText :: values
        case _ =>
      }
    }
    values
  }
}
