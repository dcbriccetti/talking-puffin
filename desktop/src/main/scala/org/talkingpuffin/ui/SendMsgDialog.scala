package org.talkingpuffin.ui

import _root_.scala.swing._
import event.{SelectionChanged, CaretUpdate, EditDone}
import java.awt.event.{KeyAdapter, KeyEvent}
import java.awt.{Dimension, Font}
import javax.swing.{SwingWorker}
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.ui.util.Cancelable
import org.talkingpuffin.{Globals, Session}
import twitter4j.User

/**
 * A dialog for sending messages
 */
class SendMsgDialog(session: Session, parent: java.awt.Component, recipients: Option[String],
    replyToId: Option[Long], retweetMsg: Option[String], isDm: Boolean) 
    extends Frame with Cancelable with Loggable {
  
  class CustomTextArea extends TextArea { 
    preferredSize = new Dimension(400, 80); wordWrap = true; lineWrap = true
    font = new Font("Serif", Font.PLAIN, 18)
  }
  
  case class NameAndScreenName(val name: String, val screenName: String) extends Ordered[NameAndScreenName] {
    override def toString = screenName + " (" + name + ")"
    def compare(other: NameAndScreenName) = screenName compareToIgnoreCase other.screenName
    def matches(search: String) = {
      val slc = search.toLowerCase
      search.length == 0 || name.toLowerCase.contains(slc) || screenName.toLowerCase.contains(slc)
    }
  }

  title = if (isDm) "Send Direct Message" else "Send Message"
  private def nameAndScreenNames(names: List[User]) = names.map(u =>
      NameAndScreenName(u.getName, u.getScreenName))
  private var sendingSession = session
  private val rels = session.windows.streams.relationships
  
  private def users = {
    val matches = (nameAndScreenNames(rels.friends ::: rels.followers).
        filter(_.matches(searchText.text))).sort(_ < _).removeDuplicates
    (matches.length match {
      case 0 => "No matches were found"
      case 1 => "Select the following item to insert a @screenname"
      case _ => "Select one of these " + matches.length + " items to insert a @screenname" 
    }) :: matches
  }
  private val dmRecipCombo = new ComboBox(
    if (rels.followers == Nil) List("Followers not loaded") else 
      nameAndScreenNames(rels.followers).sortBy(_.name))
  private val searchText = new TextField {columns = 15}
  private val usersCombo = new ComboBox(users)
  private val message = new CustomTextArea
  private val total = new Label(remainingMsg)
  
  listenTo(message.caret)
  reactions += { case CaretUpdate(c) => total.text = remainingMsg }
  listenTo(searchText)
  reactions += {
    case EditDone(`searchText`) => usersCombo.peer.setModel(ComboBox.newConstantModel(users))
  }

  if (! isDm) {
    var userNames = ""
    recipients match {
      case Some(r) => 
        userNames = r
        message.text = userNames + " "
      case None =>
    }
    retweetMsg match {
      case Some(msg) => message.text = "RT " + userNames + " " + msg
      case None =>
    }
  }
  
  contents = new GridBagPanel {
    preferredSize = new Dimension(600, 250)
    border = Swing.EmptyBorder(5,5,5,5)
    class Constr extends Constraints { anchor=GridBagPanel.Anchor.West }
    if (isDm) {
      add(new FlowPanel(FlowPanel.Alignment.Left)() {
        contents += new Label("To: ")
        recipients match {
          case Some(r) => {
            rels.followers.find(_.getScreenName == r) match {
              case Some(u) => dmRecipCombo.selection.item = NameAndScreenName(u.getName, u.getScreenName)
              case _ =>
            }
          }
          case _ =>
        }
        contents += dmRecipCombo
      }, new Constr {grid=(0,0)})
    }
    add(new FlowPanel(FlowPanel.Alignment.Left)() {
      val searchLabel = new Label("Search: ")
      searchLabel.peer.setLabelFor(searchText.peer)
      contents += searchLabel
      contents += searchText
    }, new Constr {grid=(0,1); anchor=GridBagPanel.Anchor.West})
    add(usersCombo, new Constr {grid=(0,2); anchor=GridBagPanel.Anchor.West})
    listenTo(usersCombo.selection)
    reactions += {
      case SelectionChanged(`usersCombo`) => usersCombo.selection.item match {
        case nsn: NameAndScreenName => message.peer.insert("@" + nsn.screenName + " ",
            message.peer.getCaretPosition)
        case _ =>
      }
    }
    add(message, new Constr {grid=(0,3); fill=GridBagPanel.Fill.Both; weightx=1; weighty=1})
    add(total,   new Constr {grid=(0,4); anchor=GridBagPanel.Anchor.West})
    case class SessionDisplay(session: Session) {
      override def toString = session.twitter.getScreenName + " " + session.serviceName
    }
    debug("Sessions: " + Globals.sessions.length)
    if (Globals.sessions.length > 1) {
      add(new FlowPanel(FlowPanel.Alignment.Left)() {
        contents += new Label("Send from:")
        val sessionsCB = new ComboBox(Globals.sessions.map(SessionDisplay)) {
          selection.item = SessionDisplay(session)
        }
        reactions += {
          case SelectionChanged(`sessionsCB`) => sendingSession = sessionsCB.selection.item.session 
        }
        listenTo(sessionsCB.selection)
        contents += sessionsCB
      }, new Constr {grid=(0,5); anchor=GridBagPanel.Anchor.West})
    }
    
    add(new FlowPanel(FlowPanel.Alignment.Left)() {
      val sendButton = new Button(new Action("Send") {
        def apply = send 
        mnemonic = KeyEvent.VK_S
      })
      defaultButton = sendButton
      contents += sendButton
    }, new Constr {grid=(0,6); anchor=GridBagPanel.Anchor.West})
  }
  pack
  peer.setLocationRelativeTo(parent)
  message.requestFocus
  
  private def send {
    session.addMessage("Sending message")
    new SwingWorker[Object, Object] {
      override def doInBackground: Object = {
        val tw = sendingSession.twitter
        if (isDm) dmRecipCombo.selection.item match {
          case u: NameAndScreenName => tw.sendDirectMessage(u.screenName, message.text)
          case _ =>
        } else replyToId match {
          case Some(idStr) => tw.updateStatus(message.text, idStr)
          case _ => tw.updateStatus(message.text)
        }
        null
      }
      override def done = {
        val result = get
        session.addMessage("Message sent")
      }
    }.execute

    visible = false
  }

  private def remainingMsg = "Characters remaining: " + (140 - message.text.length)
}
