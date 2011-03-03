package org.talkingpuffin.snippet

import scala.collection.JavaConversions._
import xml.Text
import net.liftweb.widgets.flot._
import net.liftweb.http.js.JsCmds._
import net.liftweb.common._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.util.StringHelpers._
import twitter4j.{Status, Twitter}
import org.talkingpuffin.apix.RichStatus._
import org.talkingpuffin.util.EscapeHtml
import org.talkingpuffin.apix.PartitionedTweets

object UserTimelinePlotRenderer {
  /**
   * Renders JavaScript to create a user timeline tweet-reading plot.
   */
  def render(pt: PartitionedTweets, screenName: String): JsCmd = {
    Flot.renderJs("ph_graph", List(
      newSer("Tweets"     , pt.plainTweets),
      newSer("Replies"    , pt.replies),
      newSer("Retweets"   , pt.newStyleRts),
      newSer("OldRetweets", pt.oldStyleRts)),
      createFlotOptions, Flot.script(Text(""))) & emitTweetsJs(pt)
  }

  private def newSer(heading: String, statuses: Seq[Status]) =
    new FlotSerie() {
      override def label = Full(heading)

      override val points = Full(new FlotPointsOptions() {
        override val show = Full(true) })

      override val data = statuses.map(_.getCreatedAt.getTime).sorted.
        map(t => Pair(t.toDouble, 1.toDouble)).toList
    }

  private def createFlotOptions = new FlotOptions {
    override def legend = Full(new FlotLegendOptions() {
      override def container = Full("#legend") })

    override val grid = Full(new FlotGridOptions() {
      override def hoverable = Full(true) })

    override def xaxis = Full(new FlotAxisOptions {
      override def mode = Full("time") })

    override def yaxis = Full(new FlotAxisOptions {
      override def ticks = List(0d) })
  }

  private def emitTweetsJs(pt: PartitionedTweets): JsCmd =
    JsCrVar("tweets", JsRaw("{" +
      pt.tweets.map(st => st.getCreatedAt.getTime.toString + ": " +
      st.text.encJs).mkString(",\n") + "}"))

}
