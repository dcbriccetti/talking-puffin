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
  val method = new GetMethod("http://twitter.com/statuses/public_timeline.xml")
  
  /** How often, in ms, to fetch and load new data */
  private final var RELOAD_INTERVAL = 10000;

  loadTwitterData

  /**
   * Creates the Swing frame, which consists of a JTable inside a JScrollPane.
   */
  def top = new MainFrame {
    title = "Scala Twitter Client"
    val scrollPane = new ScrollPane {
      preferredSize = new Dimension(600,600)
      contents = new Table() {
        model = StatusTableModel
      }
    }
    contents = scrollPane
    peer.setLocationRelativeTo(null)

    continuallyLoadData(this)
  }

  /**
   * Reloads the data periodically
   */
  private def continuallyLoadData(container: Container) {
    new Timer(RELOAD_INTERVAL, new ActionListener() {
      def actionPerformed(event: ActionEvent) {
        loadTwitterData
        StatusTableModel.fireTableDataChanged
      }
    }).start
  }

  /**
   * Fetches statuses from Twitter and stores them in statuses field.
   */
  private def loadTwitterData() {
    val result = httpClient.executeMethod(method)
    val timeline = XML.load(method.getResponseBodyAsStream())
    statuses.clear
    for (st <- timeline \\ "status") {
      statuses.add(st)
    }
  }

  /**
   * Model providing data to the JTable
   */
  private object StatusTableModel extends AbstractTableModel {
    val colNames = List("Name", "Status")

    def getColumnCount = 2
    def getRowCount = statuses.size
    override def getColumnName(column: Int) = colNames(column)

    override def getValueAt(rowIndex: Int, columnIndex: Int) = {
      val status = statuses.get(rowIndex)
      val node = if (columnIndex == 0) (status \ "user" \ "name") else (status \ "text")
      node.text
    }
  }
  
  override def main(args: Array[String]): Unit = super.main(args)  // Without this, IDEA doesn’t see main 
}