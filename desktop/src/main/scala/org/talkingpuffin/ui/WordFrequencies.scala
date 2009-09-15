package org.talkingpuffin.ui

import io.Source
import java.awt.BorderLayout
import swing.{Frame, BorderPanel}
import util.Cancelable
import javax.swing.{JScrollPane, JTextPane}
import collection.immutable.Map

class WordFrequenciesFrame(text: String) extends Frame with Cancelable {
  title = "Word Frequencies"
  contents = new BorderPanel {
    peer.add(new JScrollPane(new JTextPane {
      setContentType("text/html")
      setEditable(false)
      setText(createDisplayText(calculateBuckets(calculateCounts(text))))
      setCaretPosition(0)
    }), BorderLayout.CENTER
    )
  }

  private def calculateCounts(text: String): List[WordCount] = {
    val words = List.fromArray(text.replaceAll("""["'(),:;.!?/\-+]""", "").toLowerCase.split("\\s")).
        filter(_.trim.length > 0) -- stopList
    val emptyMap = Map.empty[String, WordCount].withDefault(w => WordCount(w, 0))
    val countsMap = words.foldLeft(emptyMap)((map, word) => map(word.toLowerCase) += 1)
    countsMap.values.toList.sort(_.count > _.count)
  }
  
  private type BucketMap = Map[Long,List[String]]
  
  private def calculateBuckets(wordCounts: List[WordCount]): BucketMap = {
    val emptyMap = Map.empty[Long,List[String]].withDefaultValue(List[String]())
    wordCounts.foldLeft(emptyMap)((map, wordCount) => 
        map(wordCount.count) = wordCount.word :: map(wordCount.count))
  }
  
  private def createDisplayText(buckets: BucketMap): String = 
    "<div style='font-family: sans-serif'>" +
    (for (freq <- List.fromArray(buckets.keySet.toArray).sort(_ > _))
      yield "<b>" + freq + "</b>" + ": " + buckets.get(freq).get.sort(_ < _).mkString(", ") + "<br>"
    ).mkString + "</div>"
  
  private def stopList: List[String] = List.fromArray(Source.fromInputStream(
      getClass.getResourceAsStream("/stoplist.csv")).getLines.mkString(",").split(","))

  private case class WordCount(word: String, count: Long) {
    override def toString = word + ":&nbsp;" + "<b>" + count + "</b>"
    def +(n: Long): WordCount = WordCount(word, count + n)
  }

}
  
