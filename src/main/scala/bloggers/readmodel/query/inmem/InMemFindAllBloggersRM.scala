package bloggers.readmodel.query.inmem

import bloggers.readmodel.query.api.FindAllBloggersRM
import bloggers.readmodel.query.api.FindAllBloggersRM.Blogger

class InMemFindAllBloggersRM extends FindAllBloggersRM {

  var internal = Map[String, Blogger]()

  override def insertBlogger(blogger: Blogger): Unit = {
    internal += (blogger.id -> blogger)
  }

  override def query: Seq[Blogger] = internal.values.toSeq

  def clear = {
    internal = Map[String, Blogger]()
  }
}
