package org.talkingpuffin.ui.util

import java.util.concurrent.atomic.AtomicBoolean
import org.talkingpuffin.state.{GlobalPrefs, PrefKeys}
import org.talkingpuffin.ui.SwingInvoke
import org.talkingpuffin.util.Threads

/**
 * An animator to make old text slide out and new text slide in.
 */
class TextChangingAnimator {
  private val keepAnimating = new AtomicBoolean

  def run(origText: String, newText: String, callback: String => Unit) {
    if (! GlobalPrefs.isOn(PrefKeys.USE_ANIMATIONS)) {
      callback(newText) // Simply show end result
    } else {
      keepAnimating.set(true)
      Threads.pool.execute(new Runnable {
        def run() {
          var shrinkingText = origText
          while (keepAnimating.get && shrinkingText.length > 0) {
            shrinkingText = shrinkingText.substring(1)
            SwingInvoke.later{callback(shrinkingText)}
            Thread.sleep(10)
          }
          val newTextBuf = new StringBuilder
          for (i <- 0 until newText.length()) {
            if (keepAnimating.get) {
              newTextBuf.append(newText.charAt(i))
              SwingInvoke.later{callback(newTextBuf.toString())}
              Thread.sleep(10)
            }
          }
        }
      })
    }
  }
  
  def stop() = keepAnimating.set(false)
}
