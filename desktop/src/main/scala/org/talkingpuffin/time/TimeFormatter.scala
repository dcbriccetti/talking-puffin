package org.talkingpuffin.time

/**
 * Time formatter
 */
case class TimeFormatter(time: Long) {
  val days = time / 86400
  val hours = (time / 3600) - (days * 24)
  val minutes = (time / 60) - (days * 1440) - (hours * 60)
  val seconds = time % 60
  
  def colonSeparated: String = {
    val sb = new StringBuilder
    if (days > 0)                      sb.append(twoDigitNum(days))    .append(":")
    if (hours > 0    || sb.length > 0) sb.append(twoDigitNum(hours))   .append(":")
    if (minutes > 0  || sb.length > 0) sb.append(twoDigitNum(minutes)) .append(":")
    sb.append(twoDigitNum(seconds))
    sb.toString
  }
  
  def longForm: String = {
    val sb = new StringBuilder
    addPart(sb, days,    "d")
    addPart(sb, hours,   "h")
    addPart(sb, minutes, "m")
    addPart(sb, seconds, "s")
    
    def addPart(sb: StringBuilder, value: Long, name: String): Unit = {
      if (value > 0) {
        if (sb.length > 0) sb.append(' ')
        sb.append(value).append(' ').append(name)
      }
    }
    
    sb.toString
  }
  
  private def twoDigitNum(num: java.lang.Long) = String.format("%02d", num)
  
}
