package org.talkingpuffin.ui

import _root_.scala.swing.GridBagPanel._
import java.awt.event.KeyEvent
import java.awt.{Dimension}
import javax.swing.border.EmptyBorder
import swing.{Orientation, BoxPanel, TabbedPane, Frame, GridBagPanel, FlowPanel, Button, CheckBox, Action}
import org.talkingpuffin.filter.{NoiseFilter, TextFilters, TagUsers, FilterSet}

/**
 * Dialog for setting filters
 */
class FiltersDialog(paneTitle: String, tableModel: StatusTableModel, filterSet: FilterSet, 
    tagUsers: TagUsers) extends Frame {
  
  title = paneTitle + " Filters"
  preferredSize = new Dimension(600, 600)
  
  val generalPane = new GridBagPanel {
    class CnsM(x: Int) extends Constraints {grid=(x,0); anchor=Anchor.West; fill=Fill.Both; weightx=1; weighty=1}

    border = new EmptyBorder(5, 5, 0, 5)

    add(new UnmutePane("Muted users", tableModel, filterSet, filterSet.mutedUsers, tableModel.unmuteUsers),
      new CnsM(1))
    add(new UnmutePane("Retweet-Muted users", tableModel, filterSet, filterSet.retweetMutedUsers, 
      tableModel.unmuteRetweetUsers), new CnsM(2))
    add(new UnmutePane("Muted apps", tableModel, filterSet, filterSet.mutedApps, 
      tableModel.unmuteApps), new CnsM(3))

    val excludeFriendRetweets = new CheckBox("Exclude retweets of statuses of people you follow") {
      peer.setMnemonic(KeyEvent.VK_R) // TODO find out why the pure scala.swing attempt caused assertion failure
    }
    class Cns(row: Int) extends Constraints {grid=(0,row); gridwidth=3; anchor=Anchor.West}
    add(excludeFriendRetweets, new Cns(1))
    val excludeNonFollowers = new CheckBox("Exclude non-followers") {peer.setMnemonic(KeyEvent.VK_F)}
    add(excludeNonFollowers, new Cns(2))
    val useNoiseFilters = new CheckBox("Use external noise filters") {peer.setMnemonic(KeyEvent.VK_N)}
    add(new FlowPanel {
      contents += useNoiseFilters
      contents += new Button(new Action("Update") {
        mnemonic = KeyEvent.VK_O
        def apply = NoiseFilter.load
      }) {
        tooltip = "Fetch the latest noise filters from the external service"
      }
    }, new Cns(3))
  }
  
  val includePane = new InOutPane("Only Tweets Containing One of", filterSet.includeSet.textFilters)
  
  val excludePane = new InOutPane("Tweets Containing Any of", filterSet.excludeSet.textFilters)
  
  contents = new BoxPanel(Orientation.Vertical) {
    contents += new TabbedPane {
      pages += new TabbedPane.Page("General", generalPane)
      pages += new TabbedPane.Page("Include", includePane)
      pages += new TabbedPane.Page("Exclude", excludePane)
    }
    contents += new FlowPanel {
      val applyAction = Action("Apply") {applyChanges}
      contents += new Button(applyAction)
      val okAction = new Action("OK") {
        mnemonic = KeyEvent.VK_O
        def apply = {applyChanges; FiltersDialog.this.visible = false}
      }
      val okButton = new Button(okAction) 
      defaultButton = okButton
      contents += okButton
    }
  }
  peer.setLocationRelativeTo(null)
  
  def addIncludeMatching(text: String) {
    includePane.table.addTextFilter(text, false)
    applyChanges
  }

  def addExcludeMatching(text: String) {
    excludePane.table.addTextFilter(text, false)
    applyChanges
  }

  def applyChanges {
    filterSet.includeSet.tags = includePane.tagsPanel.selectedTags
    filterSet.excludeSet.tags = excludePane.tagsPanel.selectedTags
    filterSet.excludeFriendRetweets = generalPane.excludeFriendRetweets.selected
    filterSet.excludeNonFollowers = generalPane.excludeNonFollowers.selected
    filterSet.useNoiseFilters = generalPane.useNoiseFilters.selected
    filterSet.publish
  }

  class InOutPane(textPrompt: String, textFilters: TextFilters) extends GridBagPanel {
    val tagsPanel = new TagsPanel(true, false, tagUsers, List[String]()) {
      minimumSize = new Dimension(180, 100)
    }
  
    add(tagsPanel, new Constraints {grid=(0,0); anchor=Anchor.West; fill=Fill.Vertical; weighty=1})

    val table = new TextFilterControl(textPrompt, textFilters)
    add(table, new Constraints {
      grid=(0,3); gridwidth=3; anchor=Anchor.West; fill=Fill.Both; weightx=1; weighty=1
    })
    
  }
}


