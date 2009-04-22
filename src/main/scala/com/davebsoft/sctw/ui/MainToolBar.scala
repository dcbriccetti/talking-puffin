package com.davebsoft.sctw.ui

import _root_.com.davebsoft.sctw.twitter.{TweetsProvider}
import _root_.scala.swing.{Label, ComboBox, Action}
import java.awt.event.{ActionEvent, ActionListener}

import javax.swing.JToolBar
/**
 * The main ToolBar
 * 
 * @author Dave Briccetti
 */

class MainToolBar(streams: Streams) extends JToolBar {
  setFloatable(false)
  
  add(new Label("Sources: "){tooltip = 
    "There is one source each for the “following” and “replies” timelines, no matter how many views you create"}.peer)

  addSeparator

  add(new Label("Following: ").peer)
  addSourceControls(streams.tweetsProvider, streams.createFollowingView)

  addSeparator

  add(new Label("Mentions: ").peer)
  addSourceControls(streams.mentionsProvider, streams.createRepliesView)
  
  private def addSourceControls(provider: TweetsProvider, createStream: => Unit) {
    val newViewAction = new Action("New View") {
      toolTip = "Creates a new view"
      def apply = createStream
    }
    add(newViewAction.peer)

    add(new RefreshCombo(provider).peer)
  
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
  
  class RefreshCombo(provider: TweetsProvider) extends ComboBox(List.range(0, 50, 10) ::: List.range(60, 600, 60)) {
    peer.setToolTipText("Number of seconds between automatic “Load New”s")
    var defaultRefresh = 120
    peer.setSelectedItem(defaultRefresh)
    provider.setUpdateFrequency(defaultRefresh)
    peer.addActionListener(new ActionListener(){
      def actionPerformed(e: ActionEvent) = {  // Couldn’t get to work with reactions
        provider.setUpdateFrequency(selection.item)
      }
    })
    
  }
}

