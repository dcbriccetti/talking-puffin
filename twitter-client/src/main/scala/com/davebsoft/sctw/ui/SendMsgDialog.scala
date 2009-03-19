package com.davebsoft.sctw.ui

import _root_.scala.swing._
import java.awt.Dimension

/**
 * A dialog for sending messages
 * @author Dave Briccetti
 */

class SendMsgDialog(parent: java.awt.Component) extends Frame {
  val replyToTweet = new TextArea { preferredSize = new Dimension(500, 100)}
  contents = new GridBagPanel {
    title = "Send Message"
    preferredSize = new Dimension(600, 200)
    border = Swing.EmptyBorder(5,5,5,5)
    class Constr extends Constraints { anchor=GridBagPanel.Anchor.West }
    add(new Label("Reply to Tweet"), new Constr {grid=(0,0)})
    add(replyToTweet, new Constr {grid=(1,0); fill=GridBagPanel.Fill.Both})
    add(new Label("Reply to User"), new Constr {grid=(0,1)})
    add(new Label("Message"), new Constr {grid=(0,2)})
    add(new TextField {columns=140}, new Constr {grid=(1,2); fill=GridBagPanel.Fill.Horizontal; weightx=1})
    add(new FlowPanel { 
      contents += new Button(Action("Send") {println("Send")})
      contents += new Button("Cancel")
    }, new Constr {grid=(0,3)})
  }
  pack
  peer.setLocationRelativeTo(parent)
}