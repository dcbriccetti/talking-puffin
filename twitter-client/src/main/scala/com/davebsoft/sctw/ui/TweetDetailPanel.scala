package com.davebsoft.sctw.ui
import _root_.scala.swing.GridBagPanel._
import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.swing.event.ButtonClicked
import _root_.scala.xml.{NodeSeq, Node}

import java.awt.event.{MouseEvent, ActionEvent, MouseAdapter, ActionListener}
import java.awt.image.BufferedImage
import java.awt.{Color, Desktop, Dimension, Insets, Font}
import java.awt.event.{KeyEvent, KeyAdapter}
import java.net.{URI, URL}
import java.util.Comparator
import javax.swing.{JTable, JTextPane, JButton, JLabel, ImageIcon, Icon, SwingWorker, JMenu, JPopupMenu, JMenuItem, JToolBar}
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableCellRenderer}
import scala.swing._
import filter.TagsRepository

/**
 * Details of the currently-selected tweet.
 * @author Dave Briccetti
 */

class TweetDetailPanel(table: JTable, filtersPane: FiltersPane) extends GridBagPanel {
    
  var picLabel: Label = _
  var userDescription: TextArea = _
  var largeTweet: JTextPane = _
  private val THUMBNAIL_SIZE = 48
  val transparentPic = new ImageIcon(new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, 
    BufferedImage.TYPE_INT_ARGB))
  var bigPicFrame: Frame = _
  var bigPicLabel: Label = _
  var showingUrl: String = _

  private class CustomConstraints extends Constraints {
    gridy = 0; anchor = Anchor.SouthWest; insets = new Insets(0, 4, 0, 0)
  }
  
  largeTweet = new LargeTweet(filtersPane, table)
  //largeTweet.setBackground(StatusPane.this.background)
  
  peer.add(largeTweet, new Constraints{
    insets = new Insets(5,1,5,1)
    grid = (0,0); gridwidth=2; fill = GridBagPanel.Fill.Both;
  }.peer)

  picLabel = new Label
  picLabel.peer.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = {
      showBigPicture
    }
  })
  add(picLabel, new CustomConstraints {
    grid = (0,1); gridheight = 2;  
  })

  userDescription = new TextArea {
    background = TweetDetailPanel.this.background
    lineWrap = true
    wordWrap = true
    editable = false
  }
  add(userDescription, new CustomConstraints {
    grid = (1,1); gridheight=2; fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1;
  })
  
  val statusTableModel = table.getModel.asInstanceOf[StatusTableModel]

  table.getSelectionModel.addListSelectionListener(new ListSelectionListener {
    def valueChanged(e: ListSelectionEvent) = {
      if (! e.getValueIsAdjusting) {
        if (table.getSelectedRowCount == 1) {
          try {
            val modelRowIndex = table.convertRowIndexToModel(table.getSelectedRow)
            val status = statusTableModel.getStatusAt(modelRowIndex)
            showStatusDetails(status)
          } catch {
            case ex: IndexOutOfBoundsException => println(ex)
          }
        } else {
          clearStatusDetails
        }
      }
    }
  })
  
  private def showStatusDetails(status: NodeSeq) {
    val user = status \ "user"
    userDescription.text = (user \ "name").text + " • " +
            (user \ "location").text + " • " + (user \ "description").text
    largeTweet.setText(HtmlFormatter.createTweetHtml((status \ "text").text, 
      (status \ "in_reply_to_status_id").text)) 
    val picUrl = (user \ "profile_image_url").text
    if (! picUrl.equals(showingUrl)) {
      showingUrl = picUrl
      val u = new URL(picUrl)
      picLabel.icon = transparentPic
      new SwingWorker[Icon, Object] {
        val urlToShow = showingUrl
        def doInBackground = new ImageIcon(u)
        override def done = {
          if (urlToShow == showingUrl) { // If user is moving quickly there may be several threads
            val icon = get
            if (icon.getIconHeight <= THUMBNAIL_SIZE) picLabel.icon = icon // Ignore broken, too-big thumbnails 
            setBigPicLabelIcon
          }
        }
      }.execute
    }
  }

  def clearStatusDetails {
    picLabel.icon = null
    userDescription.text = null
    largeTweet.setText(null)
  }

  private def setBigPicLabelIcon {
    if (bigPicFrame != null && bigPicLabel != null) { 
      bigPicLabel.icon = new ImageIcon(new URL(showingUrl.replace("_normal", "")))
      bigPicFrame.pack
    }
  }

  def showBigPicture {
    bigPicLabel = new Label
    if (bigPicFrame != null) {
      bigPicFrame.dispose
    }
    bigPicFrame = new Frame {
      contents = bigPicLabel
      peer.setLocationRelativeTo(picLabel.peer)
      visible = true
    }
    setBigPicLabelIcon
    bigPicLabel.peer.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent) = {
        bigPicFrame.dispose
        bigPicFrame = null
        bigPicLabel = null
      }
    })
  }
  
}
