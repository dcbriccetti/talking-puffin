package org.talkingpuffin.twitter

import junit.framework._;
import Assert._;

object TwitterStatusTest {
  def suite: Test = {
      val suite = new TestSuite(classOf[TwitterStatusTest]);
      suite
  }
}

class TwitterStatusTest {
  var statuses = List[TwitterStatus]()
  var status: TwitterStatus = null

  val status_doc =
  <statuses type="array">
    <status>
      <created_at>Fri Jun 19 22:36:52 +0000 2009</created_at>
      <id>2245071380</id>
      <text>Last night I dreamt that I was watching a film about someone who gradually realized they were retweeting their own death.</text>
      <source>web</source>
      <truncated>false</truncated>
      <in_reply_to_status_id></in_reply_to_status_id>
      <in_reply_to_user_id></in_reply_to_user_id>
      <favorited>false</favorited>
      <in_reply_to_screen_name></in_reply_to_screen_name>
      <user>
        <id>25753325</id>
        <name>Prabhakar Ragde</name>
        <screen_name>plragde</screen_name>
        <location></location>
        <description>Autoredact.</description>
        <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/105947980/prabhakar_SP_normal.jpg</profile_image_url>
        <url></url>
        <protected>false</protected>
        <followers_count>94</followers_count>
        <profile_background_color>709397</profile_background_color>
        <profile_text_color>333333</profile_text_color>
        <profile_link_color>FF3300</profile_link_color>
        <profile_sidebar_fill_color>A0C5C7</profile_sidebar_fill_color>
        <profile_sidebar_border_color>86A4A6</profile_sidebar_border_color>
        <friends_count>40</friends_count>
        <created_at>Sun Mar 22 00:28:55 +0000 2009</created_at>
        <favourites_count>0</favourites_count>
        <utc_offset>-18000</utc_offset>
        <time_zone>Quito</time_zone>
        <profile_background_image_url>http://static.twitter.com/images/themes/theme6/bg.gif</profile_background_image_url>
        <profile_background_tile>false</profile_background_tile>
        <statuses_count>817</statuses_count>
        <notifications></notifications>
        <verified>false</verified>
        <following></following>
      </user>
      <retweet_details>
        <retweeted_at>Fri Jun 19 22:41:13 +0000 2009</retweeted_at>
        <retweeting_user>
          <id>3191321</id>
          <name>Marcel Molina</name>
          <screen_name>noradio</screen_name>
          <location>San Francisco, CA</location>
          <description>Engineer at Twitter, retired Rails Core member, and burgeoning Scala enthusiast.</description>
          <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/53473799/marcel-euro-rails-conf_normal.jpg</profile_image_url>
          <url>http://project.ioni.st</url>
          <protected>false</protected>
          <followers_count>2265</followers_count>
          <profile_background_color>9AE4E8</profile_background_color>
          <profile_text_color>333333</profile_text_color>
          <profile_link_color>0084B4</profile_link_color>
          <profile_sidebar_fill_color>DDFFCC</profile_sidebar_fill_color>
          <profile_sidebar_border_color>BDDCAD</profile_sidebar_border_color>
          <friends_count>310</friends_count>
          <created_at>Mon Apr 02 07:47:28 +0000 2007</created_at>
          <favourites_count>96</favourites_count>
          <utc_offset>-28800</utc_offset>
          <time_zone>Pacific Time (US
            &amp;
            Canada)</time_zone>
          <profile_background_image_url>http://s3.amazonaws.com/twitter_production/profile_background_images/18156348/jessica_tiled.jpg.jpeg</profile_background_image_url>
          <profile_background_tile>true</profile_background_tile>
          <statuses_count>3309</statuses_count>
          <verified>false</verified>
        </retweeting_user>
      </retweet_details>
      <geo>
        <georss:Point>37.780300 -122.396900</georss:Point>
      </geo>
    </status>
    <status>
      <created_at>Fri May 16 03:50:13 +0000 2008</created_at>
      <id>812465949</id>
      <text>@niels hi</text>
      <source>im</source>
      <truncated>false</truncated>
      <in_reply_to_status_id>812463839</in_reply_to_status_id>
      <in_reply_to_user_id>611763</in_reply_to_user_id>
      <favorited>true</favorited>
      <user>
        <id>9160152</id>
        <name>Mark McBride</name>
        <screen_name>mcwong</screen_name>
        <location>Santa Clara, CA</location>
        <description>Developer, Student, Manager</description>
        <profile_image_url>http://somewhere.com</profile_image_url>
        <url>http://themcwongs.com</url>
        <protected>false</protected>
        <followers_count>59</followers_count>
      </user>
    </status>
  </statuses>


  status_doc\"status" foreach {(entry) =>
    statuses = TwitterStatus(entry) :: statuses
  }
  status = statuses.head

  // test status methods
  def testLength() = assert(statuses.length == 2)
  def testId() = assert(812465949 == status.id)
  def testText() = assert("@niels hi" == status.text)
  def testSource() = assert("im" == status.source)
  def testTruncated() = assert(false == status.truncated)
  def testReplyToStatusId() = assert(812463839 == status.inReplyToStatusId.get)
  def testReplyToUserId() = assert(611763 == status.inReplyToUserId.get)
  def testFavorited() = assert(true == status.favorited)
  def testUserExists() = assert(status.user != null)
  def testUserId() = assert(9160152 == status.user.id)
  def testUserName() = assert("Mark McBride" == status.user.name)
  def testUserScreenName() = assert("mcwong" == status.user.screenName)
  def testLocation() = assert("Santa Clara, CA" == status.user.location)
  def testDescription() = assert("Developer, Student, Manager" == status.user.description)
  def testProfileImageURL() = assert("http://somewhere.com" == status.user.profileImageURL)
  def testUrl() = assert("http://themcwongs.com" == status.user.url)
  def testRetweet() = assert(statuses(1).retweeted != null)
  def testRetweetTime() = assert(statuses(1).retweeted.retweetedAt != null)
  def testRetweetUser() = assert(statuses(1).retweeted.name == "Marcel Molina")
  def testGeo() = {
    statuses(1).location match {
      case Some((lat,long)) => {
        assertEquals(lat, 37.780300D)
        assertEquals(long, -122.396900)
      }
      case _ => fail
    }
  }
}