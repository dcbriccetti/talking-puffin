package org.talkingpuffin.ui

import org.talkingpuffin.mac.QuitHandler
import java.awt.Frame

/**
 * Keeps track of top-level frames.
 */
object TopFrames {
  private var frames = List[TopFrame]()

  QuitHandler register TopFrames.closeAll

  def findCurrentWindow = frames.find(_.peer.isFocused)

  def closeCurrentWindow() {
    findCurrentWindow match {
      case Some(frame) => frame.close
      case _ => closeOtherFrame()
    }
  }

  def closeOtherFrame() = Frame.getFrames().find(_.isFocused).foreach(_.dispose)

  def addFrame(f: TopFrame) {
    frames = f :: frames
  }

  def removeFrame(f: TopFrame) {
    frames -= f
    exitIfNoFrames
  }

  def exitIfNoFrames = if (frames == Nil) System.exit(0)

  def numFrames = frames.size

  def closeAll: Unit = closeAll(frames)

  def closeAll(frames: List[TopFrame]) = frames.foreach(_.close)
}
  
 