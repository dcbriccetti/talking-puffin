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
 * Written for the purposes of learning Scala and the Twitter API, probably not
 * to create a real Twitter client.
 * 
 * Your feedback is welcome!
 * 
 * @Author Dave Briccetti, daveb@davebsoft.com, @dcbriccetti
 */
object Main extends SimpleGUIApplication {
  
  val friendsStatuses = Collections.synchronizedList(new ArrayList[Node]())
  var friendsModel: AbstractTableModel = null

  val publicStatuses = Collections.synchronizedList(new ArrayList[Node]())
  var publicModel: AbstractTableModel = null

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
        preferredSize = new Dimension(600, 600)
        pages.append(new TabbedPane.Page("Friends", new ScrollPane {
          val table = createTable(friendsStatuses)
          friendsModel = table.model.asInstanceOf[AbstractTableModel]
          contents = table
        }))
        pages.append(new TabbedPane.Page("Public", new ScrollPane {
          val table = createTable(publicStatuses)
          publicModel = table.model.asInstanceOf[AbstractTableModel]
          contents = table
        }))
      }
      
      peer.setLocationRelativeTo(null)

      friendsStatusDataProvider.loadTwitterData(friendsStatuses)
      publicStatusDataProvider.loadTwitterData(publicStatuses)

      continuallyLoadData(friendsStatusDataProvider, friendsStatuses, friendsModel)
      continuallyLoadData(publicStatusDataProvider, publicStatuses, publicModel)
    }
  }
    
  private def createTable(statuses: java.util.List[Node]): Table = {
    new Table() {
      model = new StatusTableModel(statuses)
      val colModel = peer.getColumnModel
      colModel.getColumn(0).setPreferredWidth(100)
      colModel.getColumn(1).setPreferredWidth(500)
    }
  }

  private def continuallyLoadData(statusDataProvider: StatusDataProvider,
      statuses: java.util.List[Node], model: AbstractTableModel) {
    new Timer(RELOAD_INTERVAL, new ActionListener() {
      def actionPerformed(event: ActionEvent) {
        statusDataProvider.loadTwitterData(statuses)
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