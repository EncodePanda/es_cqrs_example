package bloggers.domain

import akka.actor.Props


object BloggerAggregate {

  import AggregateRoot._

  case class Blogger(id: String, firstName: String, lastName: String) extends State

  case class Initialize(firstName: String, lastName: String) extends Command

  case class Iniitialized(firstName: String, lastName: String) extends Event

  def props(id: String): Props = Props(new BloggerAggregate(id))
}


class BloggerAggregate(id: String) extends AggregateRoot {

  import BloggerAggregate._
  import AggregateRoot._

  override def persistenceId: String = id

  override def updateState(evt: Event): Unit = evt match {
    case Iniitialized(fn, ln) =>
      state = new Blogger(id, fn, ln)
  }


  override def receiveCommand: Receive = {
    case Initialize(fn, ln) =>
      persist(Iniitialized(fn, ln))(afterEventPersisted)
  }
}
