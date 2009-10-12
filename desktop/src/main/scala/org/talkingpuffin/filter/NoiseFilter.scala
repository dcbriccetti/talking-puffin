package org.talkingpuffin.filter

import scala.util.matching.Regex
import scala.io.Source
import org.talkingpuffin.util.Loggable

object NoiseFilter extends Loggable {
  var exprs = List[Regex]()
  var loadError: Exception = _
  
  def noise_?(text: String): Boolean = {
    if (exprs == Nil && loadError == null) load
    val textOneLine = text.replaceAll("(\n|\r)", "")
    exprs.exists(e => {
      textOneLine match {
        case e() => {
          debug(textOneLine + " matched " + e)
          true
        }
        case _ => false
      }
    })
  }
  
  def load {
    try {
      val regExStrings = Source.fromURL("http://talkingpuffin.appspot.com/filters/noise").getLines.
          map(_.trim).toList.filter(_.length > 0)
      info("Loaded " + regExStrings)
      exprs = regExStrings.map(_.r)
      loadError = null
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