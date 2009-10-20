package org.talkingpuffin.twitter

import scala.xml.Node

class TwitterList(val xml: Node) {
  val name = (xml \ "name").text
  val shortName = name
  val longName = shortName + " from " + (xml \ "user" \ "name").text
  val owner = TwitterUser((xml \ "user")(0))
  val slug = (xml \ "slug").text
}

object TwitterList {
  def apply(xml: Node) = new TwitterList(xml)
}