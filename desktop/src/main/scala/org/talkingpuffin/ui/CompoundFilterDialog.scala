package org.talkingpuffin.ui

import swing.{TextField, Label, GridBagPanel, Frame, CheckBox, FlowPanel, Action, Button}
import swing.GridBagPanel.Anchor
import java.awt.Dimension
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import util.Cancelable
import org.talkingpuffin.filter._

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

    add(new Label("To Screen Name"), new Cns(0, 1))
    val to = new TextField {columns = 20; minimumSize = new Dimension(200, preferredSize.height)}
    add(to, new Cns(1, 1))
    val toRegex = new Regex
    add(toRegex, new Cns(2, 1))

    add(new Label("Status Text"), new Cns(0, 2))
    val text = new TextField {columns = 40; minimumSize = new Dimension(200, preferredSize.height)}
    add(text, new Cns(1, 2))
    val textRegex = new Regex
    add(textRegex, new Cns(2, 2))

    add(new Label("Application"), new Cns(0, 3))
    val source = new TextField {columns = 20; }
    add(source, new Cns(1, 3))
    val sourceRegex = new Regex
    add(sourceRegex, new Cns(2, 3))

    val rtCb = new CheckBox("Retweet") {
      tooltip = "Whether the tweet is a retweet"
    }
    add(rtCb, new Cns(0, 4))

    val crtCb = new CheckBox("Commented Retweet") {
      tooltip = "Whether the tweet is a commented retweet: [comment] RT [user] [tweet]"
    }
    add(crtCb, new Cns(0, 5))

    add(new FlowPanel {
      val okAction: Action = new Action("OK") {
        mnemonic = KeyEvent.VK_O
        def apply = {
          def on(tf: TextField) = tf.text.trim.length > 0
          var filters = List[TextFilter]()
          if (on(from)) filters ::= FromTextFilter(from.text, fromRegex.selected)
          if (on(text)) filters ::= TextTextFilter(text.text, textRegex.selected)
          if (on(to))   filters ::= ToTextFilter(to.text, toRegex.selected)
          if (on(source)) filters ::= SourceTextFilter(source.text, sourceRegex.selected)
        newCallback(CompoundFilter(filters,
            if (! rtCb.selected) None else Some(true),
            if (! crtCb.selected) None else Some(true)
          ))
          CompoundFilterDialog.this.visible = false
        }
      }
      val okButton = new Button(okAction)
      defaultButton = okButton
      contents += okButton
    }, new Cns(0, 6) {gridwidth = 3})
  }
  contents = panel
  
}