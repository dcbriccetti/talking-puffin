package com.davebsoft.sctw

import java.awt.Dimension
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.table.{DefaultTableModel, AbstractTableModel}
import javax.swing.{SwingUtilities, Timer}
import java.util.{ArrayList,Collections}
import scala.swing._
import scala.xml._

/**
 * Continually displays Twitter statuses in a Swing JTable.
 * Written to learn and teach Scala and the Twitter API, probably not
 * to create a real Twitter client.
 * 
 * Your feedback is welcome!
 * 
 * @Author Dave Briccetti, daveb@davebsoft.com, @dcbriccetti
 */
object Main extends SimpleGUIApplication {
  
  val friendsModel = new StatusTableModel
  val publicModel = new StatusTableModel

  /** How often, in ms, to fetch and load new data */
  private final var RELOAD_INTERVAL = 120 * 1000;
  
  private var username: String = null
  private var password: String = null

  /**
   * Creates the Swing frame, which consists of a TabbedPane with two panes, 
   * with each pane containing a JTable inside a JScrollPane.
   */
  def top = {
    val friendsStatusDataProvider = new FriendsStatusDataProvider(username, password)
    val publicStatusDataProvider = new PublicStatusDataProvider()
  
    new MainFrame {
      title = "Too-Simple Twitter Client"
      
      contents = new TabbedPane() {
        pages.append(new TabbedPane.Page("Friends", createStatusPane(friendsModel)))
        pages.append(new TabbedPane.Page("Public", createStatusPane(publicModel)))
      }
      
      peer.setLocationRelativeTo(null)

      friendsStatusDataProvider.loadTwitterData(friendsModel.getStatuses)
      publicStatusDataProvider.loadTwitterData(publicModel.getStatuses)

      continuallyLoadData(friendsStatusDataProvider, friendsModel)
      continuallyLoadData(publicStatusDataProvider, publicModel)
    }
  }
    
  private def createStatusPane(statusTableModel: StatusTableModel): Component = {
    new BoxPanel(Orientation.Vertical) {
      contents += new ScrollPane {
        contents = new Table() {
          model = statusTableModel
          val colModel = peer.getColumnModel
          colModel.getColumn(0).setPreferredWidth(100)
          colModel.getColumn(1).setPreferredWidth(500)
        }
      }
    }
  }

  private def continuallyLoadData(statusDataProvider: StatusDataProvider, model: StatusTableModel) {
    new Timer(RELOAD_INTERVAL, new ActionListener() {
      def actionPerformed(event: ActionEvent) {
        statusDataProvider.loadTwitterData(model.getStatuses)
        model.fireTableDataChanged
      }
    }).start
  }

  override def main(args: Array[String]): Unit = {
    if (args.length >= 2) {
      username = args(0)
      password = args(1)
      super.main(args)
    } else {
      println("Missing username and password")
    }
  }
}