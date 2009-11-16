package org.talkingpuffin.twitter

class TwitterArgs(val sinceId:Option[Long], val maxId:Option[Long], val count:Option[Int], 
     val page:Option[Int], val cursor: Option[Long], val screenName: Option[String]) {
  
  def since(since:Long):TwitterArgs = {
    if(since > 0L){
      new TwitterArgs(Some(since),maxId,count,page,cursor,screenName)
    }else{
      this
    }
  }

  def upToId(maxId:Long):TwitterArgs = {
    if(maxId > 0L){
      new TwitterArgs(sinceId,Some(maxId),count,page,cursor,screenName)
    }else{
      this
    }
  }

  def maxResults(count:Int):TwitterArgs = {
    if(count > 0 && count <= Constants.MaxItemsPerRequest){
      new TwitterArgs(sinceId,maxId,Some(count),page,cursor,screenName)
    } else {
      this
    }
  }

  def page(page:Int):TwitterArgs = {
    if(page >= 0){
      new TwitterArgs(sinceId,maxId,count,Some(page),cursor,screenName)
    } else {
      this
    }
  }

  def cursor(cursor: Long): TwitterArgs = new TwitterArgs(sinceId,maxId,count,page,Some(cursor),screenName)

  def screenName(screenName: String): TwitterArgs = 
      new TwitterArgs(sinceId,maxId,count,page,cursor,Some(screenName))
  
  override def toString():String = {
    Map("since_id" -> sinceId, "max_id" -> maxId, "count" -> count, 
        "per_page" -> count, // list status method uses this instead of count 
        "page" -> page, "cursor" -> cursor, "screen_name" -> screenName).
        filter(_._2.isDefined).map((pv) => pv._1 + "=" + pv._2.get).mkString("?","&","")
  }
}

object TwitterArgs{
  def apply():TwitterArgs = new TwitterArgs(None,None,None,None,None,None)
  
  def since(since:Long):TwitterArgs = new TwitterArgs(Some(since),None,None,None,None,None)

  def upToId(maxId:Long):TwitterArgs = new TwitterArgs(None,Some(maxId),None,None,None,None)

  def maxResults(count:Int):TwitterArgs = new TwitterArgs(None,None,Some(count),None,None,None)

  def page(page:Int):TwitterArgs = new TwitterArgs(None,None,None,Some(page),None,None)
  
  def cursor(cursor: Long): TwitterArgs = new TwitterArgs(None,None,None,None,Some(cursor),None)
  
  def screenName(screenName: String): TwitterArgs = new TwitterArgs(None,None,None,None,None,Some(screenName))
}
