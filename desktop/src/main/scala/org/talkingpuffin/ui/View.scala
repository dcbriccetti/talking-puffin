package org.talkingpuffin.ui

import swing.Reactor

case class View(model: StatusTableModel, pane: StatusPane, frame: Option[TitledStatusInternalFrame]) extends Reactor
