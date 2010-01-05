package org.talkingpuffin.ui

import java.awt.event.{KeyAdapter, KeyEvent}
import swing.{Label}
import javax.swing._
import org.talkingpuffin.Session

class BigPictureDisplayer(picFetcher: PictureFetcher, session: Session) {
  private var bigPicFrame: JInternalFrame = _
  private var bigPicLabel: Label = _

  def showBigPicture(showingUrl: String, frameDescendant: java.awt.Component) {
    bigPicLabel = new Label
    if (bigPicFrame != null) {
      bigPicFrame.dispose
    }
    bigPicFrame = new JInternalFrame("Large Picture", false,true, false, false) {
      setLayer(10)
      add(bigPicLabel.peer)
    }
    session.desktopPane.add(bigPicFrame)
    setBigPicLabelIcon(None, showingUrl, frameDescendant)

    def closePicture {
      bigPicFrame.dispose
      bigPicFrame = null
      bigPicLabel = null
    }

    bigPicFrame.addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent) = closePicture
    })
  }

  def setBigPicLabelIcon(image: Option[ImageIcon], showingUrl: String, frameDescendant: java.awt.Component) {
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
      bigPicFrame.setVisible(true)
    }
  }
  
}

