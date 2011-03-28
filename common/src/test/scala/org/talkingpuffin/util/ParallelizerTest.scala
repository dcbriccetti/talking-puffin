package org.talkingpuffin.util

import org.specs.runner.JUnit4
import org.specs.Specification

class ParallelizerTest extends JUnit4(ParallelizerTest)

object ParallelizerTest extends Specification {
  "Parallelization" should {
    "produce correct results" in {
      val inputs = List(3, 4, 7, 8)
      Parallelizer.run(2, inputs, (n: Int) => n * 2).sorted must be_==(inputs.map(_ * 2))
    }
  }
}
