package org.talkingpuffin.state

import java.util.prefs.Preferences
import org.talkingpuffin.filter.TagUsers
import org.talkingpuffin.ui.{DataProviders, Streams}

object StateSaver {

  def save(streams: Streams, prefs: Preferences, tagUsers: TagUsers) {
    saveProviderHighIds(streams.providers, prefs)
    tagUsers.save
    streams.views.last.pane.saveState // TODO instead save the order of the last status pane changed
  }
  
  private def saveProviderHighIds(provs: DataProviders, prefs: Preferences) {
    for {(provider, key) <- provs.providersAndPrefKeys
      id = provider.getHighestId
      if id.isDefined
    } prefs.put(key, id.get.toString)
  }

}