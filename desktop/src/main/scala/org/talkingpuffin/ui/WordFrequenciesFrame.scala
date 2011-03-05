package org.talkingpuffin.ui

import java.awt.BorderLayout
import swing.{Frame, BorderPanel}
import util.Cancelable
import javax.swing.{JScrollPane, JTextPane}
import org.talkingpuffin.util.WordCounter

class WordFrequenciesFrame(text: String) extends Frame with Cancelable {
  title = "Word Frequencies"
  contents = new BorderPanel {
    peer.add(new JScrollPane(new JTextPane {
      setContentType("text/html")
      setEditable(false)
      setText(createDisplayText(WordCounter.count(text)))
      setCaretPosition(0)
    }), BorderLayout.CENTER
    )
  }

  private def createDisplayText(buckets: WordCounter.BucketMap): String =
    "<div style='font-family: sans-serif'>" +
    (for (freq <- buckets.keys.toList.sorted)
      yield "<b>" + freq + "</b>" + ": " + buckets.get(freq).get.sorted.mkString(", ") + "<br>"
    ).mkString + "</div>"
  
}
