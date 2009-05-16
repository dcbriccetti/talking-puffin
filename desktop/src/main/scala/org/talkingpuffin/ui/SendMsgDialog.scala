package org.talkingpuffin.ui

import _root_.scala.actors.Reaction
import _root_.scala.swing._
import _root_.scala.swing.event.{CaretUpdate, EditDone}
import _root_.scala.xml.NodeSeq
import java.awt.event.{KeyAdapter, KeyEvent}
import java.awt.{Dimension, Font}
import java.net.URLEncoder
import javax.swing.KeyStroke
import twitter.Sender

/**
 * A dialog for sending messages
 */

class SendMsgDialog(session: Session, parent: java.awt.Component, sender: Sender, recipientsOption: Option[String],
    replyToId: Option[String], retweetMsgOption: Option[String]) extends Frame {
  
  class CustomTextArea extends TextArea { 
    preferredSize = new Dimension(400, 80); wordWrap = true; lineWrap = true
    font = new Font("Serif", Font.PLAIN, 18)
  }

  val message = new CustomTextArea
  def remainingMsg = "Characters remaining: " + (140 - message.text.length)
  val total = new Label(remainingMsg)
  listenTo(message.caret)
  reactions += {
    case CaretUpdate(c) => total.text = remainingMsg 
  }
  message.peer.addKeyListener(new KeyAdapter() {
    override def keyTyped(e: KeyEvent) = {
      if (e.getKeyChar == '\n') send 
    }
    override def keyPressed(e: KeyEvent) = if (e.getKeyCode == KeyEvent.VK_ESCAPE) SendMsgDialog.this.visible=false 
  })

  var userNames = ""
  contents = new GridBagPanel {
    title = "Send Message"
    preferredSize = new Dimension(600, 200)
    border = Swing.EmptyBorder(5,5,5,5)
    class Constr extends Constraints { anchor=GridBagPanel.Anchor.West }
    recipientsOption match {
      case Some(recipients) => {
        userNames = recipients
        message.text = userNames + " "
      }
      case None =>
    }
    retweetMsgOption match {
      case Some(retweetMsg) => message.text = "RT " + userNames + " " + retweetMsg
      case None =>
    }
    add(message, new Constr {grid=(0,0); fill=GridBagPanel.Fill.Both; weightx=1; weighty=1})
    add(total,   new Constr {grid=(0,1); anchor=GridBagPanel.Anchor.West})
  }
  pack
  peer.setLocationRelativeTo(parent)
  
  private def send {
    sender.send(message.text, replyToId)
    visible = false
    session.status.text = "Message sent"    
  }
}
