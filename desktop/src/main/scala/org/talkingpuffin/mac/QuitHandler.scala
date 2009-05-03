package org.talkingpuffin.mac

import apache.log4j.Logger
import java.lang.reflect.{Proxy, InvocationHandler, Method}

/**
 * Mac OS application quit handler
 *
 * @author Mark McBride
 */

object QuitHandler {
  def register(callback: => Unit) {
    try {
      var log = Logger getLogger "QuitHandler"
 
      // For handling OSX shutdown stuff

      // use a proxy here, as we can't pull in the Mac ApplicationHandler class'
      class ShutdownHandler extends InvocationHandler {
        def invoke(proxy: Any, m: Method, args: Array[Object]): Object = {
          m.getName() match {
            case "handleQuit" => {
              log.info("application exiting ")
              callback
            }
            case _ =>
          }
        }
      }

      // look up the Mac Application class.  If it isn't found, we should
      // fall through to the ClassNotFoundException catch and just proceed'
      val applicationClass = Class.forName("com.apple.eawt.Application")
      val macOSXApplication = applicationClass.getConstructor().newInstance()
      val applicationListenerClass = Class.forName("com.apple.eawt.ApplicationListener")
      val addListenerMethod = applicationClass.getDeclaredMethod("addApplicationListener",
        applicationListenerClass);
      // create a proxy that implements ApplicationListener.  This is ugly, but since we can't actually pull 
      // in ApplicationListener this is pretty much the best we can do'
      val osxAdapterProxy = Proxy.newProxyInstance(getClass().getClassLoader(),
        Array(applicationListenerClass), new ShutdownHandler());
      addListenerMethod.invoke(macOSXApplication, osxAdapterProxy)
    } catch {
      // this is expected if not running on OSX
      case cnfe: ClassNotFoundException =>
    }

  }
}