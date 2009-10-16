package org.talkingpuffin.twitter

class TwitterArgs(val sinceId:Option[Long], val maxId:Option[Long], val count:Option[Int], 
     val page:Option[Int], val cursor: Option[Long]) {
  
  def since(since:Long):TwitterArgs = {
    if(since > 0L){
      new TwitterArgs(Some(since),maxId,count,page,cursor)
    }else{
      this
    }
  }

  def upToId(maxId:Long):TwitterArgs = {
    if(maxId > 0L){
      new TwitterArgs(sinceId,Some(maxId),count,page,cursor)
    }else{
      this
    }
  }

  def maxResults(count:Int):TwitterArgs = {
    if(count > 0 && count <= 200){
      new TwitterArgs(sinceId,maxId,Some(count),page,cursor)
    } else {
      this
    }
  }

  def page(page:Int):TwitterArgs = {
    if(page >= 0){
      new TwitterArgs(sinceId,maxId,count,Some(page),cursor)
    } else {
      this
    }
  }

  def cursor(cursor: Long): TwitterArgs = new TwitterArgs(sinceId,maxId,count,page,Some(cursor))

  override def toString():String = {
    Map("since_id" -> sinceId, "max_id" -> maxId, "count" -> count, "page" -> page, "cursor" -> cursor).
        filter(_._2.isDefined).map((argTuple) => argTuple._1 + "=" + argTuple._2.getOrElse(""))
        .mkString("?","&","")
  }
}

object TwitterArgs{
  def apply():TwitterArgs = new TwitterArgs(None,None,None,None,None)
  
  def since(since:Long):TwitterArgs = new TwitterArgs(Some(since),None,None,None,None)

  def upToId(maxId:Long):TwitterArgs = new TwitterArgs(None,Some(maxId),None,None,None)

  def maxResults(count:Int):TwitterArgs = new TwitterArgs(None,None,Some(count),None,None)

  def page(page:Int):TwitterArgs = new TwitterArgs(None,None,None,Some(page),None)
  
  def cursor(cursor: Long):TwitterArgs = new TwitterArgs(None,None,None,None,Some(cursor))
}
