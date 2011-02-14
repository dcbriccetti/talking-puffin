package org.talkingpuffin.twitter

import scala.collection.JavaConversions._
import twitter4j._
import org.talkingpuffin.util.Loggable

/**
 * Allow fetching all TwitterResponse objects, without having to page.
 */
object PageHandler extends Loggable {

  def friendsStatuses(tw: Twitter, screenName: String)(cursor: Long) = tw.getFriendsStatuses(screenName, cursor)

  def followersStatuses(tw: Twitter, screenName: String)(cursor: Long) = tw.getFollowersStatuses(screenName, cursor)

  def userLists(tw: Twitter, listOwnerScreenName: String)(cursor: Long) = tw.getUserLists(listOwnerScreenName, cursor)

  def userListMembers(tw: Twitter, listOwnerScreenName: String, listId: Int)(cursor: Long) =
    tw.getUserListMembers(listOwnerScreenName, listId, cursor)

  def userListMemberships(tw: Twitter, listMemberScreenName: String)(cursor: Long) =
    tw.getUserListMemberships(listMemberScreenName, cursor)

  def allPages[T <: TwitterResponse](fn: (Long) => PagableResponseList[T], cursor: Long = -1): List[T] = cursor match {
    case 0 => Nil
    case c =>
      var resp = fn(c)
      resp.toList ::: allPages(fn, resp.getNextCursor)
  }

  def allPages(fn: (Paging) => ResponseList[Status], paging: Paging): List[Status] = {
    val resp = fn(paging).toList
    debug("Page " + paging.getPage + " results: " + resp.size)

    def halfRequested: Int = paging.getCount match {
      case -1 => 0 // -1 means not set, and we won’t assume we know what the default is
      case n => n / 2
    }

    resp.size match {
      case n if n <= halfRequested /* Better way to find the end? */ => resp
      case n => resp ::: allPages(fn, {paging.setPage(paging.getPage + 1); paging})
    }
  }

  def newPagingMaxPer() = new Paging(1, Constants.MaxItemsPerRequest)

}