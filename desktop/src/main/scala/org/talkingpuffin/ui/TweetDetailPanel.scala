package org.talkingpuffin.ui

import _root_.scala.swing.{Label, GridBagPanel, TextArea, BorderPanel}
import _root_.scala.swing.GridBagPanel._
import java.awt.event.{MouseEvent, MouseAdapter}
import java.awt.image.BufferedImage
import java.awt.{Dimension, Insets, Font}
import java.text.NumberFormat
import java.util.prefs.Preferences
import javax.swing._
import event.{ListSelectionListener, ListSelectionEvent}
import org.talkingpuffin.twitter.{TwitterStatus,TwitterUser}
import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}
import org.talkingpuffin.geo.GeoCoder
import org.talkingpuffin.filter.TagUsers
import org.talkingpuffin.Session
import util.{TextChangingAnimator}
import org.talkingpuffin.util.{ResourceReady, FetchRequest, ShortUrl, Loggable}

object Thumbnail {
  val THUMBNAIL_SIZE = 48
  val transparentThumbnail = new ImageIcon(new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, 
    BufferedImage.TYPE_INT_ARGB))
  val MEDIUM_SIZE = 150
  val transparentMedium = new ImageIcon(new BufferedImage(MEDIUM_SIZE, MEDIUM_SIZE, 
    BufferedImage.TYPE_INT_ARGB))
}

object medThumbPicFetcher extends PictureFetcher("Medium thumb", Some(Thumbnail.MEDIUM_SIZE))

/**
 * Details of the currently-selected tweet.
 */
