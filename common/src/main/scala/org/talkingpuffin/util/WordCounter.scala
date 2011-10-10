package org.talkingpuffin.util

import io.Source

case class WordCounter(text: String, wordFilter: String => Boolean = WordCounter.startsWithLetter,
                        wordProcessor: String => String = identity) {
  val words: List[WordCount] = calculateCounts(text)
  val frequencies: WordCounter.FreqToStringsMap = words.groupBy(_.count).mapValues(_.map(wc => wc.word))

  case class WordCount(word: String, count: Long) {
    def +(n: Long): WordCount = WordCount(word, count + n)
  }

  private def calculateCounts(text: String): List[WordCount] = {
    val words = text.toLowerCase.split("\\s").toList.
        withFilter(w => w.trim.length > 0 && ! w.toLowerCase.startsWith("http") &&
                   ! WordCounter.stopList.contains(w) && wordFilter(w)).
        map(dropTrailingPunctuation).map(wordProcessor)
    val countsMap = scala.collection.mutable.Map[String, WordCount]()
    words.foreach(word => {
      countsMap.get(word) match {
        case Some(wc) => countsMap(word) = wc + 1
        case _ => countsMap(word) = WordCount(word, 0)
      }
    })
    countsMap.values.toList.sortBy(_.count)
  }

  private def dropTrailingPunctuation(word: String) =
    word match {
      case w if ",.:?".contains(w.takeRight(1)) => w.take(w.length-1)
      case w => w
    }

}

object WordCounter {
  type Frequency = Long
  type FreqToStringsMap = Map[Frequency,List[String]]

  def startsWithLetter(word: String) = word(0).isLetter

  private val stopList: Set[String] = Source.fromInputStream(
    getClass.getResourceAsStream("/stoplist.csv")).getLines().mkString(",").split(",").toSet
}
