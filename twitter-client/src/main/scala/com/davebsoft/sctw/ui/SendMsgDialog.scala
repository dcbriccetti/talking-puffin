package com.davebsoft.sctw.ui

import _root_.scala.swing._
import _root_.scala.swing.event.EditDone
import _root_.scala.xml.NodeSeq
import java.awt.Dimension
import java.awt.event.{KeyAdapter, KeyEvent}
import javax.swing.KeyStroke
import twitter.Sender

/**
 * A dialog for sending messages
 * @author Dave Briccetti
 */

class SendMsgDialog(parent: java.awt.Component, sender: Sender, status: Option[NodeSeq]) extends Frame {
  class CustomTextArea extends TextArea { 
    preferredSize = new Dimension(500, 100); wordWrap = true; lineWrap = true}
  
  val message = new CustomTextArea
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