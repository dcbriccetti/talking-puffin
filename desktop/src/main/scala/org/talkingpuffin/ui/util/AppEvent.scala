package org.talkingpuffin.ui.util

import org.talkingpuffin.Session
import swing.event.Event

abstract case class AppEvent(session: Session) extends Event 