package bloggers.build_on_top_ap.domain

import java.util.UUID

import akka.actor.Props
import bloggers.build_on_top_ap.domain.AggregateRoot.Command
import bloggers.build_on_top_ap.domain.BloggerAggregate.Initialize
import bloggers.build_on_top_ap.domain.BloggerAggregateManager.{Do, Begin}
import bloggers.build_on_top_ap.readmodel.BloggerPersistentView
import bloggers.build_on_top_ap.readmodel.query.api.FindAllBloggersRM


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
