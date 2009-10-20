package org.talkingpuffin.twitter

import scala.xml.Node

class TwitterLists(val xml: Node) {
  val shortName = (xml \ "name").text
  val longName = shortName + " from " + (xml \ "user" \ "name").text
  val lists = ((xml \ "list") map(list => TwitterList(list))).toList
}

object TwitterLists {
  def apply(xml: Node): TwitterLists = {
    new TwitterLists(xml)
  }
}