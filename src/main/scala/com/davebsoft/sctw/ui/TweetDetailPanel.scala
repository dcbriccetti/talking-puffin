package com.davebsoft.sctw.ui

import _root_.com.davebsoft.sctw.twitter.StreamUtil
import _root_.scala.swing.event.{ButtonClicked}
import _root_.scala.swing.GridBagPanel._
import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.swing.{Frame, Label, GridBagPanel, ScrollPane, TextArea, BorderPanel}
import _root_.scala.xml.{XML, NodeSeq, Node}
import geo.GeoCoder
import java.awt.event.{MouseEvent, KeyAdapter, MouseAdapter, KeyEvent}
import java.awt.image.BufferedImage
import java.awt.{Dimension, Insets, Image}
import java.net.{HttpURLConnection, URI, URL}
import javax.swing.event.{ListSelectionEvent, ListSelectionListener}
import javax.swing.text.JTextComponent
import javax.swing.{ScrollPaneConstants, JTable, JTextPane, SwingWorker, ImageIcon, JScrollPane}
import org.apache.log4j.Logger
import util.{ShortUrl, FetchRequest, ResourceReady, TextChangingAnimator}
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
  private val log = Logger.getLogger("TweetDetailPanel")  
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
  
  val largeTweetScrollPane = new JScrollPane {
    val dim = new Dimension(500, 100)
    setMinimumSize(dim)
    setPreferredSize(dim)
    setViewportView(largeTweet)
    setBorder(null)
  }
  peer.add(largeTweetScrollPane, new Constraints {
    insets = new Insets(5,1,5,1)
    grid = (1,0); gridwidth=2; fill = GridBagPanel.Fill.Both;
  }.peer)

  picLabel.peer.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = if (showingUrl != null) showBigPicture
  })
  add(new BorderPanel {
    val s = new Dimension(Thumbnail.MEDIUM_SIZE + 6, Thumbnail.MEDIUM_SIZE + 6)
    minimumSize = s
    maximumSize = s
    preferredSize = s
    add(picLabel, BorderPanel.Position.Center)
  }, new CustomConstraints { grid = (0,0); gridheight = 3; insets = new Insets(3, 3, 3, 3)})

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
    Status.message.text = " "
    val user = status \ "user"
    setText(user)
    largeTweet.setText(HtmlFormatter.createTweetHtml((status \ "text").text, 
      (status \ "in_reply_to_status_id").text, (status \ "source").text))
    largeTweet setCaretPosition 0

    ShortUrl.substituteExpandedUrls((status \ "text").text, largeTweet)
    
    val picUrl = urlFromUser(user)
    showMediumPicture(picUrl)
  }
  
  def clearStatusDetails {
    Status.message.text = " "
    animator.stop
    showingUrl = null
    picLabel.icon = Thumbnail.transparentMedium
    userDescription.text = null
    largeTweet.setText(null)
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
    if (resourceReady.userData.equals(showingUser)) {
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
      setPicLabelIconAndBigPic(picFetcher.getCachedObject(fullSizeUrl) match {
        case Some(images) => images
        case None => 
          picFetcher.requestItem(picFetcher.FetchImageRequest(fullSizeUrl, null))
          ImageWithScaled(Thumbnail.transparentMedium, None)
      })
    }
  }
  
  private def setPicLabelIconAndBigPic(imageWithScaled: ImageWithScaled) {
    picLabel.icon = imageWithScaled.scaledImage match { case Some(icon) => icon case None => null} 
    setBigPicLabelIcon(Some(imageWithScaled.image))
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
    setBigPicLabelIcon(None)

    def closePicture {
      bigPicFrame.dispose
      bigPicFrame = null
      bigPicLabel = null
    }

    bigPicLabel.peer.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent) = closePicture
    })
    
    bigPicFrame.peer.addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent) = closePicture
    })
  }

  private def setBigPicLabelIcon(image: Option[ImageIcon]) {
    if (bigPicFrame != null && bigPicLabel != null) { 
      bigPicLabel.icon = image match {
        case Some(icon) => icon
        case None => 
          picFetcher.getCachedObject(PictureFetcher.getFullSizeUrl(showingUrl)) match {
            case Some(imageWithScaledImage) => imageWithScaledImage.image
            case None => null
          }
      }
      bigPicFrame.pack
    }
  }
}

