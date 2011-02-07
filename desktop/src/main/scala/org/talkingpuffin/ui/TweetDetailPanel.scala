package org.talkingpuffin.ui

import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import java.awt.event.{MouseEvent, MouseAdapter}
import java.text.NumberFormat
import javax.swing.{JScrollPane, BorderFactory, JTable}
import java.awt.{Color, Dimension, Insets, Font}
import scala.swing.{Label, GridBagPanel, TextArea}
import scala.swing.GridBagPanel._
import scala.swing.GridBagPanel.Anchor._
import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}
import org.talkingpuffin.geo.GeoCoder
import org.talkingpuffin.Session
import org.talkingpuffin.ui.filter.FiltersDialog
import util.{Activateable, CenteredPicture, TextChangingAnimator}
import org.talkingpuffin.util._
import twitter4j.{User, Status}

object medThumbPicFetcher extends PictureFetcher("Medium thumb", Some(Thumbnail.MEDIUM_SIZE))

/**
 * Details of the currently-selected tweet.
 */
class TweetDetailPanel(session: Session,  
    filtersDialog: Option[FiltersDialog]) extends GridBagPanel with Loggable {
  
  preferredSize = new Dimension(600, 380)
  border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
  private val animator = new TextChangingAnimator

  private var picLabel: Label = new Label {
    icon = Thumbnail.transparentMedium
  }

  private val bigPic = new BigPictureDisplayer(medThumbPicFetcher, session)
  private var userDescription: TextArea = _
  private var largeTweet = new LargeTweet(session, background, getActivateable _)
  private var showingUrl: String = _
  private var showingUser: User = _
          
  val largeTweetScrollPane = new JScrollPane {
    val dim = new Dimension(600, 140); setMinimumSize(dim); setPreferredSize(dim)
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
    override def mouseClicked(e: MouseEvent) = if (showingUrl != null) bigPic.showBigPicture(showingUrl, peer)
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
  addUserDescription
  
  var userDescScrollPane: JScrollPane = _
  private var currentActivateable: Option[Activateable] = None
  def getActivateable = currentActivateable
  
  def connectToTable(activateable: Activateable, filtersDialog: Option[FiltersDialog]) {
    val table = activateable.asInstanceOf[JTable]
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
          if (activateable.isActive && table.getSelectedRowCount == 1) {
            try {
              val modelRowIndex = table.convertRowIndexToModel(table.getSelectedRow)
              val (user, retweetedUser, status) = model.getUserAndStatusAt(modelRowIndex)
              currentActivateable = Some(activateable)
              showStatusDetails(user, retweetedUser, status, filtersDialog)
              prefetchAdjacentRows        
            } catch {
              case ex: IndexOutOfBoundsException => println(ex)
            }
          } else {
            clearStatusDetails
            currentActivateable = None
          }
        }
      }
    })
  }

  private def addUserDescription {
    userDescription = new UserDescription
    userDescScrollPane = new JScrollPane {
      val dim = new Dimension(400, Thumbnail.MEDIUM_SIZE); setMinimumSize(dim); setPreferredSize(dim)
      setViewportView(userDescription.peer)
      setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4))
      setVisible(false)
    }
    peer.add(userDescScrollPane, new Constraints {
      anchor = SouthWest; insets = new Insets(0, 8, 0, 0); 
      grid = (1,1); fill = GridBagPanel.Fill.Horizontal; weightx = 1; 
    }.peer)
  }
  
  private def getFiltersDialog: Option[FiltersDialog] = None
  
  private def showStatusDetails(topUser: User, retweetedUser: Option[User],
      status: Option[Status], filtersDialog: Option[FiltersDialog]) {
    val user = if (retweetedUser.isDefined) retweetedUser.get else topUser
    session.clearMessage()
    setText(user, status)
    largeTweetScrollPane.setVisible(true)
    userDescScrollPane.setVisible(true)
    picture.visible = true
    status match {
      case None => largeTweet.setText(null) 
      case Some(topStatus) =>
        val st = topStatus //todo .retweetOrTweet
        largeTweet.filtersDialog = filtersDialog
        largeTweet.setText(HtmlFormatter.createTweetHtml(st.getText,
          Some(st.getInReplyToStatusId), st.getSource, if (retweetedUser.isDefined) Some(topUser) else None))

        if (GlobalPrefs.isOn(PrefKeys.EXPAND_URLS)) {
          def replaceUrl(shortUrl: String, fullUrl: String) = {
            val beforeText = largeTweet.getText
            val afterText = beforeText.replace(shortUrl, fullUrl)
            if (beforeText != afterText) {
              largeTweet setText afterText
              largeTweet setCaretPosition 0
            }
          }
      
          ShortUrl.getExpandedUrls(st.getText, replaceUrl)
        } 
    }
    largeTweet setCaretPosition 0

    showMediumPicture(user.getProfileImageURL.toString)
  }
  
  def clearStatusDetails {
    session.clearMessage()
    animator.stop
    showingUrl = null
    picLabel.icon = Thumbnail.transparentMedium
    userDescription.text = null
    largeTweet.setText(null)
    largeTweetScrollPane.setVisible(false)
    userDescScrollPane.setVisible(false)
    picture.visible = false
  }
  
  def showBigPicture = bigPic.showBigPicture(showingUrl, peer)

  private def setText(user: User, statusOp: Option[Status]) {
    animator.stop
    showingUser = user
    val rawLocationOfShowingItem = user.getLocation

    statusOp match {
      case None =>
      case Some(topStatus) =>
        val status = topStatus //todo .retweetOrTweet
        if (GlobalPrefs.isOn(PrefKeys.LOOK_UP_LOCATIONS)) {
          (status.getGeoLocation match {
            case null => GeoCoder.extractLatLong(rawLocationOfShowingItem)
            case location => {
              val key = GeoCoder.formatLatLongKey(String.valueOf(location.getLatitude),
                String.valueOf(location.getLongitude))
              Some(key)
            }
          }) match {
            case Some(latLong) =>
              GeoCoder.getCachedObject(latLong) match {
                case Some(location) => {
                  setText(user, location)
                  return
                }
                case None =>
                  GeoCoder.requestItem(new FetchRequest[String,String](latLong, user,
                    processFinishedGeocodes))
              }
            case None =>
          }
        }
    }
    setText(user, rawLocationOfShowingItem)
  }
  
  private def setText(user: User, location: String) {
    def fmt(value: Int) = NumberFormat.getIntegerInstance.format(value)

    userDescription.text = UserProperties.overriddenUserName(session.userPrefs, user) + 
        " (" + user.getScreenName + ")\n" +
        location + "\n\n" + 
        user.getDescription + "\n\n" +
        fmt(user.getFollowersCount) + " followers, following " +
        fmt(user.getFriendsCount) +
        (session.tagUsers.tagsForUser(user.getId) match {
          case Nil => "" 
          case tags => "\n\nTags: " + tags.mkString(", ")
        })
  }

  private def processFinishedGeocodes(resourceReady: ResourceReady[String,String]): Unit = {
    if (resourceReady.userData == showingUser) {
      animator.stop
      animator.run(showingUser.getLocation, resourceReady.resource,
          (text: String) => setText(showingUser, text))
    }
  }
  
  private def processFinishedPicture(imageReady: PictureFetcher.ImageReady) = {
    if (imageReady.key.equals(showingUrl)) {
      setPicLabelIconAndBigPic(imageReady.resource) 
    }
  }
  
  def prefetch(user: User) {
    val smallUrl = user.getProfileImageURL.toString
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
