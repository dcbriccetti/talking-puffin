package org.talkingpuffin.ui

import java.awt.event.{ActionEvent, ActionListener, KeyEvent}
import java.awt.Insets
import javax.swing.{BorderFactory}
import swing.GridBagPanel._
import swing.{Frame, Label, ComboBox, GridBagPanel, FlowPanel, Action, Button, BorderPanel}
import time.TimeFormatter
import util.Cancelable

class DataProvidersDialog(owner: java.awt.Frame, streams: Streams) 
    extends Frame with Cancelable {
  title = "Data Providers"
  val panel = new BorderPanel {
    val mainPanel = new GridBagPanel {
      border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
      val headingInsets = new Insets(0, 0, 10, 0)
      add(new Label("Provider"), new Constraints {grid=(0,0); anchor=Anchor.West; insets=headingInsets})
      add(new Label("Update Frequency"), new Constraints {grid=(1,0); anchor=Anchor.West; insets=headingInsets})
      streams.providers.providers.zipWithIndex.foreach(p => {
        val provider = p._1
        val i = p._2 + 1
        add(new Label(provider.providerName), new Constraints {grid=(0,i); anchor=Anchor.West})
        add(new RefreshCombo(provider, assortedTimes), new Constraints {grid=(1,i); anchor=Anchor.West})
      })
      
      case class DisplayTime(val seconds: Int) {
        override def toString = TimeFormatter(seconds).longForm
      }
  
      class RefreshCombo(provider: BaseProvider, times: List[Int]) extends ComboBox(times map DisplayTime) {
        peer.setToolTipText("How often to load new items")
        var defaultRefresh = DisplayTime(600)
        peer.setSelectedItem(defaultRefresh)
        provider.setUpdateFrequency(defaultRefresh.seconds)
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
}

