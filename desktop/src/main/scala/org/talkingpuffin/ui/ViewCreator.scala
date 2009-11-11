package org.talkingpuffin.ui

import java.util.prefs.Preferences
import java.awt.Point

trait ViewCreator {
  def createView(dataProvider: DataProvider, include: Option[String], location: Option[Point]): View
  val providers: DataProviders
  val prefs: Preferences
}