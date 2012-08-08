package org.talkingpuffin.ui

import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import java.awt.event.{MouseEvent, MouseAdapter}
import java.text.NumberFormat
import javax.swing.{JScrollPane, BorderFactory, JTable}
import java.awt.{Color, Dimension, Insets, Font}
import scala.swing.{Label, GridBagPanel, TextArea}
import scala.swing.GridBagPanel.Anchor._
import twitter4j.{User, Status}
import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}
import org.talkingpuffin.geo.GeoCoder
import org.talkingpuffin.Session
import org.talkingpuffin.ui.filter.FiltersDialog
import org.talkingpuffin.util._
import org.talkingpuffin.apix.RichStatus._
import org.talkingpuffin.apix.RichUser._
import org.talkingpuffin.util.EscapeHtml
import util.{Activateable, CenteredPicture, TextChangingAnimator}
import java.net.URL

object medThumbPicFetcher extends PictureFetcher("Medium thumb", Some(Thumbnail.MEDIUM_SIZE), 2, Some(5))

/**
 * Details of the currently-selected tweet.
 */
class TweetDetailPanel(session: Session,  
    filtersDialog: Option[FiltersDialog]) extends GridBagPanel with Loggable {
  
  preferredSize = new Dimension(600, 380)
  minimumSize = preferredSize
  border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
  private val animator = new TextChangingAnimator

  private var picLabel: Label = new Label {
    icon = Thumbnail.transparentMedium
  }

  private val bigPic = new BigPictureDisplayer(medThumbPicFetcher)
  private var userDescription: Option[TextArea] = None
  private var largeTweet = new LargeTweet(session, background, getActivateable _)
  private var showingUrl: Option[String] = None
  private var showingUser: Option[User] = None
          
  val largeTweetScrollPane = new JScrollPane {
    val dim = new Dimension(600, 140)
    setMinimumSize(dim)
    setPreferredSize(dim)
    setViewportView(largeTweet)
    setBorder(null)
    setBackground(Color.WHITE)
    setVisible(false)
  }
  peer.add(largeTweetScrollPane, new Constraints {
    insets = new Insets(5,1,5,1)
    grid = (0,0); gridwidth = 2; fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1 
  }.peer)

  picLabel.peer.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) {
      showingUrl.foreach(bigPic.showBigPicture(_, peer))
    }
  })
  val picture = new CenteredPicture(picLabel) {visible = false}
  add(picture, new Constraints { anchor = SouthWest; grid = (0,1)})

  class UserDescription extends TextArea {
    font = new Font("Georgia", Font.PLAIN, 16)
    background = Color.WHITE
    lineWrap = true
    wordWrap = true
    editable = false
  }
  addUserDescription()
  
  var userDescScrollPane: JScrollPane = _
  private var currentActivateable: Option[Activateable] = None
  def getActivateable = currentActivateable
  
  def connectToTable(activateable: Activateable, filtersDialog: Option[FiltersDialog]) {
    val table = activateable.asInstanceOf[JTable]
    val model = table.getModel.asInstanceOf[UserAndStatusProvider]
    
    def prefetchAdjacentRows() {
      List(-1, 1).foreach(offset => {
        val adjacentRowIndex = table.getSelectedRow + offset
        if (adjacentRowIndex >= 0 && adjacentRowIndex < table.getRowCount) {
          prefetch(model.getUserAndStatusAt(
            table.convertRowIndexToModel(adjacentRowIndex)).user)
        }
      })
    }

    table.getSelectionModel.addListSelectionListener(new ListSelectionListener {
      def valueChanged(e: ListSelectionEvent) {
        if (! e.getValueIsAdjusting) {
          if (activateable.isActive && table.getSelectedRowCount == 1) {
            try {
              val modelRowIndex = table.convertRowIndexToModel(table.getSelectedRow)
              val userAndStatus = model.getUserAndStatusAt(modelRowIndex)
              currentActivateable = Some(activateable)
              showStatusDetails(userAndStatus, filtersDialog)
              prefetchAdjacentRows()
            } catch {
              case ex: IndexOutOfBoundsException => println(ex)
            }
          } else {
            clearStatusDetails()
            currentActivateable = None
          }
        }
      }
    })
  }

  private def addUserDescription() {
    val newUd = new UserDescription
    userDescription = Some(newUd)
    userDescScrollPane = new JScrollPane {
      val dim = new Dimension(400, Thumbnail.MEDIUM_SIZE); setMinimumSize(dim); setPreferredSize(dim)
      setViewportView(newUd.peer)
      setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4))
      setVisible(false)
    }
    peer.add(userDescScrollPane, new Constraints {
      anchor = SouthWest; insets = new Insets(0, 8, 0, 0)
      grid = (1,1); fill = GridBagPanel.Fill.Horizontal; weightx = 1
    }.peer)
  }
  
  private def showStatusDetails(userAndStatus: UserAndStatus, filtersDialog: Option[FiltersDialog]) {
    session.clearMessage()
    setText(userAndStatus.origUser, userAndStatus.status)
    largeTweetScrollPane.setVisible(true)
    userDescScrollPane.setVisible(true)
    picture.visible = true
    userAndStatus.status match {
      case None => largeTweet.setText(null) 
      case Some(topStatus) =>
        val st = topStatus.retweetOrTweet
        largeTweet.filtersDialog = filtersDialog
        largeTweet.setText(HtmlFormatter.createTweetHtml(EscapeHtml(st.getText), st.inReplyToStatusId, st.source,
            userAndStatus.retweetingUser))

        if (GlobalPrefs.isOn(PrefKeys.EXPAND_URLS)) {
          def formatUrl(url: String) = {
            try {
              val u = new URL(url)
              //TODO make a way to show a short version of the ultimate URL while preserving the hyperlink
              u.getHost + u.getPath
              url
            } catch {
              case e: Exception => url
            }
          }

          def replaceUrl(shortUrl: String, fullUrl: String) {
            val beforeText = largeTweet.getText
            val afterText = beforeText.replace(shortUrl, formatUrl(fullUrl))
            if (beforeText != afterText) {
              largeTweet setText afterText
              largeTweet setCaretPosition 0
            }
          }
      
          ShortUrl.expandUrls(st.getText, replaceUrl)
        } 
    }
    largeTweet setCaretPosition 0

    showMediumPicture(userAndStatus.origUser.getProfileImageURL.toString)
  }
  
  def clearStatusDetails() {
    session.clearMessage()
    animator.stop()
    showingUrl = None
    picLabel.icon = Thumbnail.transparentMedium
    userDescription.foreach(_.text = null)
    largeTweet.setText(null)
    largeTweetScrollPane.setVisible(false)
    userDescScrollPane.setVisible(false)
    picture.visible = false
  }
  
  def showBigPicture() {
    showingUrl.foreach(bigPic.showBigPicture(_, peer))
  }

  private def setText(user: User, opTopStatus: Option[Status]) {
    animator.stop()
    showingUser = Some(user)
    val rawLocationOfShowingItem = user.location

    if (GlobalPrefs.isOn(PrefKeys.LOOK_UP_LOCATIONS)) {
      opTopStatus.foreach(topStatus => {
        val opLatLong = Option(topStatus.retweetOrTweet.getGeoLocation).map(loc =>
          Some(GeoCoder.formatLatLongKey(loc.getLatitude.toString, loc.getLongitude.toString))).getOrElse(
          GeoCoder.extractLatLong(rawLocationOfShowingItem))
        opLatLong.foreach(latLong => {
          GeoCoder.getCachedObject(latLong).foreach(location => {
            setUserDescriptionText(user, location)
            return
          })
          GeoCoder.requestItem(FetchRequest[String](latLong, user, processFinishedGeocodes))
        })
      })
    }
    setUserDescriptionText(user, rawLocationOfShowingItem)
  }
  
  private def setUserDescriptionText(user: User, location: String) {
    def fmt(value: Int) = NumberFormat.getIntegerInstance.format(value)

    userDescription.foreach(_.text = UserProperties.overriddenUserName(session.userPrefs, user) +
      " (" + user.getScreenName + ")\n" +
      location + "\n\n" +
      user.description  + "\n\n" +
      fmt(user.getFollowersCount) + " followers, following " +
      fmt(user.getFriendsCount) +
      (session.tagUsers.tagsForUser(user.getId) match {
        case Nil => ""
        case tags => "\n\nTags: " + tags.mkString(", ")
      }))
  }

  private def processFinishedGeocodes(resourceReady: ResourceReady[String]) {
    showingUser.filter(_ == resourceReady.request.userData).foreach(user =>
      SwingInvoke.later {
        animator.stop()
        animator.run(user.location, resourceReady.resource, (text: String) => setUserDescriptionText(user, text))
      }
    )
  }
  
  private def processFinishedPicture(imageReady: PictureFetcher.ImageReady) {
    showingUrl.foreach(url =>
      SwingInvoke.later {
        if (imageReady.request.key.equals(url))
          setPicLabelIconAndBigPic(imageReady.resource)
      }
    )
  }

  def prefetch(user: User) {
    val smallUrl = user.getProfileImageURL.toString
    val mediumUrl = PictureFetcher.getFullSizeUrl(smallUrl)
    List(smallUrl, mediumUrl).foreach(url =>
      medThumbPicFetcher.requestItem(FetchImageRequest(url, null, processFinishedPicture)))
  }

  private def showMediumPicture(picUrl: String) {
    val fullSizeUrl = PictureFetcher.getFullSizeUrl(picUrl)
    if (! showingUrl.exists(_ == fullSizeUrl)) {
      showingUrl = Some(fullSizeUrl)
      setPicLabelIconAndBigPic(medThumbPicFetcher.getCachedObject(fullSizeUrl) match {
        case Some(images) => images
        case None => 
          medThumbPicFetcher.requestItem(FetchImageRequest(fullSizeUrl, null, processFinishedPicture))
          ImageWithScaled(Thumbnail.transparentMedium, None)
    })}
  }
  
  private def setPicLabelIconAndBigPic(imageWithScaled: ImageWithScaled) {
    picLabel.icon = imageWithScaled.scaledImage.getOrElse(null)
    showingUrl.foreach(bigPic.setBigPicLabelIcon(Some(imageWithScaled.image), _, peer))
  }
}
