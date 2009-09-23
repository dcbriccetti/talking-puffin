package org.talkingpuffin.ui

import _root_.scala.swing.{Label, GridBagPanel, TextArea, BorderPanel}
import _root_.scala.swing.GridBagPanel._
import filter.TagUsers
import geo.GeoCoder
import java.awt.event.{MouseEvent, MouseAdapter}
import java.awt.image.BufferedImage
import java.awt.{Dimension, Insets, Font}
import java.text.NumberFormat
import java.util.prefs.Preferences
import javax.swing._
import state.{GlobalPrefs, PrefKeys}
import util.{ShortUrl, FetchRequest, ResourceReady, TextChangingAnimator}
import org.talkingpuffin.twitter.{TwitterStatus,TwitterUser}

object Thumbnail {
  val THUMBNAIL_SIZE = 48
  val transparentThumbnail = new ImageIcon(new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, 
    BufferedImage.TYPE_INT_ARGB))
  val MEDIUM_SIZE = 150
  val transparentMedium = new ImageIcon(new BufferedImage(MEDIUM_SIZE, MEDIUM_SIZE, 
    BufferedImage.TYPE_INT_ARGB))
}

/**
 * Details of the currently-selected tweet.
 */
class TweetDetailPanel(session: Session, table: JTable, 
    filtersDialog: FiltersDialog, tagUsers: TagUsers, viewCreator: ViewCreator, userPrefs: Preferences) 
    extends GridBagPanel {
  private val geoCoder = new GeoCoder(processFinishedGeocodes)
  private val animator = new TextChangingAnimator

  private var picLabel: Label = new Label {
    icon = Thumbnail.transparentMedium
  }
  val picFetcher = new PictureFetcher(Some(Thumbnail.MEDIUM_SIZE), (imageReady: PictureFetcher.ImageReady) => {
    if (imageReady.key.equals(showingUrl)) {
      setPicLabelIconAndBigPic(imageReady.resource) 
    }
  })

  private val bigPic = new BigPictureDisplayer(picFetcher)
  private var userDescription: TextArea = _
  private var largeTweet = new LargeTweet(filtersDialog, viewCreator, table, background)
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
  
  val statusTableModel = table.getModel.asInstanceOf[StatusTableModel]

  def showStatusDetails(status: TwitterStatus) {
    session.status.text = " "
    val user = status.user
    setText(user)
    largeTweetScrollPane.setVisible(true)
    largeTweet.setText(HtmlFormatter.createTweetHtml(status.text,
        status.inReplyToStatusId, status.source))
    largeTweet setCaretPosition 0

    if (GlobalPrefs.prefs.getBoolean(PrefKeys.EXPAND_URLS, false)) 
      ShortUrl.substituteExpandedUrls(status.text, largeTweet)
    
    showMediumPicture(user.profileImageURL)
  }
  
  def clearStatusDetails {
    session.status.text = " "
    animator.stop
    showingUrl = null
    picLabel.icon = Thumbnail.transparentMedium
    userDescription.text = null
    largeTweet.setText(null)
    largeTweetScrollPane.setVisible(false)
  }
  
  def showBigPicture = bigPic.showBigPicture(showingUrl, peer)

  private def setText(user: TwitterUser) {
    animator.stop
    showingUser = user
    val rawLocationOfShowingItem = user.location

    if (GlobalPrefs.prefs.getBoolean(PrefKeys.LOOK_UP_LOCATIONS, false)) { 
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
  
  private def setText(user: TwitterUser, location: String) {
    addFreshUserDescription
    def fmt(value: Int) = NumberFormat.getIntegerInstance.format(value)
    val tags = tagUsers.tagsForUser(user.id).mkString(", ")
    userDescription.text = UserProperties.overriddenUserName(userPrefs, user) + 
        " (" + user.screenName + ") • " +
        location + " • " + user.description  + " • " +
        fmt(user.followersCount) + " followers, following " +
        fmt(user.friendsCount) +
        (tags.length match { case 0 => "" case _ => " • Tags: " + tags})
  }

  private def processFinishedGeocodes(resourceReady: ResourceReady[String,String]): Unit = 
    if (resourceReady.userData.equals(showingUser)) {
      animator.stop
      animator.run(showingUser.location, resourceReady.resource, (text: String) => setText(showingUser, text))
    }
  
  def prefetch(status: TwitterStatus) {
    val smallUrl = status.user.profileImageURL
    val mediumUrl = PictureFetcher.getFullSizeUrl(smallUrl)
    List(smallUrl, mediumUrl).foreach(url => picFetcher.requestItem(picFetcher.FetchImageRequest(url, null)))
  }

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
    bigPic.setBigPicLabelIcon(Some(imageWithScaled.image), showingUrl, peer)
  }

}

