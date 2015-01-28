package bloggers.readmodel.query.api


trait FindAllBloggersRM {

  import bloggers.readmodel.query.api.FindAllBloggersRM.Blogger

  def insertBlogger(blogger: Blogger)

  def query: Seq[Blogger]

  def clear: Unit
}

object FindAllBloggersRM {

  case class Blogger(id: String, firstName: String, lastName: String)

}
