package org.talkingpuffin.ui

import io.Source
import java.awt.BorderLayout
import swing.{Frame, BorderPanel}
import util.Cancelable
import javax.swing.{JScrollPane, JTextPane}

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
    val words = List.fromArray(text.replaceAll("""["'(),:;.!?/\-+]""", "").toLowerCase.split("""[\s]""")).
        filter(_.trim.length > 0) -- stopList
    val emptyMap = collection.immutable.Map.empty[String, WordCount].withDefault(w => WordCount(w, 0))
    val countsMap = words.foldLeft(emptyMap)((map, word) => map(word.toLowerCase) += 1)
    countsMap.values.toList.sort(_.count > _.count)
  }
  
  private type BucketMap = Map[Long,List[String]]
  
  private def calculateBuckets(wordCounts: List[WordCount]): BucketMap = {
    val emptyMap = collection.immutable.Map.empty[Long,List[String]].withDefault(w => List[String]())
    wordCounts.foldLeft(emptyMap)((map, wordCount) => map(wordCount.count) = wordCount.word :: map(wordCount.count))
  }
  
  private def createDisplayText(buckets: BucketMap): String = {
    val sb = new StringBuilder("<div style='font-family: sans-serif'>")
    List.fromArray(buckets.keySet.toArray).sort(_ > _).map(occurrances => {
      sb.append("<b>" + occurrances + "</b>" + ": ")
      sb.append(buckets.get(occurrances).get.sort(_ < _).mkString(", ")).append("<br>")
    })
    sb.append("</div>")
    sb.toString
  }

  private def stopList: List[String] = List.fromArray(Source.fromInputStream(
      getClass.getResourceAsStream("/stoplist.csv")).getLines.mkString(",").split(","))

  private case class WordCount(word: String, count: Long) {
    override def toString = word + ":&nbsp;" + "<b>" + count + "</b>"
    def +(n: Long): WordCount = WordCount(word, count + n)
  }

}
  
