package org.talkingpuffin.filter

/**
 * Helps add filters to a FilterSet.
 */
class FilterAdder(filterSet: FilterSet) {

  def muteApps(apps: List[String]) {
    apps.foreach(app => exclude(CompoundFilter.muteApp(app)))
    filterSet.publish
  }

  def muteSenders(senders: List[String]) {
    senders.foreach(sender => exclude(CompoundFilter.muteSender(sender)))
    filterSet.publish
  }

  def muteRetweetUsers(senders: List[String]) {
    senders.foreach(sender => exclude(CompoundFilter.muteRtSender(sender)))
    filterSet.publish
  }

  def muteSelectedUsersCommentedRetweets(senders: List[String]) {
    senders.foreach(sender => {
      exclude(CompoundFilter.muteRtSender(sender))
      exclude(CompoundFilter.muteCRtSender(sender))
    })
    filterSet.publish
  }

  def muteSenderReceivers(srs: List[(String, String)]) {
    srs.foreach(sr => exclude(
        CompoundFilter(List(FromTextFilter(sr._1, false), 
        ToTextFilter(sr._2, false)), None, None)))
    filterSet.publish
  }
  
  private def exclude(filter: CompoundFilter) = filterSet.excludeSet.cpdFilters.add(filter) 
}