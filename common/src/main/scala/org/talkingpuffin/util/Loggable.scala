package org.talkingpuffin.util

import org.slf4j.LoggerFactory

trait Loggable {
  private val cn = getClass.getName
  val logger = LoggerFactory.getLogger(if (cn endsWith "$") cn.substring(0, cn.length - 1) else cn)
  def debug(msg: String) = logger debug msg
  def info (msg: String) = logger info  msg
  def warn (msg: String) = logger warn  msg
  def error(msg: String) = logger error msg
}
