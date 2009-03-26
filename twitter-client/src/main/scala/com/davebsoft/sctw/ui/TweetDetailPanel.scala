package com.davebsoft.sctw.ui
import _root_.scala.swing.event.{ButtonClicked}
import _root_.scala.swing.GridBagPanel._
import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.xml.{NodeSeq, Node}

import java.awt.event.{MouseEvent, MouseAdapter}
import java.awt.image.BufferedImage
import java.awt.{Dimension, Insets, Image}
import java.net.{URI, URL}
import javax.swing._
import javax.swing.event.{ListSelectionEvent, ListSelectionListener}
import scala.swing._

/**
 * Details of the currently-selected tweet.
 * @author Dave Briccetti
 */

object Thumbnail {
  val THUMBNAIL_SIZE = 48
  val transparentThumbnail = new ImageIcon(new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, 
    BufferedImage.TYPE_INT_ARGB))
  val MEDIUM_SIZE = 150
  val transparentMedium = new ImageIcon(new BufferedImage(MEDIUM_SIZE, MEDIUM_SIZE, 
    BufferedImage.TYPE_INT_ARGB))
}

class TweetDetailPanel(table: JTable, filtersPane: FiltersPane) extends GridBagPanel {
    
  var picLabel: Label = new Label {
    icon = Thumbnail.transparentMedium
  }
  var userDescription: TextArea = _
  var largeTweet: JTextPane = _
  var bigPicFrame: Frame = _
  var bigPicLabel: Label = _
  var showingUrl: String = _
          
  private class CustomConstraints extends Constraints {
    gridy = 0; anchor = Anchor.SouthWest; insets = new Insets(0, 4, 0, 0)
  }
  
  largeTweet = new LargeTweet(filtersPane, table)
  
  peer.add(largeTweet, new Constraints{
    insets = new Insets(5,1,5,1)
    grid = (1,0); gridwidth=2; fill = GridBagPanel.Fill.Both;
  }.peer)

  picLabel.peer.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = {
      showBigPicture
    }
  })
  add(new BorderPanel {
    val s = new Dimension(Thumbnail.MEDIUM_SIZE, Thumbnail.MEDIUM_SIZE)
    minimumSize = s
    maximumSize = s
    preferredSize = s
    add(picLabel, BorderPanel.Position.Center)
  }, new CustomConstraints {
    grid = (0,0); gridheight = 3;  
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
        (user \ "location").text + " • " + (user \ "description").text  + " • " +
        (user \ "followers_count").text + " followers"
    largeTweet.setText(HtmlFormatter.createTweetHtml((status \ "text").text, 
      (status \ "in_reply_to_status_id").text, (status \ "source").text))
    val picUrl = (user \ "profile_image_url").text
    showSmallPicture(picUrl)
  }

  val picFetcher = new PictureFetcher((imageReady: ImageReady) => {
    //if (imageReady.url == showingUrl) {
      //if (imageReady.imageIcon.getIconHeight <= Thumbnail.THUMBNAIL_SIZE) 
        picLabel.icon = scaleImage(imageReady.imageIcon) // Ignore broken, too-big thumbnails 
      setBigPicLabelIcon
    //}
  }, false)
  
  private def scaleImage(imageIcon: ImageIcon): ImageIcon = {
    val image = imageIcon.getImage
    val w = image.getWidth(null)
    val h = image.getHeight(null)
    val newW: Int = if (w > h) Math.min(w, Thumbnail.MEDIUM_SIZE) else -1
    val newH: Int = if (w > h) -1 else Math.min(h, Thumbnail.MEDIUM_SIZE)
    new ImageIcon(image.getScaledInstance(newW, newH, Image.SCALE_DEFAULT))
  }

  private def showSmallPicture(picUrl: String) {
    if (! picUrl.equals(showingUrl)) {
      showingUrl = picUrl
      picLabel.icon = Thumbnail.transparentMedium
      picFetcher ! new FetchImage(picUrl.replace("_normal", ""), null)
    }
  }

  def clearStatusDetails {
    showingUrl = null
    picLabel.icon = Thumbnail.transparentMedium
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
