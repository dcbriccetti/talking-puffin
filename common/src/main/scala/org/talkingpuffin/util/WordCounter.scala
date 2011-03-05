package org.talkingpuffin.util

import io.Source

case class WordCounter(text: String) {
  private val stopList: List[String] = Source.fromInputStream(
    getClass.getResourceAsStream("/stoplist.csv")).getLines.mkString(",").split(",").toList

  val words = calculateCounts(text)
  val frequencies = calculateBuckets(words)

  case class WordCount(word: String, count: Long) {
    def +(n: Long): WordCount = WordCount(word, count + n)
  }

  private def calculateCounts(text: String): List[WordCount] = {
    val words = text.replaceAll("""["'“”‘’()<>,:;.!?/\-+]""", "").toLowerCase.split("\\s").
        filter(w => w.trim.length > 0 && w(0) != '@').toList -- stopList
    val emptyMap = Map.empty[String, WordCount].withDefault(w => WordCount(w, 0))
    val countsMap = words.foldLeft(emptyMap)((map, word) => map(word) += 1)
    countsMap.values.toList.sort(_.count > _.count)
  }

  private def calculateBuckets(wordCounts: List[WordCount]): WordCounter.BucketMap = {
    val emptyMap = Map.empty[Long,List[String]].withDefaultValue(List[String]())
    wordCounts.foldLeft(emptyMap)((map, wordCount) =>
        map(wordCount.count) = wordCount.word :: map(wordCount.count))
  }
}

object WordCounter {
  type Frequency = Long
  type BucketMap = Map[Frequency,List[String]]
}
