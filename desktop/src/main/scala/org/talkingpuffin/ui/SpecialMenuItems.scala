package org.talkingpuffin.ui

import swing.Action
import org.talkingpuffin.util.Loggable
import javax.swing.JTable
import javax.swing.event.{ListSelectionEvent, ListSelectionListener}

class SpecialMenuItems(table: JTable, rels: Relationships,
      getSelectedUserIds: => List[Long], getSelectedScreenNames: => List[String],
      getReplyIsSelected: => Boolean) extends Loggable {

  table.getSelectionModel.addListSelectionListener(new ListSelectionListener {
    def valueChanged(event: ListSelectionEvent) = 
      if (! event.getValueIsAdjusting) 
        enableActions(getSelectedUserIds, getSelectedScreenNames.length)
  })
  
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
  def all = List(oneStatusSelected.list, oneScreennameSelected.list, followersOnly.list, 
      friendsOnly.list, notFriendsOnly.list, replyOnly.list)
    
  def enableActions(selIds: List[Long], numSelectedScreenNames: Int) {
    
    all foreach(_.foreach(_.enabled = true))
    
    oneStatusSelected     disableIf selIds.length != 1
    oneScreennameSelected disableIf numSelectedScreenNames != 1
    followersOnly         disableIf selIds.exists(id => ! rels.followerIds.contains(id))
    friendsOnly           disableIf selIds.exists(id => ! rels.friendIds  .contains(id))
    notFriendsOnly        disableIf selIds.exists(id =>   rels.friendIds  .contains(id))
    replyOnly             disableIf ! getReplyIsSelected
  }
}
  
