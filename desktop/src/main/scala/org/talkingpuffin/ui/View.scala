package org.talkingpuffin.ui

import swing.{Reactor}

case class View(val model: StatusTableModel, val pane: StatusPane, val frame: Option[TitledStatusFrame]) 
    extends Reactor 

