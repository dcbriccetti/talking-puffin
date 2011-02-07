package org.talkingpuffin.filter

import scala.util.matching.Regex
import scala.io.Source
import org.talkingpuffin.Constants
import org.talkingpuffin.util.Loggable
import java.net.URL

/**
 * Loads noise filters from a repository and finds noisy tweets.
 */
object NoiseFilter extends Loggable {
  var expressions = List[Regex]()
  var loadError: Option[Exception] = None

  /**
   * Returns whether the provided tweet is noise.
   */
  def isNoise(text: String): Boolean = {
    if (needsLoading) 
      load
    
    val textOneLine = text.replaceAll("(\n|\r)", "") // Easier to match text all on one line
    
    val noise = expressions.exists(_.findFirstIn(textOneLine).isDefined)
    if (noise)
      debug(textOneLine + " matched")
    noise
  }

  /**
   * Loads noise-matching regular expressions from an external repository.
   */
  def load {
    try {
      val regExStrings = Source.fromURL(new URL(Constants.NoiseRepository)).getLines.
          map(_.trim).toList.filter(_.length > 0)
      info("Loaded " + regExStrings)
      expressions = regExStrings.map(_.r)
      loadError = None
    } catch {
      case e: Exception => {
        loadError = Some(e)
        error(e.toString) 
      }
    }
  }
  
  private def needsLoading: Boolean = {
    expressions == Nil /* None loaded */ && 
        ! loadError.isDefined /* We didn’t previously fail on loading */
  }
  
}