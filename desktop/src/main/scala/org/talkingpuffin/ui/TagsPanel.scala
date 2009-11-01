package org.talkingpuffin.ui

import org.talkingpuffin.filter.TagUsers
import java.awt.event.KeyEvent
import javax.swing.{BorderFactory}
import swing._
import _root_.scala.swing.GridBagPanel._

/**
 * A panel displaying and allowing selection of tags.
 */
class TagsPanel(showTitle: Boolean, showNew: Boolean, tagUsers: TagUsers, checkedValues: List[String]) 
    extends BorderPanel {
  border = BorderFactory.createEmptyBorder(5,5,5,5)
  val checkBoxView = new CheckBoxView(tagUsers.getTagsWithCounts, checkedValues)
  if (showTitle) add(new Label("Tags"), BorderPanel.Position.North)
  
  add(new GridBagPanel {
    add(new FlowPanel {
      contents += new Button(Action("All" ) {checkBoxView.selectAll(true)})
      contents += new Button(Action("None") {checkBoxView.selectAll(false)})
    }, new Constraints {grid=(0,0)})
    add(new ScrollPane(checkBoxView), new Constraints {grid=(0,1); fill=Fill.Both; weightx=1; weighty=1})
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
    var selectedTags = checkBoxView.getSelectedValues
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

class DecoratedCheckBox(value: String, val undecoratedValue: String) extends CheckBox(value) 

class CheckBoxView(values: List[Tuple2[String,Int]], checkedValues: List[String]) 
    extends GridPanel((values.length.toDouble / 2).ceil.toInt, 2) {
  private var checkBoxes = List[DecoratedCheckBox]()
  for ((value, i) <- values.zipWithIndex) {
    val keyVal = KeyEvent.VK_A + i
    val checkBox = new DecoratedCheckBox("" + keyVal.toChar + CheckBoxView.mnemonicSep + 
        value._1 + " (" + value._2 + ")", value._1) {
      peer.setMnemonic(keyVal)
      selected = checkedValues contains value._1
    }
    checkBoxes ::= checkBox
    contents += checkBox
  }
  
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
