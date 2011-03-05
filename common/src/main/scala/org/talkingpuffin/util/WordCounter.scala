package org.talkingpuffin.util

import io.Source

object WordCounter {
  type BucketMap = Map[Long,List[String]]

  def count(text: String): BucketMap = calculateBuckets(calculateCounts(text))

  private val stopList: List[String] = Source.fromInputStream(
    getClass.getResourceAsStream("/stoplist.csv")).getLines.mkString(",").split(",").toList

  private case class WordCount(word: String, count: Long) {
    def +(n: Long): WordCount = WordCount(word, count + n)
  }

  private def calculateCounts(text: String): List[WordCount] = {
    val words = text.replaceAll("""["'“”‘’(),:;.!?/\-+]""", "").toLowerCase.split("\\s").
        filter(_.trim.length > 0).toList -- stopList
    val emptyMap = Map.empty[String, WordCount].withDefault(w => WordCount(w, 0))
    val countsMap = words.foldLeft(emptyMap)((map, word) => map(word.toLowerCase) += 1)
    countsMap.values.toList.sort(_.count > _.count)
  }

  private def calculateBuckets(wordCounts: List[WordCount]): BucketMap = {
    val emptyMap = Map.empty[Long,List[String]].withDefaultValue(List[String]())
    wordCounts.foldLeft(emptyMap)((map, wordCount) =>
        map(wordCount.count) = wordCount.word :: map(wordCount.count))
  }

}
