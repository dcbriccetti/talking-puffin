package org.talkingpuffin.ui

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.Insets
import java.util.Date
import javax.swing.{BorderFactory}
import joda.time.DateTime
import swing.GridBagPanel._
import swing.{Reactor, Frame, Label, ComboBox, GridBagPanel, FlowPanel, Action, Button, BorderPanel}
import talkingpuffin.util.Loggable
import time.TimeFormatter
import util.Cancelable

object DataProvidersDialog {
  val DefaultRefreshSecs = 600
}

class DataProvidersDialog(owner: java.awt.Frame, streams: Streams) extends Frame with Cancelable with Loggable {
  
  title = "Data Providers"
  
  val panel = new BorderPanel {
    
    val mainPanel = new GridBagPanel {
      border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
      val headingInsets = new Insets(0, 0, 10, 0)
      add(new Label("Provider"), new Constraints {grid=(0,0); anchor=Anchor.West; insets=headingInsets})
      add(new Label("Update Every"), new Constraints {grid=(1,0); anchor=Anchor.West; insets=headingInsets})
      add(new Label("Next"), new Constraints {grid=(2,0); anchor=Anchor.West; insets=headingInsets})
      val firstReloadTime = new DateTime((new Date).getTime + DataProvidersDialog.DefaultRefreshSecs * 1000)
      
      streams.providers.providers.zipWithIndex.foreach(p => {
        val provider = p._1
        val i = p._2 + 1
        add(new Label(provider.providerName), new Constraints {grid=(0,i); anchor=Anchor.West})
        add(new RefreshCombo(provider, assortedTimes), new Constraints {grid=(1,i); anchor=Anchor.West})
        val nextLoadLabel = new Label(formatTime(firstReloadTime))
        new Reactor {
          listenTo(provider)
          reactions += {
            case e: NextLoadAt => {
              val time = formatTime(e.when)
              nextLoadLabel.text = time
              debug("NextLoadAt notification received from " + provider.providerName + ": " + time)
            }
          }
        }
        add(nextLoadLabel, new Constraints {grid=(2,i); anchor=Anchor.West})
      })
      
      case class DisplayTime(val seconds: Int) {
        override def toString = TimeFormatter(seconds).longForm
      }
  
      class RefreshCombo(provider: BaseProvider, times: List[Int]) extends ComboBox(times map DisplayTime) {
        peer.setToolTipText("How often to load new items")
        val DefaultRefresh = DisplayTime(DataProvidersDialog.DefaultRefreshSecs)
        peer.setSelectedItem(DefaultRefresh)
        provider.setUpdateFrequency(DefaultRefresh.seconds)
        peer.addActionListener(new ActionListener(){
          def actionPerformed(e: ActionEvent) = {  // Couldn’t get to work with reactions
            provider.setUpdateFrequency(selection.item.seconds)
          }
        })
    
      }
    }
    add(mainPanel, BorderPanel.Position.Center)

    add(new FlowPanel {
      val okAction = new Action("Close") {
        def apply = {DataProvidersDialog.this.visible = false}
      }
      val okButton = new Button(okAction)
      contents += okButton
      defaultButton = okButton
    }, BorderPanel.Position.South)
  }
  contents = panel
  pack
  peer.setLocationRelativeTo(null)

  private def assortedTimes: List[Int] = {
    val tenThruFiftySecs = List.range(10, 50, 10)
    val oneThruNineMins = List.range(60, 600, 60)
    val tenThruSixtyMins = List.range(10 * 60, 60 * 60 + 1, 10 * 60)
    tenThruFiftySecs ::: oneThruNineMins ::: tenThruSixtyMins 
  }
  
  private def formatTime(dt: DateTime) = dt.toString("HH:mm:ss")
}

