package com.davebsoft.sctw.ui

import _root_.com.davebsoft.sctw.twitter.{TweetsProvider}
import _root_.scala.swing.{Label, ComboBox, Action}
import java.awt.event.{ActionEvent, ActionListener}

import javax.swing.JToolBar
import time.TimeFormatter
/**
 * The main ToolBar
 * 
 * @author Dave Briccetti
 */

class MainToolBar(streams: Streams) extends JToolBar {
  val remaining = new Label
  setFloatable(false)
  
  add(new Label("Following: ").peer)
  addSourceControls(streams.tweetsProvider, streams.createFollowingView)

  addSeparator

  add(new Label("Mentions: ").peer)
  addSourceControls(streams.mentionsProvider, streams.createRepliesView)

  /*  TODO finish adding rate limiting status. How often and from where to invoke?
  addSeparator

  add(new Label("Rem: ") {tooltip = "The number of requests remaining"}.peer)
  add(remaining.peer)
  */
  
  private def addSourceControls(provider: TweetsProvider, createStream: => Unit) {
    val newViewAction = new Action("New View") {
      toolTip = "Creates a new view"
      def apply = createStream
    }
    add(newViewAction.peer)

    val tenThruFiftySecs = List.range(10, 50, 10)
    val oneThruNineMins = List.range(60, 600, 60)
    val tenThruSixtyMins = List.range(10 * 60, 60 * 60 + 1, 10 * 60)
    val assortedTimes = tenThruFiftySecs ::: oneThruNineMins ::: tenThruSixtyMins 

    add(new RefreshCombo(provider, assortedTimes).peer)
  
    val loadNewAction = new Action("Load New") {
      toolTip = "Loads any new items"
      def apply = provider.loadNewData
    }
    add(loadNewAction.peer)
  
    val last200Action = new Action("Last 200") {
      toolTip = "Fetches the last 200 items"
      def apply = provider.loadLastBlockOfTweets
    }
    add(last200Action.peer)
  }
  
  case class DisplayTime(val seconds: Int) {
    override def toString = TimeFormatter(seconds).longForm
  }
  
  class RefreshCombo(provider: TweetsProvider, times: List[Int]) extends ComboBox(times map(DisplayTime(_))) {
    peer.setToolTipText("How often to load new items")
    var defaultRefresh = DisplayTime(120)
    peer.setSelectedItem(defaultRefresh)
    provider.setUpdateFrequency(defaultRefresh.seconds)
    peer.addActionListener(new ActionListener(){
      def actionPerformed(e: ActionEvent) = {  // Couldn’t get to work with reactions
        provider.setUpdateFrequency(selection.item.seconds)
      }
    })
    
  }
}

