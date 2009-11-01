package org.talkingpuffin.ui

import swing.{TextField, Label, GridBagPanel, Frame, CheckBox, FlowPanel, Action, Button}
import swing.GridBagPanel.Anchor
import java.awt.Dimension
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import org.talkingpuffin.filter.{CompoundFilter, TextTextFilter, FromTextFilter, SourceTextFilter}
import util.Cancelable

class CompoundFilterDialog(newCallback: (CompoundFilter) => Unit) extends Frame with Cancelable {
  title = "Filter"
  val panel = new GridBagPanel {
    border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
    class Cns(x: Int, y: Int) extends Constraints {grid = (x, y); anchor = Anchor.West}
    class Regex extends CheckBox("Regex") {
      tooltip = "Whether this search argument is a regular expression"
    }

    add(new Label("From Screen Name"), new Cns(0, 0))
    val from = new TextField {columns = 20; minimumSize = new Dimension(200, preferredSize.height)}
    add(from, new Cns(1, 0))
    val fromRegex = new Regex
    add(fromRegex, new Cns(2, 0))

    add(new Label("Status Text"), new Cns(0, 1))
    val text = new TextField {columns = 40; minimumSize = new Dimension(200, preferredSize.height)}
    add(text, new Cns(1, 1))
    val textRegex = new Regex
    add(textRegex, new Cns(2, 1))

    add(new Label("Application"), new Cns(0, 3))
    val source = new TextField {columns = 20; }
    add(source, new Cns(1, 3))
    val sourceRegex = new Regex
    add(sourceRegex, new Cns(2, 3))

    val rtCb = new CheckBox("RT") {
      tooltip = "Whether the tweet is a retweet"
    }
    add(rtCb, new Cns(0, 4))

    add(new FlowPanel {
      val okAction: Action = new Action("OK") {
        mnemonic = KeyEvent.VK_O
        def apply = {
          def off(tf: TextField) = tf.text.trim.length == 0 
          newCallback(CompoundFilter(
            if (off(from))   None else Some(FromTextFilter(from.text, fromRegex.selected)), 
            if (off(text))   None else Some(TextTextFilter(text.text, textRegex.selected)), 
            None, 
            if (off(source)) None else Some(SourceTextFilter(source.text, sourceRegex.selected)),
            if (! rtCb.selected) None else Some(true)))
          CompoundFilterDialog.this.visible = false
        }
      }
      val okButton = new Button(okAction)
      defaultButton = okButton
      contents += okButton
    }, new Cns(0, 5) {gridwidth = 3})
  }
  contents = panel
  
}