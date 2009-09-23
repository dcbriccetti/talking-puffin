package org.talkingpuffin.ui.util

class TitleCreator(baseName: String) {
  var index = 0
  def create: String = {
    index += 1
    if (index == 1) baseName else baseName + index
  }
}
  
