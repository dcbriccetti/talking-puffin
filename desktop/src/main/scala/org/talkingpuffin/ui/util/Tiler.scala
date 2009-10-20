package org.talkingpuffin.ui.util

import java.util.concurrent.atomic.AtomicInteger
import java.awt.{Point, Toolkit}

class Tiler(numTiles: Int) {
  private val screenSize = Toolkit.getDefaultToolkit().getScreenSize()
  private val cols = Math.sqrt(numTiles.toDouble).ceil.toInt
  private val tileHeight = screenSize.height / cols
  private val tileWidth = screenSize.width / cols
  private val nextTileIndex = new AtomicInteger(0)
    
  def next: Point = {
    val tileIndex = nextTileIndex.getAndIncrement
    val row = tileIndex / cols
    val col = tileIndex % cols
    new Point(col * tileWidth, row * tileHeight)
  } 
}

