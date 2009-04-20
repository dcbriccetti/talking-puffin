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
import util.{FetchRequest, ResourceReady, TextChangingAnimator}

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

class TweetDetailPanel(table: JTable, filtersDialog: FiltersDialog, streams: Streams) extends GridBagPanel {
    
  private val geoCoder = new GeoCoder(processFinishedGeocodes)
  private val animator = new TextChangingAnimator
  
  private var picLabel: Label = new Label {
    icon = Thumbnail.transparentMedium
  }
  private var userDescription: TextArea = _
  private var largeTweet: JTextPane = _
  private var bigPicFrame: Frame = _
  private var bigPicLabel: Label = _
  private var showingUrl: String = _
  private var showingUser: NodeSeq = _
  var geoEnabled = true
          
  private class CustomConstraints extends Constraints {
    gridy = 0; anchor = Anchor.SouthWest; insets = new Insets(0, 4, 0, 0)
  }
  
  largeTweet = new LargeTweet(filtersDialog, streams, table, background)
  
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
  
  def enableAnimation(enabled: Boolean) = animator.enabled = enabled
  
  private def setText(user: NodeSeq) {
    animator.stop
    showingUser = user
    val rawLocationOfShowingItem = userLoc(user)

    if (geoEnabled) {
      GeoCoder.extractLatLong(rawLocationOfShowingItem) match {
        case Some(latLong) =>
          geoCoder.getCachedObject(latLong) match {
            case Some(location) => {
              setText(user, location)
              return
            }
            case None => geoCoder.requestItem(new FetchRequest[String](latLong, user)) 
          }
          
        case None =>
      }
    }
    setText(user, rawLocationOfShowingItem)
  }
  
  private def setText(user: NodeSeq, location: String) {
    userDescription.text = (user \ "name").text + " • " +
        location + " • " + (user \ "description").text  + " • " +
        (user \ "followers_count").text + " followers"
  }

  private def processFinishedGeocodes(resourceReady: ResourceReady[String,String]): Unit = {
    if (resourceReady.id.equals(showingUser)) {
      animator.stop
      var origText = userLoc(showingUser)
      val newText = resourceReady.resource
      def callBack(text: String) {
        setText(showingUser, text)
      }
      animator.run(origText, newText, callBack)
      
    }
  }
  
  private def userLoc(user: NodeSeq) = (user \ "location").text
  
  private def urlFromUser(user: NodeSeq): String = (user \ "profile_image_url").text 
  
  def prefetch(status: NodeSeq) {
    val smallUrl = urlFromUser(status \ "user")
    val mediumUrl = PictureFetcher.getFullSizeUrl(smallUrl)
    List(smallUrl, mediumUrl).foreach(url => picFetcher.requestItem(picFetcher.FetchImageRequest(url, null)))
  }

  val picFetcher = new PictureFetcher(Some(Thumbnail.MEDIUM_SIZE), (imageReady: PictureFetcher.ImageReady) => {
    if (imageReady.key.equals(showingUrl)) {
      setPicLabelIconAndBigPic(imageReady.resource) 
    }
  })
  
  private def showMediumPicture(picUrl: String) {
    val fullSizeUrl = PictureFetcher.getFullSizeUrl(picUrl)
    if (! fullSizeUrl.equals(showingUrl)) {
      showingUrl = fullSizeUrl
      var icon = PictureFetcher.scaledPictureCache.get(fullSizeUrl)
      if (icon == null) {  
        picFetcher.requestItem(picFetcher.FetchImageRequest(fullSizeUrl, null))
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
    animator.stop
    showingUrl = null
    picLabel.icon = Thumbnail.transparentMedium
    userDescription.text = null
    largeTweet.setText(null)
  }

  private def setBigPicLabelIcon {
    if (bigPicFrame != null && bigPicLabel != null) { 
      bigPicLabel.icon = picFetcher.getCachedObject(PictureFetcher.getFullSizeUrl(showingUrl)) match {
        case Some(icon) => icon
        case None => null
      }
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
      override def keyPressed(e: KeyEvent) = closePicture
    })
    
  }
}

