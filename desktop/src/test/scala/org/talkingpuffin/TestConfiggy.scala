package org.talkingpuffin

import org.specs.runner.JUnit4
import org.specs.Specification
import net.lag.configgy.Configgy

class ConfiggyTest extends JUnit4(ConfiggySpec)

object ConfiggySpec extends Specification {

  "Configgy Loads" in {
    Configgy.configureFromResource("test.conf")
    val config = Configgy.config
    assert(config.getString("hostname","localhost") == "foo")
  }
}
