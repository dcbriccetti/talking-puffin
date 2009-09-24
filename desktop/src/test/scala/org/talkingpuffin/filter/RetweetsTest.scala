import org.talkingpuffin.filter.Retweets
import org.specs.runner.JUnit4
import org.specs.Specification
 
class RetweetsTest extends JUnit4(RetweetsSpec)
 
object RetweetsSpec extends Specification {
    
  "Retweets are identified correctly" in {
    assert(Retweets.fromFriend_?("RT @dave Hi", List("dave", "mary")))
    assert(Retweets.fromFriend_?("Hi (via @dave)", List("dave", "mary")))
    assert(!Retweets.fromFriend_?("Hi (via @dave2)", List("dave", "mary")))
  }
}
 