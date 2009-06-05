/*
 * TwitterAPIArgs.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.talkingpuffin.twitter

class TwitterArgs(val sinceId:Option[Int], val maxId:Option[Int], val count:Option[Int], val page:Option[Int]) {
  def since(since:Int):TwitterArgs = {
    if(since > 0){
      new TwitterArgs(Some(since),maxId,count,page)
    }else{
      this
    }
  }

  def upToId(maxId:Int):TwitterArgs = {
    if(maxId > 0){
      new TwitterArgs(sinceId,Some(maxId),count,page)
    }else{
      this
    }
  }

  def maxResults(count:Int):TwitterArgs = {
    if(count > 0 && count <= 200){
      new TwitterArgs(sinceId,maxId,Some(count),page)
    } else {
      this
    }
  }

  def page(page:Int):TwitterArgs = {
    if(page >= 0){
      new TwitterArgs(sinceId,maxId,count,Some(page))
    } else {
      this
    }
  }

  override def toString():String = {
    val argsMap = Map("since_id" -> sinceId, "max_id" -> maxId, "count" -> count, "page" -> page)
    argsMap.filter(_._2.isDefined)
      .map((argTuple) => argTuple._1 + "=" + argTuple._2.get())
      .mkString("?","&","")
  }
}

object TwitterArgs{
  def apply():TwitterArgs = new TwitterArgs(None,None,None,None)
  def since(since:Int):TwitterArgs = {
    new TwitterArgs(Some(since),None,None,None)
  }

  def upToId(maxId:Int):TwitterArgs = {
    new TwitterArgs(None,Some(maxId),None,None)
  }

  def maxResults(count:Int):TwitterArgs = {
    new TwitterArgs(None,None,Some(count),None)
  }

  def page(page:Int):TwitterArgs = {
    new TwitterArgs(None,None,None,Some(page))
  }
}
