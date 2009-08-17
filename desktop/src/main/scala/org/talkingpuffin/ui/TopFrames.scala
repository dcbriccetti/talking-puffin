package org.talkingpuffin.ui

import apache.log4j.Logger
import mac.QuitHandler
import java.awt.Frame
import talkingpuffin.util.Loggable

/**
 * Keeps track of top-level frames.
 */
object TopFrames extends Loggable {
  private var frames = List[TopFrame]()

  QuitHandler register TopFrames.closeAll

  def closeCurrentWindow(){
    frames.find(_.peer.isFocused) match{
      case Some(frame) => close(frame)
      case _ => closeOtherFrame()
    }
  }

  def closeOtherFrame(){
    val frames = Frame.getFrames()
    frames.find(_.isFocused) match {
      case Some(frame) => frame.dispose
      case _ => // noop
    }
  }
  def addFrame(f: TopFrame){
    frames = f :: frames
    debug("Frame added. Number of frames is " + frames.size + ".")
  }

  def removeFrame(f: TopFrame){
    frames = frames.remove {f == _}
    debug ("Frame removed. Number of frames is " + frames.size + ".")
    exitIfNoFrames
  }

  def exitIfNoFrames =
    if(frames.size == 0){
      debug("No more frames. Exiting.")
      // it's kinda ugly to put the exit logic here, but not sure where
      // else to put it.'
      System.exit(0)
    }
  
  def numFrames = frames.size

  def close(frame:TopFrame): Unit = {
      frame.dispose
      frame.saveState
      TopFrames removeFrame frame
  }
  def closeAll: Unit = closeAll(frames)

  def closeAll(frames: List[TopFrame]): Unit = frames match {
    case frame :: rest => {
      close(frame)
      closeAll(rest)
    }
    case Nil =>
  }
}
  
 