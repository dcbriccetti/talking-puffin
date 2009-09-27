package org.talkingpuffin.ui

import java.util.prefs.Preferences

trait ViewCreator {
  def createView(dataProvider: DataProvider, include: Option[String]): View
  val providers: DataProviders
  val prefs: Preferences
}