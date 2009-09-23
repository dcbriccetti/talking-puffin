package org.talkingpuffin.ui

import filter.TagUsers
import java.awt.event.KeyEvent
import javax.swing.{BorderFactory, JCheckBox}
import swing._
import _root_.scala.swing.GridBagPanel._

/**
 * A panel displaying and allowing selection of tags.
 */
class TagsPanel(showTitle: Boolean, showNew: Boolean, tagUsers: TagUsers, checkedValues: List[String]) 
    extends BorderPanel {
  border = BorderFactory.createEmptyBorder(5,5,5,5)
  val checkBoxView = new CheckBoxView(tagUsers.getTags, checkedValues)
  if (showTitle) add(new Label("Tags"), BorderPanel.Position.North)
  
  add(new GridBagPanel {
    add(new FlowPanel {
      contents += new Button(Action("All" ) {checkBoxView.selectAll(true)})
      contents += new Button(Action("None") {checkBoxView.selectAll(false)})
    }, new Constraints {grid=(0,0)})
    add(new ScrollPane(checkBoxView), new Constraints {grid=(0,1); fill=Fill.Both; weighty=1})
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
  private var checkBoxes = List[CheckBox]()
  for ((value, i) <- values.zipWithIndex) {
    val keyVal = KeyEvent.VK_A + i
    val checkBox = new CheckBox("" + keyVal.toChar + CheckBoxView.mnemonicSep + value) {
      peer.setMnemonic(keyVal)
      selected = checkedValues contains value
    }
    checkBoxes ::= checkBox
    contents += checkBox
  }
  
  def selectAll(select: Boolean) = for (b <- checkBoxes) b.selected = select
  
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
