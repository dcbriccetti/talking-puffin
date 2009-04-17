package com.davebsoft.sctw.ui

import _root_.com.davebsoft.sctw.twitter.StreamUtil
import _root_.scala.swing.event.{ButtonClicked}
import _root_.scala.swing.GridBagPanel._
import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.swing.{Frame, Label, GridBagPanel, TextArea, BorderPanel}
import _root_.scala.xml.{XML, NodeSeq, Node}
import geo.GeoCoder
import java.awt.event.{MouseEvent, KeyAdapter, MouseAdapter, KeyEvent}
import java.awt.image.BufferedImage
import java.awt.{Dimension, Insets, Image}
import java.net.{URI, URL}
import javax.swing.event.{ListSelectionEvent, ListSelectionListener}
import javax.swing.{JTable, JTextPane, ImageIcon}

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

class TweetDetailPanel(table: JTable, filtersDialog: FiltersDialog) extends GridBagPanel {
    
  var picLabel: Label = new Label {
    icon = Thumbnail.transparentMedium
  }
  var userDescription: TextArea = _
  var largeTweet: JTextPane = _
  var bigPicFrame: Frame = _
  var bigPicLabel: Label = _
  var showingUrl: String = _
  var geoEnabled = true
          
  private class CustomConstraints extends Constraints {
    gridy = 0; anchor = Anchor.SouthWest; insets = new Insets(0, 4, 0, 0)
  }
  
  largeTweet = new LargeTweet(filtersDialog, table, background)
  
  peer.add(largeTweet, new Constraints{
    insets = new Insets(5,1,5,1)
    grid = (1,0); gridwidth=2; fill = GridBagPanel.Fill.Both;
  }.peer)

  picLabel.peer.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = {
      if (showingUrl != null) showBigPicture
    }
  })
  add(new BorderPanel {
    val s = new Dimension(Thumbnail.MEDIUM_SIZE + 6, Thumbnail.MEDIUM_SIZE + 6)
    minimumSize = s
    maximumSize = s
    preferredSize = s
    add(picLabel, BorderPanel.Position.Center)
  }, new CustomConstraints {
    grid = (0,0); gridheight = 3; insets = new Insets(3, 3, 3, 3)  
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

  def showStatusDetails(status: NodeSeq) {
    val user = status \ "user"
    setText(user)
    largeTweet.setText(HtmlFormatter.createTweetHtml((status \ "text").text, 
      (status \ "in_reply_to_status_id").text, (status \ "source").text))
    val picUrl = urlFromUser(user)
    showMediumPicture(picUrl)
  }
  
  private def setText(user: NodeSeq) {
    val rawLocation = (user \ "location").text
    var location = if (geoEnabled) GeoCoder.decode(rawLocation) else rawLocation
    userDescription.text = (user \ "name").text + " • " +
        location + " • " + (user \ "description").text  + " • " +
        (user \ "followers_count").text + " followers"
  }
  
  private def urlFromUser(user: NodeSeq): String = (user \ "profile_image_url").text 
  
  def prefetch(status: NodeSeq) {
    val smallUrl = urlFromUser(status \ "user")
    val mediumUrl = PictureFetcher.getFullSizeUrl(smallUrl)
    List(smallUrl, mediumUrl).foreach(url => picFetcher.requestImage(url, null))
  }

  val picFetcher = new PictureFetcher(Some(Thumbnail.MEDIUM_SIZE), (imageReady: ImageReady) => {
    if (imageReady.url.equals(showingUrl)) {
      setPicLabelIconAndBigPic(imageReady.imageIcon) 
    }
  }, false)
  
  private def showMediumPicture(picUrl: String) {
    val fullSizeUrl = PictureFetcher.getFullSizeUrl(picUrl)
    if (! fullSizeUrl.equals(showingUrl)) {
      showingUrl = fullSizeUrl
      var icon = PictureFetcher.scaledPictureCache.get(fullSizeUrl)
      if (icon == null) {  
        picFetcher.requestImage(fullSizeUrl, null)
        icon = Thumbnail.transparentMedium
      }
      setPicLabelIconAndBigPic(icon)
    }
  }
  
  private def setPicLabelIconAndBigPic(icon: ImageIcon) {
    picLabel.icon = icon 
    setBigPicLabelIcon
  }

  def clearStatusDetails {
    showingUrl = null
    picLabel.icon = Thumbnail.transparentMedium
    userDescription.text = null
    largeTweet.setText(null)
  }

  private def setBigPicLabelIcon {
    if (bigPicFrame != null && bigPicLabel != null) { 
      bigPicLabel.icon = PictureFetcher.pictureCache.get(PictureFetcher.getFullSizeUrl(showingUrl))
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

    def closePicture {
      bigPicFrame.dispose
      bigPicFrame = null
      bigPicLabel = null
    }

    bigPicLabel.peer.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent) = {
        closePicture
      }
    })
    
    bigPicFrame.peer.addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent) = if (e.getKeyCode == KeyEvent.VK_ESCAPE) closePicture
    })
    
  }
  
}
