package com.davebsoft.sctw

import java.awt.Dimension
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.table.{DefaultTableModel, AbstractTableModel}
import javax.swing.{SwingUtilities, Timer}
import java.util.{ArrayList,Collections}
import scala.swing._
import scala.xml._
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod

/**
 * Continually displays current Twitter public timeline statuses in a Swing JTable.
 * Written for the purposes of learning Scala and the Twitter API. Your feedback is welcome!
 * I want to know every little thing that should be improved.
 * 
 * @Author Dave Briccetti, daveb@davebsoft.com, @dcbriccetti
 */
object Main extends SimpleGUIApplication {
  
  /** Statuses, in a list for direct access from table model */
  val statuses = Collections.synchronizedList(new ArrayList[Node]())
  
  val httpClient = new HttpClient()
  val timelineMethod = new GetMethod("http://twitter.com/statuses/public_timeline.xml")
  var publicModel: AbstractTableModel = null
  val friendsMethod = new GetMethod("http://twitter.com/statuses/friends_timeline.xml")
  
  /** How often, in ms, to fetch and load new data */
  private final var RELOAD_INTERVAL = 10000;

  loadTwitterData(statuses)

  /**
   * Creates the Swing frame, which consists of a JTable inside a JScrollPane.
   */
  def top = {
    new MainFrame {
      title = "Too-Simple Twitter Client"
      val tp: TabbedPane = new TabbedPane() {
        pages.append(new TabbedPane.Page("Public", new ScrollPane {
          preferredSize = new Dimension(600, 600)
          contents = new Table() {
            model = new StatusTableModel(statuses)
            publicModel = model.asInstanceOf[AbstractTableModel]
            val colModel = peer.getColumnModel
            colModel.getColumn(0).setPreferredWidth(100)
            colModel.getColumn(1).setPreferredWidth(500)
          }
        }))
      }
      contents = tp
      peer.setLocationRelativeTo(null)

      continuallyLoadData(this)
    }
  }

  /**
   * Reloads the data periodically
   */
  private def continuallyLoadData(container: Container) {
    new Timer(RELOAD_INTERVAL, new ActionListener() {
      def actionPerformed(event: ActionEvent) {
        loadTwitterData(statuses)
        publicModel.fireTableDataChanged
      }
    }).start
  }

  /**
   * Fetches statuses from Twitter and stores them in statuses field.
   */
  private def loadTwitterData(statuses: java.util.List[Node]) {
    val result = httpClient.executeMethod(timelineMethod)
    val timeline = XML.load(timelineMethod.getResponseBodyAsStream())
    statuses.clear
    for (st <- timeline \\ "status") {
      statuses.add(st)
    }
  }

  override def main(args: Array[String]): Unit = super.main(args)  // Without this, IDEA doesn’t see main 
}