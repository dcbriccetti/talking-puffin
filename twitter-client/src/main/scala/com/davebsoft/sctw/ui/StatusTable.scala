package com.davebsoft.sctw.ui
import _root_.scala.swing.GridBagPanel._
import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.swing.event.ButtonClicked
import _root_.scala.xml.{NodeSeq, Node}

import java.awt.event.{MouseEvent, ActionEvent, MouseAdapter, ActionListener}
import java.awt.image.BufferedImage
import java.awt.{Color, Desktop, Dimension, Insets, Font}
import java.net.{URI, URL}
import java.util.Comparator
import javax.swing._
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableCellRenderer}
import scala.swing._
import filter.TagsRepository

/**
 * Table of statuses.
 * @author Dave Briccetti
 */

class StatusTable(statusTableModel: StatusTableModel, statusSelected: (NodeSeq) => Unit) 
    extends JTable(statusTableModel) {
  setRowSorter(new TableRowSorter[StatusTableModel](statusTableModel))
  
  val colModel = getColumnModel
  
  val ageCol = colModel.getColumn(0)
  ageCol.setPreferredWidth(60)
  ageCol.setMaxWidth(100)
  ageCol.setCellRenderer(new AgeCellRenderer)
  
  val nameCol = colModel.getColumn(1)
  nameCol.setPreferredWidth(100)
  nameCol.setMaxWidth(200)
  
  val statusCol = colModel.getColumn(2)
  statusCol.setPreferredWidth(600)
  statusCol.setCellRenderer(new StatusCellRenderer)

  addMouseListener(new PopupListener(this, getPopupMenu))
  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = {
      if (e.getClickCount == 2) {
        val status = statusTableModel.getStatusAt(convertRowIndexToModel(
          getSelectedRow))
        var uri = "http://twitter.com/" +
                (status \ "user" \ "screen_name").text + "/statuses/" +
                (status \ "id").text
        if (Desktop.isDesktopSupported) {
          Desktop.getDesktop.browse(new URI(uri))
        }
      }
    }
  })
  
  getSelectionModel.addListSelectionListener(new ListSelectionListener {
    def valueChanged(e: ListSelectionEvent) = {
      if (! e.getValueIsAdjusting) {
        if (getSelectedRowCount == 1) {
          showDetailsForTableRow(getSelectedRow)
        }
      }
    }
  })

  def getPopupMenu: JPopupMenu = {
    val menu = new JPopupMenu()

    val mi = new JMenuItem("Mute")
    mi.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        statusTableModel.muteSelectedUsers(getSelectedModelIndexes)
      }
    })
    menu.add(mi)

    val tagAl = new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        statusTableModel.tagSelectedUsers(getSelectedModelIndexes, e.getActionCommand)
      }
    }
    
    val tagMi = new JMenu("Tag Friend With")
    for (tag <- TagsRepository.get) {
      val tagSmi = new JMenuItem(tag)
      tagSmi.addActionListener(tagAl)
      tagMi.add(tagSmi)
    }
    menu.add(tagMi)

    menu
  }
  
  private def getSelectedModelIndexes: List[Int] = {
    val tableRows = getSelectedRows
    var smi = List[Int]()
    for (i <- 0 to (tableRows.length - 1)) {
      smi ::= convertRowIndexToModel(tableRows(i))
    }
    smi
  }
  
  private def showDetailsForTableRow(r: Int) {
    try {
      val modelRowIndex = convertRowIndexToModel(r)
      val status = statusTableModel.getStatusAt(modelRowIndex)
      statusSelected(status)
    } catch {
      case ex: IndexOutOfBoundsException => println(ex)
    }
  }
  
}