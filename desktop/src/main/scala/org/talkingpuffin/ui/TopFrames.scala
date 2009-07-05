package org.talkingpuffin.ui

import apache.log4j.Logger
import mac.QuitHandler
import talkingpuffin.util.Loggable

/**
 * Keeps track of top-level frames.
 */
object TopFrames extends Loggable {
  private var frames = List[TopFrame]()

  QuitHandler register TopFrames.closeAll

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

  def closeAll: Unit = closeAll(frames)

  def closeAll(frames: List[TopFrame]): Unit = frames match {
    case frame :: rest => {
      frame.dispose
      frame.saveState
      TopFrames removeFrame frame
      closeAll(rest)
    }
    case Nil =>
  }
}
  
 