package org.talkingpuffin.twitter

import org.junit.Test

class TwitterMessageTest {
  var msgs = List[TwitterMessage]()

  val message_xml = 
    <direct-messages type="array">
      <direct_message>
        <id>31317269</id>
        <text>What is Scala?  Second time I saw you twitter about it.</text>
        <sender_id>4567221</sender_id>
        <recipient_id>9160152</recipient_id>
        <created_at>Sun May 18 03:59:39 +0000 2008</created_at>
        <sender_screen_name>jraggio</sender_screen_name>
        <recipient_screen_name>mcwong</recipient_screen_name>
        <sender>
          <id>4567221</id>
          <name>John Raggio</name>
          <screen_name>jraggio</screen_name>
          <location></location>
          <description></description>
          <profile_image_url>http://static.twitter.com/images/default_profile_normal.png</profile_image_url>
          <url></url>
          <protected>false</protected>
          <followers_count>13</followers_count>
        </sender>
        <recipient>
          <id>9160152</id>
          <name>Mark McBride</name>
          <screen_name>mcwong</screen_name>
          <location>Santa Clara, CA</location>
          <description>Developer, Student, Manager</description>
          <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/51480235/fb_profile2_normal.jpg</profile_image_url>
          <url>http://themcwongs.com</url>
          <protected>false</protected>
          <followers_count>59</followers_count>
        </recipient>
      </direct_message>
      <direct_message>
        <id>31180886</id>
        <text>(either fb or google for that matter) that does not place proper protection on how you share the info with apps</text>
        <sender_id>1475091</sender_id>
        <recipient_id>9160152</recipient_id>
        <created_at>Wed May 14 19:31:42 +0000 2008</created_at>
        <sender_screen_name>njsf</sender_screen_name>
        <recipient_screen_name>mcwong</recipient_screen_name>
        <sender>
          <id>1475091</id>
          <name>Nelson Ferreira</name>
          <screen_name>njsf</screen_name>
          <location>New York</location>
          <description></description>
          <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/53316777/time_normal.JPG</profile_image_url>
          <url></url>
          <protected>false</protected>
          <followers_count>20</followers_count>
        </sender>
        <recipient>
          <id>9160152</id>
          <name>Mark McBride</name>
          <screen_name>mcwong</screen_name>
          <location>Santa Clara, CA</location>
          <description>Developer, Student, Manager</description>
          <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/51480235/fb_profile2_normal.jpg</profile_image_url>
          <url>http://themcwongs.com</url>
          <protected>false</protected>
          <followers_count>59</followers_count>
        </recipient>
      </direct_message>
      <direct_message>
        <id>31180861</id>
        <text>I am not a heavy facebook and don't like the app model</text>
        <sender_id>1475091</sender_id>
        <recipient_id>9160152</recipient_id>
        <created_at>Wed May 14 19:31:21 +0000 2008</created_at>
        <sender_screen_name>njsf</sender_screen_name>
        <recipient_screen_name>mcwong</recipient_screen_name>
        <sender>
          <id>1475091</id>
          <name>Nelson Ferreira</name>
          <screen_name>njsf</screen_name>
          <location>New York</location>
          <description></description>
          <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/53316777/time_normal.JPG</profile_image_url>
          <url></url>
          <protected>false</protected>
          <followers_count>20</followers_count>
        </sender>
        <recipient>
          <id>9160152</id>
          <name>Mark McBride</name>
          <screen_name>mcwong</screen_name>
          <location>Santa Clara, CA</location>
          <description>Developer, Student, Manager</description>
          <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/51480235/fb_profile2_normal.jpg</profile_image_url>
          <url>http://themcwongs.com</url>
          <protected>false</protected>
          <followers_count>59</followers_count>
        </recipient>
      </direct_message>
      <direct_message>
        <id>31175601</id>
        <text>How clean is the air on the ride to work there? Many bikemuters?</text>
        <sender_id>1475091</sender_id>
        <recipient_id>9160152</recipient_id>
        <created_at>Wed May 14 17:47:35 +0000 2008</created_at>
        <sender_screen_name>njsf</sender_screen_name>
        <recipient_screen_name>mcwong</recipient_screen_name>
        <sender>
          <id>1475091</id>
          <name>Nelson Ferreira</name>
          <screen_name>njsf</screen_name>
          <location>New York</location>
          <description></description>
          <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/53316777/time_normal.JPG</profile_image_url>
          <url></url>
          <protected>false</protected>
          <followers_count>20</followers_count>
        </sender>
        <recipient>
          <id>9160152</id>
          <name>Mark McBride</name>
          <screen_name>mcwong</screen_name>
          <location>Santa Clara, CA</location>
          <description>Developer, Student, Manager</description>
          <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/51480235/fb_profile2_normal.jpg</profile_image_url>
          <url>http://themcwongs.com</url>
          <protected>false</protected>
          <followers_count>59</followers_count>
        </recipient>
      </direct_message>
      <direct_message>
        <id>31089695</id>
        <text>On twitter. So what the heck is scalaliftoff?</text>
        <sender_id>14622630</sender_id>
        <recipient_id>9160152</recipient_id>
        <created_at>Mon May 12 23:07:44 +0000 2008</created_at>
        <sender_screen_name>prestons</sender_screen_name>
        <recipient_screen_name>mcwong</recipient_screen_name>
        <sender>
          <id>14622630</id>
          <name>Preston</name>
          <screen_name>prestons</screen_name>
          <location>SF Bay Area</location>
          <description>Preston is Director of Interaction Design @ eBay and a UC Berkeley MBA 08</description>
          <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/53670996/prestonlittlecowboy_normal.jpg</profile_image_url>
          <url>http://www.prestonsmalley.com</url>
          <protected>false</protected>
          <followers_count>9</followers_count>
        </sender>
        <recipient>
          <id>9160152</id>
          <name>Mark McBride</name>
          <screen_name>mcwong</screen_name>
          <location>Santa Clara, CA</location>
          <description>Developer, Student, Manager</description>
          <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/51480235/fb_profile2_normal.jpg</profile_image_url>
          <url>http://themcwongs.com</url>
          <protected>false</protected>
          <followers_count>59</followers_count>
        </recipient>
      </direct_message>
    </direct-messages>

  message_xml\"direct_message" foreach {(entry) =>
    msgs = TwitterMessage(entry) :: msgs
  }

  // test featured parsing
  @Test def testSenderDetail = assert(msgs.head.sender != null)
  @Test def testRecipientDetail = assert(msgs.head.recipient != null)
}