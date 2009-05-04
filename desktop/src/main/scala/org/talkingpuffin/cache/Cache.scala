package org.talkingpuffin.cache

/**
 * The start of some cache functionality.
 * 
 * @author Dave Briccetti
 */
trait Cache[K,V] {
  def store(cache: java.util.Map[K,V], key: K, value: V) {
    if (cache.size > 1000) 
      cache.clear // TODO clear LRU instead
    cache.put(key, value)
  }
}

