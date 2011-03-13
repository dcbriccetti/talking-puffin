package org.talkingpuffin.snippet

class LineCollector {
  var msgs = List[LineCollector.InfoLine]()
  def disp(heading: String, value: AnyRef) = msgs = LineCollector.InfoLine(heading, value) :: msgs
}

object LineCollector {
  case class InfoLine(heading: String, value: AnyRef)
}
