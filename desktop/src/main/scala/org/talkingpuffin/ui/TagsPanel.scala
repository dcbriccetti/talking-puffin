package org.talkingpuffin.ui

import org.talkingpuffin.filter.TagUsers
import java.awt.event.KeyEvent
import javax.swing.{BorderFactory}
import swing._
import _root_.scala.swing.GridBagPanel._
import java.awt.Insets

/**
 * A panel displaying and allowing selection of tags.
 */
class TagsPanel(showTitle: Boolean, showNew: Boolean, tagUsers: TagUsers, checkedValues: List[String]) 
    extends BorderPanel {
  border = BorderFactory.createEmptyBorder(5,5,5,5)
  private val checkBoxView = new CheckBoxView(tagUsers, checkedValues)

  add(new FlowPanel {
    if (showTitle) contents += new Label("Tags")
    contents += new Button(Action("All" ) {checkBoxView.selectAll(true)})
    contents += new Button(Action("None") {checkBoxView.selectAll(false)})
  }, BorderPanel.Position.North)
  
  add(new GridBagPanel {
    add(new ScrollPane(checkBoxView), new Constraints {grid=(0,1); fill=Fill.Both; weightx=1; weighty=1})
  }, BorderPanel.Position.Center)

  var newTag: TextField = _
  var newTagDesc: TextField = _
  
  if (showNew) {
    add(new GridBagPanel {
      val ins = new Insets(5,5,0,0)
      add(new Label("Check above, and/or add a new tag name and description: "), 
          new Constraints{anchor=Anchor.West; grid=(0,0); gridwidth=2; insets=ins})
      newTag = new TextField(15)
      add(newTag, new Constraints{anchor=Anchor.West; grid=(0,1); insets=ins}) 
      newTagDesc = new TextField(25)
      add(newTagDesc, new Constraints{anchor=Anchor.West; grid=(1,1); insets=ins})
    }, BorderPanel.Position.South)
  }
      
  def selectedTags: List[String] = {
    var selectedTags = checkBoxView.getSelectedValues
    if (showNew) {
      val newTagVal = newTag.text.trim
      if (newTagVal.length > 0) {
        selectedTags ::= newTagVal
        val newTagDescVal = newTagDesc.text.trim
        if (newTagDescVal.length > 0)
          tagUsers.addDescription(newTagVal, newTagDescVal)
      } 
    }
    selectedTags        
  }
}

object CheckBoxView {
  val mnemonicSep = ": "
}

private class DecoratedCheckBox(value: String, val undecoratedValue: String) extends CheckBox(value) 

private class CheckBoxView(tagUsers: TagUsers, checkedValues: List[String]) extends GridBagPanel {
  private val values = tagUsers.getTagsWithCounts
  private val cols = 3
  private val rows = (values.length.toDouble / cols).ceil.toInt
  private var checkBoxes = List[DecoratedCheckBox]()
  for (((tag, count), i) <- values.zipWithIndex) {
    val keyVal = KeyEvent.VK_A + i
    val checkBox = new DecoratedCheckBox("" + keyVal.toChar + CheckBoxView.mnemonicSep + 
        tag + " (" + count + ")", tag) {
      peer.setMnemonic(keyVal)
      tagUsers.getDescription(tag) match {
        case Some(desc) => tooltip = desc
        case _ =>
      }
      selected = checkedValues contains tag
    }
    checkBoxes ::= checkBox
    add(checkBox, new Constraints{anchor=Anchor.West; grid=(i / rows, i % rows)})
  }
  add(new Label(""), new Constraints{fill=Fill.Both; weightx=1; weighty=1; grid=(cols, rows)}) 
  
  def selectAll(select: Boolean) = for (b <- checkBoxes) b.selected = select
  
  def getSelectedValues: List[String] = {
    var values = List[String]()
    for (child <- contents) {
      child match {
        case cb: DecoratedCheckBox => if (cb.selected) values = cb.undecoratedValue :: values
        case _ =>
      }
    }
    values
  }
}
