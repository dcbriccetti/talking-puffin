package org.talkingpuffin.snippet

import xml.NodeSeq
import net.liftweb.widgets.tablesorter.{TableSorter, Sorting, Sorter}

class People {

  def tableSorter(content: NodeSeq): NodeSeq = {
    val headers = (2,Sorter("string")) :: Nil
    val sortList = (2,Sorting.ASC) :: Nil

    val options = TableSorter.options(headers,sortList)
    TableSorter("#users", options)
  }
}
