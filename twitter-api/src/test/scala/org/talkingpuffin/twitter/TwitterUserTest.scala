package org.talkingpuffin.twitter

import org.junit.Test

class TwitterUserTest {
  var users = List[TwitterUser]()
  val featured_doc = 
    <users type="array">
      <user>
        <id>6273552</id>
        <name>Hammer</name>
        <screen_name>MCHammer</screen_name>
        <location>Bay Area, California</location>
        <description>http://mchammer.blogspot.com</description>
        <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/53504342/hammer_normal.jpg</profile_image_url>
        <url>http://www.dancejam.com</url>
        <protected>false</protected>
        <followers_count>2018</followers_count>
        <status>
          <created_at>Sat May 17 08:37:06 +0000 2008</created_at>
          <id>813432293</id>
          <text>I am so grateful to have wonderful kids... they bless my heart...</text>
          <source>web</source>
          <truncated>false</truncated>
          <in_reply_to_status_id></in_reply_to_status_id>
          <in_reply_to_user_id></in_reply_to_user_id>
          <favorited>false</favorited>
        </status>
      </user>
      <user>
        <id>14266714</id>
        <name>innocent AGM 2008</name>
        <screen_name>innocentAGM2008</screen_name>
        <location>london</location>
        <description>innocent by nature</description>
        <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/52361531/dood_normal.gif</profile_image_url>
        <url>http://www.innocentdrinks.co.uk/AGM/</url>
        <protected>false</protected>
        <followers_count>364</followers_count>
        <status>
          <created_at>Thu May 08 11:19:40 +0000 2008</created_at>
          <id>806279267</id>
          <text>Now go and enjoy the summer sun. And autumn leaves. And winter frost. Thanks for following.</text>
          <source>web</source>
          <truncated>false</truncated>
          <in_reply_to_status_id></in_reply_to_status_id>
          <in_reply_to_user_id></in_reply_to_user_id>
          <favorited>false</favorited>
        </status>
      </user>
    </users>

  featured_doc\"user" foreach {(entry) =>
    users = TwitterUser(entry) :: users
  }

  // test featured parsing
  @Test def testFeaturedUserId = assert(users.head.id == 14266714)
  @Test def testFeaturedStatus = assert(users.head.status != null)
}
