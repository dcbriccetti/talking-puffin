package com.davebsoft.sctw.ui.util
import java.util.concurrent.atomic.AtomicBoolean

/**
 * An animator to make old text slide out and new text slide in.
 * 
 * @author Dave Briccetti
 */
class TextChangingAnimator {
  val keepAnimating = new AtomicBoolean
  var thread: Thread = _
  
  def run(origText: String, newText: String, callback: (String) => Unit) {
    keepAnimating.set(true)
    thread = new Thread(new Runnable {
      def run = {
        var shrinkingText = origText
        while (keepAnimating.get && shrinkingText.length > 0) {
          shrinkingText = shrinkingText.substring(1)
          SwingInvoke.invokeLater({
            callback(shrinkingText)
          })
          Thread.sleep(10)
        }
        val newTextBuf = new StringBuilder
        for (i <- 0 until newText.length()) {
          if (keepAnimating.get) {
            newTextBuf.append(newText.charAt(i))
            SwingInvoke.invokeLater({
              callback(newTextBuf.toString)
            })
            Thread.sleep(10)
          }
        }
      }
    })
    thread.start
  }
  
  def stop = {
    keepAnimating.set(false)
    if (thread != null && thread.isAlive) {
      thread.join
      thread = null
    }
  }
}
