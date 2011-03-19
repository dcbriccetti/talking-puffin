package org.talkingpuffin.util

import org.joda.time.DateTime

case class CachedExpandedUrl(lastUsed: DateTime, url: Option[String])
