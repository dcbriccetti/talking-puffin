package org.talkingpuffin.twitter

import twitter4j.User

case class TwitterList(name: String, description: String, shortName: String, owner: User, slug: String,
    subscriberCount: Long, memberCount: Long)
