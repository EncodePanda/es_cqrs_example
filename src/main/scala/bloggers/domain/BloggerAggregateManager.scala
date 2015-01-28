package bloggers.domain

import java.util.UUID

import akka.actor.Props
import bloggers.domain.AggregateRoot.Command
import bloggers.domain.BloggerAggregate.Initialize
import bloggers.domain.BloggerAggregateManager.{Do, Begin}
import bloggers.readmodel.BloggerPersistentView
import bloggers.readmodel.query.api.QueryFindAllBloggers


object BloggerAggregateManager {

  sealed trait AppCmd

  case class Begin(init: Initialize) extends AppCmd

  case class Do(id: String, command: Command) extends AppCmd

  def props(findAll: QueryFindAllBloggers): Props = Props(new BloggerAggregateManager(findAll))
}

class BloggerAggregateManager(findAll: QueryFindAllBloggers) extends AggregateManager {

  override def processCommand: Receive = {
    case begin: Begin => processAggregateCommand(generateGlobalId, begin.init)
    case doo: Do => processAggregateCommand(doo.id, doo.command)
  }

  override def aggregateProps(id: String): Props = BloggerAggregate.props(id)

  override def viewProps(id: String): Option[Props] = Some(BloggerPersistentView.props(id, findAll))
}
