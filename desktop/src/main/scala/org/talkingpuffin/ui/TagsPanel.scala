package org.talkingpuffin.ui
import _root_.scala.swing.GridBagPanel._
import _root_.scala.swing.{ListView, Frame, GridBagPanel, UIElement, FlowPanel, BorderPanel, Button, CheckBox, Label, ScrollPane, Action}
import filter.TagsRepository
import java.awt.Dimension

/**
 * A panel displaying and allowing selection of tags.
 * 
 * @author Dave Briccetti
 */
class TagsPanel(showTitle: Boolean) extends BorderPanel {
  val listView = new ListView(TagsRepository.get)
  if (showTitle) add(new Label("Tags"), BorderPanel.Position.North)
  
  add(new ScrollPane {
    contents = listView
  }, BorderPanel.Position.Center)
      
  def selectedTags: List[String] = {
    var selectedTags = List[String]()
    for (tag <- listView.peer.getSelectedValues) {
      selectedTags ::= tag.asInstanceOf[String]
    }
    selectedTags        
  }
}


