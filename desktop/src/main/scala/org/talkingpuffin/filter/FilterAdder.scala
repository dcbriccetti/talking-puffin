package org.talkingpuffin.filter

/**
 * Helps add filters to a FilterSet.
 */
class FilterAdder(filterSet: FilterSet) {

  def muteApps(apps: List[String]) {
    apps.foreach(app => filterSet.excludeSet.cpdFilters.add(
        CompoundFilter(List(SourceTextFilter(app, false)), None, None)))
    filterSet.publish
  }

  def muteSenders(senders: List[String]) {
    senders.foreach(sender => filterSet.excludeSet.cpdFilters.add(
        CompoundFilter(List(FromTextFilter(sender, false)), None, None)))
    filterSet.publish
  }

  def muteRetweetUsers(senders: List[String]) {
    senders.foreach(sender => filterSet.excludeSet.cpdFilters.add(
        CompoundFilter(List(FromTextFilter(sender, false)), Some(true), None)))
    filterSet.publish
  }

  def muteSelectedUsersCommentedRetweets(senders: List[String]) {
    senders.foreach(sender => {
      val filters = List(FromTextFilter(sender, false))
      filterSet.excludeSet.cpdFilters.add(CompoundFilter(filters, Some(true), None))
      filterSet.excludeSet.cpdFilters.add(CompoundFilter(filters, None, Some(true)))
    })
    filterSet.publish
  }

  def muteSenderReceivers(srs: List[(String, String)]) {
    srs.foreach(sr => filterSet.excludeSet.cpdFilters.add(
        CompoundFilter(List(FromTextFilter(sr._1, false), 
        ToTextFilter(sr._2, false)), None, None)))
    filterSet.publish
  }
}