package org.talkingpuffin.comet

import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.util.ActorPing
import org.apache.log4j.Logger
import org.talkingpuffin.util.UrlExpander
import org.talkingpuffin.snippet.{GeneralUserInfo, Auth}
import xml.{NodeSeq, Text}
import net.liftweb.common.Box
import net.liftweb.http.{LiftSession, CometActor}

class LinkExpander(initSession: LiftSession,
             initType: Box[String],
             initName: Box[String],
             initDefaultXml: NodeSeq,
             initAttributes: Map[String, String]) extends CometActor {

  initCometActor(initSession, initType, initName, initDefaultXml, initAttributes)
  val logger = Logger.getLogger(getClass.getName)
  val id = name.get
  logger.info("Starting for " + id)

  private case object Update

  def render = {
    schedule(0)
    Text("")
  }

  override def lowPriority = {
    case Update =>
      renderAvail()
  }

  private def renderAvail(): Unit = {
    Auth.userAnalysis.is match {
      case Some(ua) =>
        GeneralUserInfo.links(ua).find(l => l.url.hashCode.toString == id).foreach(l => {
        try {
          val expanded = UrlExpander.expand(l.url.toString)
          partialUpdate(SetHtml("link_" + l.url.hashCode,
            <span><a href={expanded}>{GeneralUserInfo.Link.stripFront(expanded)}</a><br/></span>))
        } catch {
          case ex: Throwable => logger.info(ex.getMessage)
        }
      }
      )
    }
  }

  private def schedule(secs: Int = 30) = ActorPing.schedule(this, Update, secs seconds)
}