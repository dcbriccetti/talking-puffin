package org.talkingpuffin.twitter

/**
 * Translates special values from twitter4j to None.
 */
trait OptionMaker {

  protected def makeOption[T](value: Any): Option[T] = value match {
    case null => None
    case "" => None
    case -1 => None
    case t: T => Some(t)
  }

  protected def makeEmptyString(value: String): String = value match {
    case null => ""
    case s => s
  }
}