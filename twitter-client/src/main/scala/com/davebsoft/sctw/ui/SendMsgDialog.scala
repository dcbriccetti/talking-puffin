package com.davebsoft.sctw.ui

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
 * @author Dave Briccetti
 */

class SendMsgDialog(parent: java.awt.Component, sender: Sender, status: Option[NodeSeq]) extends Frame {
  class CustomTextArea extends TextArea { 
    preferredSize = new Dimension(400, 80); wordWrap = true; lineWrap = true
    font = new Font(font.getFontName, Font.BOLD, font.getSize * 150 / 100)
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

  var userName = ""
  contents = new GridBagPanel {
    title = "Send Message"
    preferredSize = new Dimension(600, 200)
    border = Swing.EmptyBorder(5,5,5,5)
    class Constr extends Constraints { anchor=GridBagPanel.Anchor.West }
    status match {
      case Some(s) => {
        userName = "@" + (s \ "user" \ "screen_name").text
        message.text = userName + " "
      }
      case None =>
    }
    add(message, new Constr {grid=(0,0); fill=GridBagPanel.Fill.Both; weightx=1; weighty=1})
    add(total,   new Constr {grid=(0,1); anchor=GridBagPanel.Anchor.West})
  }
  pack
  peer.setLocationRelativeTo(parent)
  
  private def send {
    val replyTo = status match { 
      case Some(s) => if (message.text.startsWith(userName)) Some((s \ "id").text) else None 
      case None => None 
    }
    sender.send(message.text, replyTo)
    visible = false
  }
}