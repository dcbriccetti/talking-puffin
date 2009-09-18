import org.talkingpuffin.filter.Retweets

object RetweetsTest {
  def main(args: Array[String]): Unit = {
    assert(Retweets.fromFriend_?("RT @dave Hi", List("dave", "mary")))
    assert(Retweets.fromFriend_?("Hi (via @dave)", List("dave", "mary")))
    assert(! Retweets.fromFriend_?("Hi (via @dave2)", List("dave", "mary")))
  }
}