class TweetDetailPanel(session: Session, focusAfterHyperlinkClick: JComponent, 
    filtersDialog: Option[FiltersDialog], viewCreator: Option[ViewCreator]) 
    extends GridBagPanel with Loggable {
  private val animator = new TextChangingAnimator

  private var picLabel: Label = new Label {
    icon = Thumbnail.transparentMedium
  }

  private val bigPic = new BigPictureDisplayer(medThumbPicFetcher)
  private var userDescription: TextArea = _
  private var largeTweet = new LargeTweet(filtersDialog, viewCreator, focusAfterHyperlinkClick, background)
  private var showingUrl: String = _
  private var showingUser: TwitterUser = _
          
  private class CustomConstraints extends Constraints {
    gridy = 0; anchor = Anchor.SouthWest; insets = new Insets(0, 4, 0, 0)
  }
  
  val largeTweetScrollPane = new JScrollPane {
    val dim = new Dimension(500, 100)
    setMinimumSize(dim)
    setPreferredSize(dim)
    setViewportView(largeTweet)
    setBorder(BorderFactory.createEtchedBorder)
    setVisible(false)
  }
  peer.add(largeTweetScrollPane, new Constraints {
    insets = new Insets(5,1,5,1)
    grid = (1,0); gridwidth=2; fill = GridBagPanel.Fill.Both;
  }.peer)

  picLabel.peer.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = if (showingUrl != null) bigPic.showBigPicture(showingUrl, peer)
  })
  add(new BorderPanel {
    val s = new Dimension(Thumbnail.MEDIUM_SIZE + 6, Thumbnail.MEDIUM_SIZE + 6)
    minimumSize = s
    maximumSize = s
    preferredSize = s
    add(picLabel, BorderPanel.Position.Center)
  }, new CustomConstraints { grid = (0,0); gridheight = 3; insets = new Insets(3, 3, 3, 3)})

  class UserDescription extends TextArea {
    font = new Font("SansSerif", Font.PLAIN, 14)
    background = TweetDetailPanel.this.background
    lineWrap = true
    wordWrap = true
    editable = false
  }
  addFreshUserDescription
  
  /** Recreate the entire control as a defense against Hebrew characters which break the control */
  def addFreshUserDescription {
    if (userDescription != null) TweetDetailPanel.this.peer.remove(userDescription.peer)
    userDescription = new UserDescription
    add(userDescription, new CustomConstraints {
      grid = (1,1); gridheight=2; fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1;
    })
  }
  
  def connectToTable(table: JTable) {
    val model = table.getModel.asInstanceOf[UserAndStatusProvider]
    
    def prefetchAdjacentRows {        
      List(-1, 1).foreach(offset => {
        val adjacentRowIndex = table.getSelectedRow + offset
        if (adjacentRowIndex >= 0 && adjacentRowIndex < table.getRowCount) {
          prefetch(model.getUserAndStatusAt(
            table.convertRowIndexToModel(adjacentRowIndex))._1)
        }
      })
    }

    table.getSelectionModel.addListSelectionListener(new ListSelectionListener {
      def valueChanged(e: ListSelectionEvent) = {
        if (! e.getValueIsAdjusting) {
          if (table.getSelectedRowCount == 1) {
            try {
              val modelRowIndex = table.convertRowIndexToModel(table.getSelectedRow)
              val (user, status) = model.getUserAndStatusAt(modelRowIndex)
              showStatusDetails(user, status)
              prefetchAdjacentRows        
            } catch {
              case ex: IndexOutOfBoundsException => println(ex)
            }
          } else {
            clearStatusDetails
          }
        }
      }
    })
  }

  def showStatusDetails(user: TwitterUser, status: Option[TwitterStatus]) {
    session.statusMsg = " "
    setText(user, status)
    largeTweetScrollPane.setVisible(true)
    status match {
      case None => largeTweet.setText(null) 
      case Some(st) =>
        largeTweet.setText(HtmlFormatter.createTweetHtml(st.text,
          st.inReplyToStatusId, st.source))

        if (GlobalPrefs.isOn(PrefKeys.EXPAND_URLS)) {
          def replaceUrl(shortUrl: String, fullUrl: String) = {
            val beforeText = largeTweet.getText
            val afterText = beforeText.replace(shortUrl, fullUrl)
            if (beforeText != afterText) {
              largeTweet setText afterText
              largeTweet setCaretPosition 0
            }
          }
      
          ShortUrl.getExpandedUrls(st.text, replaceUrl)
        } 
    }
    largeTweet setCaretPosition 0

    showMediumPicture(user.profileImageURL)
  }
  
  def clearStatusDetails {
    session.statusMsg = " "
    animator.stop
    showingUrl = null
    picLabel.icon = Thumbnail.transparentMedium
    userDescription.text = null
    largeTweet.setText(null)
    largeTweetScrollPane.setVisible(false)
  }
  
  def showBigPicture = bigPic.showBigPicture(showingUrl, peer)

  private def setText(user: TwitterUser, statusOp: Option[TwitterStatus]) {
    animator.stop
    showingUser = user
    val rawLocationOfShowingItem = user.location

    statusOp match {
      case None =>
      case Some(status) =>
        if (GlobalPrefs.isOn(PrefKeys.LOOK_UP_LOCATIONS)) {
          (status.location match {
            case Some(location) => {
              val key = GeoCoder.formatLatLongKey(location)
              debug("New geo loc found: " + key)
              Some(key)
            }
            case None => GeoCoder.extractLatLong(rawLocationOfShowingItem)
          }) match {
            case Some(latLong) =>
              GeoCoder.getCachedObject(latLong) match {
                case Some(location) => {
                  setText(user, location)
                  return
                }
                case None => GeoCoder.requestItem(new FetchRequest[String,String](latLong, user,
                  processFinishedGeocodes))
              }
            case None =>
          }
        }
    }
    setText(user, rawLocationOfShowingItem)
  }
  
  private def setText(user: TwitterUser, location: String) {
    addFreshUserDescription

    def fmt(value: Int) = NumberFormat.getIntegerInstance.format(value)

    userDescription.text = UserProperties.overriddenUserName(session.userPrefs, user) + 
        " (" + user.screenName + ") • " +
        location + " • " + user.description  + " • " +
        fmt(user.followersCount) + " followers, following " +
        fmt(user.friendsCount) +
        (session.tagUsers.tagsForUser(user.id) match { 
          case Nil => "" 
          case tags => " • Tags: " + tags.mkString(", ")
        })
  }

  private def processFinishedGeocodes(resourceReady: ResourceReady[String,String]): Unit = 
    if (resourceReady.userData == showingUser) {
      animator.stop
      animator.run(showingUser.location, resourceReady.resource, 
          (text: String) => setText(showingUser, text))
    }
  
  private def processFinishedPicture(imageReady: PictureFetcher.ImageReady) = {
    if (imageReady.key.equals(showingUrl)) {
      setPicLabelIconAndBigPic(imageReady.resource) 
    }
  }
  
  def prefetch(user: TwitterUser) {
    val smallUrl = user.profileImageURL
    val mediumUrl = PictureFetcher.getFullSizeUrl(smallUrl)
    List(smallUrl, mediumUrl).foreach(url => medThumbPicFetcher.requestItem(
        medThumbPicFetcher.FetchImageRequest(url, null, processFinishedPicture)))
  }

  private def showMediumPicture(picUrl: String) {
    val fullSizeUrl = PictureFetcher.getFullSizeUrl(picUrl)
    if (! fullSizeUrl.equals(showingUrl)) {
      showingUrl = fullSizeUrl
      setPicLabelIconAndBigPic(medThumbPicFetcher.getCachedObject(fullSizeUrl) match {
        case Some(images) => images
        case None => 
          medThumbPicFetcher.requestItem(medThumbPicFetcher.FetchImageRequest(fullSizeUrl, null, processFinishedPicture))
          ImageWithScaled(Thumbnail.transparentMedium, None)
    })
  }
  }
  
  private def setPicLabelIconAndBigPic(imageWithScaled: ImageWithScaled) {
    picLabel.icon = imageWithScaled.scaledImage match { case Some(icon) => icon case None => null} 
    bigPic.setBigPicLabelIcon(Some(imageWithScaled.image), showingUrl, peer)
  }

}
