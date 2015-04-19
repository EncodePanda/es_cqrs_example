package bloggers.attempt1.domain

import java.util.UUID

import akka.actor.Props
import bloggers.attempt1.domain.AggregateRoot.Command
import bloggers.attempt1.domain.BloggerAggregate.Initialize
import bloggers.attempt1.domain.BloggerAggregateManager.{Do, Begin}
import bloggers.attempt1.readmodel.BloggerPersistentView
import bloggers.attempt1.readmodel.query.api.FindAllBloggersRM


object BloggerAggregateManager {

  sealed trait AppCmd

  case class Begin(init: Initialize) extends AppCmd

  case class Do(id: String, command: Command) extends AppCmd

  def props(findAll: FindAllBloggersRM): Props = Props(new BloggerAggregateManager(findAll))
}

class BloggerAggregateManager(findAll: FindAllBloggersRM) extends AggregateManager {

  override def processCommand: Receive = {
    case begin: Begin => processAggregateCommand(generateGlobalId, begin.init)
    case doo: Do => processAggregateCommand(doo.id, doo.command)
  }

  override def aggregateProps(id: String): Props = BloggerAggregate.props(id)

  override def viewProps(id: String): Option[Props] = Some(BloggerPersistentView.props(id, findAll))
}
