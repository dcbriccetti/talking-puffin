package org.talkingpuffin.ui

import java.awt.BorderLayout
import swing.{Frame, BorderPanel}
import util.Cancelable
import javax.swing.{JScrollPane, JTextPane}

case class WordCount(word: String, count: Long) {
  override def toString = word + ":&nbsp;" + count
  def +(n: Long): WordCount = WordCount(word, count + n)
}

class WordFrequenciesFrame(text: String) extends Frame with Cancelable {
  val words = List.fromArray(text.split("""[\s(),:;.!?/\-+]""")) -- 
    List("", "http", "of", "a", "an", "the", "to", "I", "and", "is", "in", "on", "for", "you", "it",
      "that", "my", "rt", "with", "be", "was", "this", "now", "from", "at", "new", "but", "so")
  val emptyMap = collection.immutable.Map.empty[String, WordCount].withDefault(w => WordCount(w, 0))
  val countsMap = words.foldLeft(emptyMap)((map, word) => map(word.toLowerCase) += 1)
  val wordCounts = countsMap.values.toList.sort(_.count > _.count)
  title = "Word Frequencies"
  contents = new BorderPanel {
    peer.add(new JScrollPane(new JTextPane {
      setContentType("text/html")
      setEditable(false)
      setText("<div style='font-family: sans-serif'>" + wordCounts.mkString(", ") + "</div>")
      setCaretPosition(0)
    }), BorderLayout.CENTER
    )
  }
}
  
