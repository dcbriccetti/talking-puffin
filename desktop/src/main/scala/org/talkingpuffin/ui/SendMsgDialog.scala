package org.talkingpuffin.ui

import _root_.scala.actors.Reaction
import _root_.scala.swing._
import _root_.scala.xml.NodeSeq
import event.{SelectionChanged, CaretUpdate, EditDone}
import java.awt.event.{KeyAdapter, KeyEvent}
import java.awt.{Dimension, Font}
import javax.swing.{SwingWorker, KeyStroke}
import twitter.TwitterStatus
import util.Cancelable

/**
 * A dialog for sending messages
 */
class SendMsgDialog(session: Session, parent: java.awt.Component, recipientsOption: Option[String],
    replyToId: Option[String], retweetMsgOption: Option[String]) extends Frame with Cancelable {
  
  class CustomTextArea extends TextArea { 
    preferredSize = new Dimension(400, 80); wordWrap = true; lineWrap = true
    font = new Font("Serif", Font.PLAIN, 18)
  }
  
  case class NameAndScreenName(val name: String, val screenName: String) {
    override def toString = name + " (" + screenName + ")"
    def compareTo(other: NameAndScreenName) = screenName compareToIgnoreCase other.screenName
    def matches(search: String) = {
      val slc = search.toLowerCase
      search.length == 0 || name.toLowerCase.contains(slc) || screenName.toLowerCase.contains(slc)
    }
  }

  title = "Send Message"
  def users = {
    val utm = session.windows.streams.usersTableModel
    val matches = (
      for {
        u <- utm.friends ::: utm.followers
        nsn = NameAndScreenName(u.name, u.screenName)
        if nsn.matches(searchText.text)
      } yield nsn).
      sort((e1, e2) => (e1 compareTo e2) < 0).removeDuplicates
    (matches.length match {
      case 0 => "No matches were found"
      case 1 => "Select the following item to insert a @screenname"
      case _ => "Select one of these " + matches.length + " items to insert a @screenname" 
    }) :: matches
  }
  private val searchText = new TextField {columns = 15}
  private val usersCombo = if (users.length == 0) None else Some(new ComboBox(users))
  private val message = new CustomTextArea
  private val total = new Label(remainingMsg)
  listenTo(message.caret)
  reactions += {
    case CaretUpdate(c) => total.text = remainingMsg 
  }
  message.peer.addKeyListener(new KeyAdapter() {
    override def keyTyped(e: KeyEvent) = if (e.getKeyChar == '\n') send 
  })
  listenTo(searchText)
  reactions += {
    case EditDone(`searchText`) => usersCombo.get.peer.setModel(ComboBox.newConstantModel(users))
  }

  private var userNames = ""
  contents = new GridBagPanel {
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
    usersCombo match {
      case Some(uc) =>
        add(new FlowPanel(FlowPanel.Alignment.Left) {
          val searchLabel = new Label("Search: ")
          searchLabel.peer.setLabelFor(searchText.peer)
          contents += searchLabel
          contents += searchText
        }, new Constr {grid=(0,0); anchor=GridBagPanel.Anchor.West})
        add(uc, new Constr {grid=(0,1); anchor=GridBagPanel.Anchor.West})
        listenTo(uc.selection)
        reactions += {
          case SelectionChanged(`uc`) => uc.selection.item match {
            case nsn: NameAndScreenName => message.peer.insert("@" + nsn.screenName + " ", 
                message.peer.getCaretPosition)
            case _ =>
          }
        }
      case _ =>
    }
    add(message,    new Constr {grid=(0,2); fill=GridBagPanel.Fill.Both; weightx=1; weighty=1})
    add(total,      new Constr {grid=(0,3); anchor=GridBagPanel.Anchor.West})
  }
  pack
  peer.setLocationRelativeTo(parent)
  message.requestFocus
  
  private def send {
    session.status.text = "Sending message"
    new SwingWorker[TwitterStatus, Object] {
      override def doInBackground: TwitterStatus = {
        val twses = session.twitterSession
        replyToId match {
          case Some(idStr) => twses.updateStatus(message.text, java.lang.Long.parseLong(idStr))
          case _ => twses.updateStatus(message.text)
        }
      }
      override def done = {
        val twitterStatus = get
        session.status.text = "Message sent"
      }
    }.execute

    visible = false
  }

  private def remainingMsg = "Characters remaining: " + (140 - message.text.length)
}
