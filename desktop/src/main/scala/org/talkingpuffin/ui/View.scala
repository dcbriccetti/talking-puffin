package org.talkingpuffin.ui

import swing.Reactor
import javax.swing.JComponent

case class View(model: StatusTableModel, pane: StatusPane, frame: Option[JComponent]) extends Reactor
