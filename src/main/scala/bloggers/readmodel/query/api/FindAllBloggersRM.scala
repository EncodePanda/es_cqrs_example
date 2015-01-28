package bloggers.readmodel.query.api


trait QueryFindAllBloggers {

  import bloggers.readmodel.query.api.QueryFindAllBloggers.Blogger

  def insertBlogger(blogger: Blogger)

  def query: Seq[Blogger]

  def clear: Unit
}

object QueryFindAllBloggers {

  case class Blogger(id: String, firstName: String, lastName: String)

}
