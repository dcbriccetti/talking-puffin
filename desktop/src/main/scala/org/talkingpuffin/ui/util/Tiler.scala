package org.talkingpuffin.ui.util

import java.util.concurrent.atomic.AtomicInteger
import java.awt.{Dimension, Rectangle, Toolkit}

/**
 * A very simple way to position windows in a grid.
 */
class Tiler(numTiles: Int) {
  val screenSize = Toolkit.getDefaultToolkit().getScreenSize()
  private val cols = Math.sqrt(numTiles.toDouble).ceil.toInt
  private val tileHeight = screenSize.height / cols
  val tileWidth = screenSize.width / cols
  protected val nextTileIndex = new AtomicInteger(0)
    
  def next: Rectangle = {
    val tileIndex = nextTileIndex.getAndIncrement
    val row = tileIndex / cols
    val col = tileIndex % cols
    new Rectangle(col * tileWidth, row * tileHeight, tileWidth, tileHeight)
  } 
}

class ColTiler(size: Dimension, numCols: Int, heightFactor: Double) extends Tiler(numCols) {
  override val tileWidth = size.width / numCols

  override def next: Rectangle = {
    val colWidth = size.width / numCols
    new Rectangle(nextTileIndex.getAndIncrement * colWidth, 0, colWidth, 
      (heightFactor * size.height).toInt)
  }  
}

