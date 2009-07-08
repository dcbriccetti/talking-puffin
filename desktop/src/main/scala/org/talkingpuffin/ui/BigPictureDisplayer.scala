package org.talkingpuffin.ui

import java.awt.event.{MouseAdapter, MouseEvent, KeyAdapter, KeyEvent}
import javax.swing.{JFrame, SwingUtilities, ImageIcon}
import swing.{Label, Frame}

class BigPictureDisplayer(picFetcher: PictureFetcher) {
  private var bigPicFrame: Frame = _
  private var bigPicLabel: Label = _

  def showBigPicture(showingUrl: String, frameDescendant: java.awt.Component) {
    bigPicLabel = new Label
    if (bigPicFrame != null) {
      bigPicFrame.dispose
    }
    bigPicFrame = new Frame {
      contents = bigPicLabel
    }
    setBigPicLabelIcon(None, showingUrl, frameDescendant)

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
      bigPicFrame.peer.setLocationRelativeTo(
        SwingUtilities.getAncestorOfClass(classOf[JFrame], frameDescendant))
      bigPicFrame.visible = true
    }
  }
  
}

