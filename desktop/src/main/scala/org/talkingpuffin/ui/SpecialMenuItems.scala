package org.talkingpuffin.ui

import swing.Action
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.twitter.TwitterStatus

class SpecialMenuItems extends Loggable {
  class ActionsList {
    var list = List[Action]()
    def disableIf(value: Boolean) = if (value) list.foreach(action => action.enabled = false)
  }
  /** Only applicable when one status is selected */
  var oneStatusSelected = new ActionsList
  /** Only applicable when one screen name (even if multiple statuses) is selected */
  var oneScreennameSelected = new ActionsList
  /** Only applicable to followers */
  var followersOnly = new ActionsList
  /** Only applicable to those being followed */
  var friendsOnly = new ActionsList
  /** Only applicable to those not being followed */
  var notFriendsOnly = new ActionsList
  /** Only applicable to statuses that are a reply */
  var replyOnly = new ActionsList
  /** All special actions */
  def all = oneStatusSelected.list ::: oneScreennameSelected.list ::: followersOnly.list ::: 
      friendsOnly.list ::: notFriendsOnly.list ::: replyOnly.list
    
  def enableActions(selectedStatuses: List[TwitterStatus], numSelectedScreenNames: Int,
      friendIds: List[Long], followerIds: List[Long]) {
    
    all foreach(_.enabled = true)
    
    oneStatusSelected    .disableIf(selectedStatuses.length != 1) 
    oneScreennameSelected.disableIf(numSelectedScreenNames != 1)
    followersOnly        .disableIf(selectedStatuses.exists(status => ! followerIds.contains(status.user.id)))
    friendsOnly          .disableIf(selectedStatuses.exists(status => ! friendIds  .contains(status.user.id)))
    notFriendsOnly       .disableIf(selectedStatuses.exists(status => friendIds.contains(status.user.id)))
    replyOnly            .disableIf(! selectedStatuses.exists(_.inReplyToStatusId.isDefined))
  }
}
  
