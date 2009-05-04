package org.talkingpuffin.ui.util

import java.awt.Desktop
import java.net.URI

object DesktopUtil {
  def browse(uri: String) {
    if (Desktop.isDesktopSupported) {
      Desktop.getDesktop.browse(new URI(uri))
    }
  }
  
}