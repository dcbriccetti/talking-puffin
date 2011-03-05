package org.talkingpuffin.snippet

import xml.{Text, NodeSeq}
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import org.talkingpuffin.web.Users
import org.talkingpuffin.model.TooManyFriendsFollowers
import org.talkingpuffin.apix.RichUser._

class UsersList {
  def show(content: NodeSeq): NodeSeq = {
    try {
      val tw = Auth.twitterS.is.get
      val ux = new Users()
      ux.setSession(tw)
      val userRows = ux.getUsers
      bind("resources", content,
        "resourceItems" -> userRows.flatMap(u =>
          bind("item", chooseTemplate("resources", "resourceItems", content),
            "arrows" -> Text(ux.getArrows(u)),
            "img" -> <img alt="Thumbnail" height="48" width="48" src={u.getProfileImageURL.toString}/>,
            "name" -> Text(u.getName),
            "screenName" -> <span><a target="_blank" href={"/analyze?user=" + u.getScreenName}>{u.getScreenName}</a>
              </span>,
            "friends" -> Text(u.getFriendsCount.toString),
            "followers" -> Text(u.getFollowersCount.toString),
            "location" -> Text(u.location),
            "description" -> Text(u.description),
            "status" -> Text(u.status match {case Some(s) => s.getText case _ => " "})
            )))
    } catch {
      case e: TooManyFriendsFollowers =>
        S.error("Can't process that many friends or followers")
        Text("")
    }
  }
}
