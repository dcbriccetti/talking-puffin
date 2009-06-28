package org.talkingpuffin.ui
import _root_.scala.swing.GridBagPanel._
import _root_.scala.swing.{ListView, Frame, GridBagPanel, UIElement, FlowPanel, BorderPanel, BoxPanel, Orientation, Button, CheckBox, Label, ScrollPane, Action}

import filter.TagsRepository
import java.awt.Dimension
import java.awt.event.KeyEvent
import javax.swing.{KeyStroke, JCheckBox}
/**
 * A panel displaying and allowing selection of tags.
 */
class TagsPanel(showTitle: Boolean, checkedValues: List[String]) extends BorderPanel {
  val checkBoxView = new CheckBoxView(TagsRepository.get, checkedValues)
  if (showTitle) add(new Label("Tags"), BorderPanel.Position.North)
  
  add(new ScrollPane {
    contents = checkBoxView
  }, BorderPanel.Position.Center)
      
  def selectedTags: List[String] = {
    var selectedTags = List[String]()
    for (tag <- checkBoxView.getSelectedValues) {
      selectedTags ::= tag.asInstanceOf[String].split(CheckBoxView.mnemonicSep)(1)
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
