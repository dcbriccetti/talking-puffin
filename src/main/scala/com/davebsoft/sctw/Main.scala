package com.davebsoft.sctw

import _root_.scala.swing.GridBagPanel._
import _root_.scala.swing.event.{ButtonClicked, SelectionChanged, WindowClosing}
import filter.{FilterSet, TextFilter, TagUsers}
import java.lang.reflect.{Proxy,InvocationHandler,Method}
import java.awt.event.{ActionEvent, ActionListener, KeyEvent}
import java.awt.{Dimension, BorderLayout, Insets}
import javax.swing.border.{BevelBorder, EmptyBorder}
import javax.swing.{JToolBar, ImageIcon, UIManager, JFrame}
import org.apache.log4j.Logger
import scala.swing._
import scala.xml._

import TabbedPane._
import state.StateRepository
import twitter._
import ui._
import ui.util.FetchRequest

/**
 * TalkingPuffin
 *
 * @author Dave Briccetti, daveb@davebsoft.com, @dcbriccetti
 */
object Main {
  var log = Logger getLogger "Main"
  val title = "TalkingPuffin" 
  private var user: Node = _
  private var username: String = ""
  private var password: String = ""
  
  object TopFrame {
    var frames = List[TopFrame]()

    def addFrame(f: TopFrame){
      frames = f :: frames
      log debug "new frame added.  Number of frames is " + frames.size
    }

    def removeFrame(f: TopFrame){
      frames = frames.remove {f == _}
      log debug "frame removed.  Number of frames is " + frames.size
      if(frames.size == 0){
          log info "no more frames active, exiting"
          // it's kinda ugly to put the exit logic here, but not sure where
          // else to put it.'
          System.exit(0)
      }
    }

    def numFrames():Int = { frames.size}

    def closeAll() {
      closeAll(frames)
    }

    def closeAll(frames: List[TopFrame]) {
      frames match {
        case frame :: rest => {
          frame.dispose
          TopFrame.removeFrame(frame)
          closeAll(rest)
        }
        case Nil =>
      }
    }

  }
  
  /**
   * The Swing frame.
   */
  class TopFrame(username: String) extends Frame{

    TopFrame.addFrame(this)
    val session = new Session
    Globals.sessions ::= session
    iconImage = new ImageIcon(getClass.getResource("/TalkingPuffin.png")).getImage
    
    val tabbedPane = new TabbedPane() {
      preferredSize = new Dimension(900, 600)
    }
    session.windows.tabbedPane = tabbedPane

    val streams = new Streams(session, username, password)
    session.windows.streams = streams
    val mainToolBar = new MainToolBar(streams)
    
    title = Main.title + " - " + username
    
    menuBar = new MenuBar {
      contents += new Menu("Session") {
        contents += new MenuItem(Action("New...") { launchSession })
      }
    }

    TagUsers.load

    contents = new GridBagPanel {
      val userPic = new Label
      val picFetcher = new PictureFetcher(None, (imageReady: PictureFetcher.ImageReady) => {
        if (imageReady.resource.image.getIconHeight <= Thumbnail.THUMBNAIL_SIZE) {
          userPic.icon = imageReady.resource.image 
        }
      })
      picFetcher.requestItem(new FetchRequest((user \ "profile_image_url").text, null))
      add(userPic, new Constraints { grid = (0,0); gridheight=2})
      add(session.status, new Constraints {
        grid = (1,0); anchor=GridBagPanel.Anchor.West; fill = GridBagPanel.Fill.Horizontal; weightx = 1;  
        })
      peer.add(mainToolBar, new Constraints {grid = (1,1); anchor=GridBagPanel.Anchor.West}.peer)
      add(tabbedPane, new Constraints {
        grid = (0,2); fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; gridwidth=2})
    }

    reactions += {
      case WindowClosing(_) => {
        Globals.sessions = Globals.sessions remove(s => s == session) // TODO is this best way?
        saveState
        TopFrame.removeFrame(this)
      }
    }

    try{
       
      // For handling OSX shutdown stuff

      // use a proxy here, as we can't pull in the Mac ApplicationHandler class'
		  class ShutdownHandler extends InvocationHandler{
		    def invoke(proxy: Any, m: Method, args: Array[Object]):Object = {
          m.getName() match {
              case "handleQuit" => {
                  log.info("application exiting ")
                  saveState
                  TopFrame.closeAll()
              }
              case _ =>
          }
		    }
		  }

      // look up the Mac Application class.  If it isn't found, we should
      // fall through to the ClassNotFoundException catch and just proceed'
	    val applicationClass = Class.forName("com.apple.eawt.Application")
	    val macOSXApplication = applicationClass.getConstructor().newInstance()
	    val applicationListenerClass = Class.forName("com.apple.eawt.ApplicationListener")
      val addListenerMethod = applicationClass.getDeclaredMethod("addApplicationListener", applicationListenerClass);
      // create a proxy that implements ApplicationListener.  This is ugly, but since we can't actually pull in ApplicationListener
      // this is pretty much the best we can do'
	    val osxAdapterProxy = Proxy.newProxyInstance(getClass().getClassLoader(), Array(applicationListenerClass), new ShutdownHandler());
	    addListenerMethod.invoke(macOSXApplication,osxAdapterProxy)
    } catch {
      // this is expected if not running on OSX
      case cnfe: ClassNotFoundException =>
    }


    peer.setLocationRelativeTo(null)

    SwingInvoke.execSwingWorker({
      (new FriendsDataProvider(username, password).getUsers,
        new FollowersDataProvider(username, password).getUsers)
      }, 
      { (result: Tuple2[List[Node],List[Node]]) =>
      val (following, followers) = result 
              
      streams.usersTableModel.friends = following
      streams.usersTableModel.followers = followers
      streams.usersTableModel.usersChanged
 
      streams setFollowerIds (followers map (u => (u \ "id").text))
              
      val paneTitle = "People (" + following.length + ", " + followers.length + ")"
      val pane = new PeoplePane(session, streams.apiHandlers, streams.usersTableModel, following, followers)
      tabbedPane.pages += new TabbedPane.Page(paneTitle, pane)
    })

    private def saveState {
      val highFol = streams.tweetsProvider.getHighestId
      val highMen = streams.mentionsProvider.getHighestId
      log info("Saving last seen IDs. Following: " + highFol + ", mentions: " + highMen)
      if (highFol != null) StateRepository.set(username + "-highestId", highFol)
      if (highMen != null) StateRepository.set(username + "-highestMentionId", highMen)
      StateRepository.save
      TagUsers.save
    }
  }
  
  def main(args: Array[String]): Unit = {
    val props = System.getProperties
    props setProperty("apple.laf.useScreenMenuBar", "true")
    props setProperty("com.apple.mrj.application.apple.menu.about.name", Main.title)
    UIManager setLookAndFeel UIManager.getSystemLookAndFeelClassName
    JFrame setDefaultLookAndFeelDecorated true

    launchSession
  }
  
  def launchSession {
    def startUp(username: String, pwd: String, user: Node) {
      this.user = user
      this.username = username
      password = pwd
      log = Logger getLogger "Main (" + username + ")"

      new TopFrame(username) {
        pack
        visible = true
      }
    }

    new LoginDialog(new twitter.AuthenticationProvider, System.exit(1), startUp).display
  }
}

class ApiHandlers(val sender: Sender, val follower: Follower)

class Session {
  val windows = new Windows
  val status = new Label(" ")
}

object Globals {
  var sessions: List[Session] = Nil
}