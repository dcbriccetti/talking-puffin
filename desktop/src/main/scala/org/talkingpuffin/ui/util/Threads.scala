package org.talkingpuffin.ui.util

import java.util.concurrent.Executors

object Threads {
  val pool = Executors.newCachedThreadPool
}