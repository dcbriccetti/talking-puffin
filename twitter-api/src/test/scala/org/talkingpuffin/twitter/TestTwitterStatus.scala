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
      <created_at>Fri May 16 17:28:17 +0000 2008</created_at>
      <id>812948076</id>
      <text>Looking forward to sitting outside for 2 hours on a hot day encased in black graduation attire</text>
      <source>&lt;a href="http://www.twhirl.org/"&gt;twhirl&lt;/a&gt;</source>
      <truncated>false</truncated>
      <in_reply_to_status_id></in_reply_to_status_id>
      <in_reply_to_user_id></in_reply_to_user_id>
      <favorited>false</favorited>
      <user>
        <id>9160152</id>
        <name>Mark McBride</name>
        <screen_name>mcwong</screen_name>
        <location>Santa Clara, CA</location>
        <description>Developer, Student, Manager</description>
        <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/51480235/fb_profile2_normal.jpg</profile_image_url>
        <url>http://themcwongs.com</url>
        <protected>false</protected>
        <followers_count>59</followers_count>
      </user>
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
  def testReplyToStatusId() = assert(812463839 == status.inReplyToStatusId)
  def testReplyToUserId() = assert(611763 == status.inReplyToUserId)
  def testFavorited() = assert(true == status.favorited)
  def testUserExists() = assert(status.user != null)
  def testUserId() = assert(9160152 == status.user.id)
  def testUserName() = assert("Mark McBride" == status.user.name) 
  def testUserScreenName() = assert("mcwong" == status.user.screenName)
  def testLocation() = assert("Santa Clara, CA" == status.user.location)
  def testDescription() = assert("Developer, Student, Manager" == status.user.description)
  def testProfileImageURL() = assert("http://somewhere.com" == status.user.profileImageURL)
  def testUrl() = assert("http://themcwongs.com" == status.user.url)
  
}