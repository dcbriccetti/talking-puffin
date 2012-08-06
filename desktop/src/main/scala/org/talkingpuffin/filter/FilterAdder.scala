package org.talkingpuffin.filter

/**
 * Helps add filters to a FilterSet.
 */
class FilterAdder(filterSet: FilterSet) {

  def muteApps(apps: Iterable[String]) {
    apps.foreach(app => exclude(CompoundFilter.muteApp(app)))
    filterSet.publish()
  }

  def muteSenders(senders: Iterable[String]) {
    senders.foreach(sender => exclude(CompoundFilter.muteSender(sender)))
    filterSet.publish()
  }

  def muteRetweetUsers(senders: Iterable[String]) {
    senders.foreach(sender => exclude(CompoundFilter.muteRtSender(sender)))
    filterSet.publish()
  }

  def muteSelectedUsersCommentedRetweets(senders: Iterable[String]) {
    senders.foreach(sender => {
      exclude(CompoundFilter.muteRtSender(sender))
      exclude(CompoundFilter.muteCRtSender(sender))
    })
    filterSet.publish()
  }

  def muteSenderReceivers(senderReceivers: Iterable[(String, String)]) {
    senderReceivers.foreach(sr => exclude(
      CompoundFilter(List(
        FromTextFilter(sr._1, isRegEx = false),
        ToTextFilter(sr._2, isRegEx = false)),
      None, None)))
    filterSet.publish()
  }
  
  private def exclude(filter: CompoundFilter) {
    filterSet.excludeSet.cpdFilters.add(filter)
  }
}
