package org.talkingpuffin.ui

import java.awt.event.KeyEvent
import java.awt.{Dimension}
import javax.swing.border.EmptyBorder
import scala.swing.GridBagPanel._
import swing._
import org.talkingpuffin.filter.{CompoundFilter, CompoundFilters, NoiseFilter, TextTextFilter, TagUsers, FilterSet}
import org.talkingpuffin.util.Loggable

/**
 * Dialog for setting filters
 */
class FiltersDialog(paneTitle: String, tableModel: StatusTableModel, filterSet: FilterSet, 
    tagUsers: TagUsers) extends Frame with Loggable {
  
  title = paneTitle + " Filters"
  preferredSize = new Dimension(600, 600)
  
  val generalPane = new GridBagPanel {
    border = new EmptyBorder(5, 5, 0, 5)

    val excludeFriendRetweets = new CheckBox("Exclude retweets of statuses of people you follow") {
      tooltip = "Requires a People view to have been created, which is not recommended for huge sets of following/followers"
      peer.setMnemonic(KeyEvent.VK_R) // TODO find out why the pure scala.swing attempt caused assertion failure
    }
    class Cns(row: Int) extends Constraints {grid=(0,row); gridwidth=3; anchor=Anchor.West}
    add(excludeFriendRetweets, new Cns(1))
    val excludeNonFollowers = new CheckBox("Exclude non-followers") {peer.setMnemonic(KeyEvent.VK_F)}
    add(excludeNonFollowers, new Cns(2))
    val useNoiseFilters = new CheckBox("Use external noise filters") {peer.setMnemonic(KeyEvent.VK_N)}
    add(useNoiseFilters, new Constraints {grid=(0,3); anchor=Anchor.West})
    add(new Button(new Action("Update") {
        mnemonic = KeyEvent.VK_O
        def apply = NoiseFilter.load
      }) {
        tooltip = "Fetch the latest noise filters from the external service"
      }, new Constraints {grid=(1,3); anchor=Anchor.West})
    add(new Label(" "), new Constraints {grid=(1,5); fill=Fill.Both; weightx=1; weighty=1}) // Filler
  }
  
  val includePane = new InOutPane("Only Tweets Matching One of", filterSet.includeSet.cpdFilters)
  
  val excludePane = new InOutPane("Tweets Matching Any of", filterSet.excludeSet.cpdFilters)
  
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
      val saveAction = new Action("Save") {
        mnemonic = KeyEvent.VK_S
        def apply = {debug(filterSet.excludeSet.cpdFilters.toString)}
      }
      val saveButton = new Button(saveAction) 
      // Not ready     contents += saveButton
    }
  }
  peer.setLocationRelativeTo(null)
  
  def addIncludeMatching(text: String) {
    includePane.table.addFilter(CompoundFilter(List(TextTextFilter(text, false)), None, None))
    applyChanges
  }

  def addExcludeMatching(text: String) {
    excludePane.table.addFilter(CompoundFilter(List(TextTextFilter(text, false)), None, None))
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

  class InOutPane(textPrompt: String, textFilters: CompoundFilters) extends GridBagPanel {
    val tagsPanel = new TagsPanel(true, false, tagUsers, List[String]())
  
    add(tagsPanel, new Constraints {grid=(0,0); anchor=Anchor.West; fill=Fill.Both; weightx=1; weighty=1})

    val table = new CompoundFilterControl(textPrompt, textFilters)
    add(table, new Constraints {
      grid=(0,3); gridwidth=3; anchor=Anchor.West; fill=Fill.Both; weightx=1; weighty=1
    })
    
  }
}


