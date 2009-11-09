package org.talkingpuffin.ui.filter

import swing.GridBagPanel
import scala.swing.GridBagPanel._
import org.talkingpuffin.ui.{CompoundFilterControl, TagsPanel}
import org.talkingpuffin.filter.{TagUsers, CompoundFilters}

/**
 * Provides panes for include and exclude filters, in the Filters dialog.
 */
class IncludeExcludePane(tagUsers: TagUsers, textPrompt: String, 
    textFilters: CompoundFilters) extends GridBagPanel {
  val tagsPanel = new TagsPanel(true, false, tagUsers, List[String]())
  add(tagsPanel, new Constraints {
    grid=(0,0); anchor=Anchor.West; fill=Fill.Both; weightx=1; weighty=1
  })

  val table = new CompoundFilterControl(textPrompt, textFilters)
  add(table, new Constraints {
    grid=(0,3); gridwidth=3; anchor=Anchor.West; fill=Fill.Both; weightx=1; weighty=1
  })
    
}
