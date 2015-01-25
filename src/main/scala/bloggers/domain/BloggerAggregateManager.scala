package bloggers.domain

import java.util.UUID

import akka.actor.Props


object BloggerAggregateManager {
  def props: Props = Props(new BloggerAggregateManager)
}

class BloggerAggregateManager extends AggregateManager {

  override def processCommand: Receive = {
    case cmd: BloggerAggregate.Initialize => processAggregateCommand(generateGlobalId, cmd)
  }

  override def aggregateProps(id: String): Props = BloggerAggregate.props(id)
}
