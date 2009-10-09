package org.talkingpuffin.filter

import scala.util.matching.Regex
import scala.io.Source
import org.talkingpuffin.util.Loggable

object NoiseFilter extends Loggable {
  var exprs = List[Regex]()
  var loadError: Exception = _
  
  def noise_?(text: String): Boolean = {
    if (exprs == Nil && loadError == null) load
    
    exprs.exists(e => {
      text match {
        case e() => true
        case _ => false
      }
    })
  }
  
  private def load {
    try {
      val regExStrings = Source.fromURL("http://talkingpuffin.appspot.com/filters/noise").getLines.map(_.trim).toList
      info("Loaded " + regExStrings)
      exprs = regExStrings.map(_.r)
    } catch {
      case e: Exception => {
        loadError = e
        error(e.toString) 
      }
    }
  }
  
  private def loadSamples {
    exprs = List(".*web.*", ".*Twitter.*").map(_.r)
  }
}