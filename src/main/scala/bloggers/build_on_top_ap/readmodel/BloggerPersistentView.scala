package bloggers.build_on_top_ap.readmodel

import akka.actor.Props
import akka.persistence.PersistentView
import bloggers.build_on_top_ap.domain.BloggerAggregate
import bloggers.build_on_top_ap.readmodel.query.api.FindAllBloggersRM

object BloggerPersistentView {
  def props(id: String, findAll: FindAllBloggersRM): Props = Props(new BloggerPersistentView(id, findAll))
}

class BloggerPersistentView(id: String, findAll: FindAllBloggersRM) extends PersistentView {
  override def persistenceId: String = id

  override def viewId: String = s"$id-view"

  override def receive: Receive = {
    case BloggerAggregate.Initialized(firstName, lastName) =>
      findAll.insertBlogger(FindAllBloggersRM.Blogger(id, firstName, lastName))
  }
}
