package org.talkingpuffin.ui
import _root_.scala.swing.GridBagPanel._
import _root_.scala.swing.{ListView, Frame, GridBagPanel, UIElement, FlowPanel, Button, CheckBox, Label, ScrollPane, Action}
import filter.TagsRepository
import java.awt.Dimension

/**
 * A panel displaying and allowing selection of tags.
 * 
 * @author Dave Briccetti
 */
class TagsPanel extends GridBagPanel {
  val listView = new ListView(TagsRepository.get)
  add(new Label("Tags"), new Constraints {grid=(0,0)})
  
  add(new ScrollPane {
    preferredSize = new Dimension(100, 170)
    minimumSize = new Dimension(100, 100)
    contents = listView
  }, new Constraints {grid=(0,1)})
      
  def selectedTags: List[String] = {
    var selectedTags = List[String]()
    for (tag <- listView.peer.getSelectedValues) {
      selectedTags ::= tag.asInstanceOf[String]
    }
    selectedTags        
  }
}


