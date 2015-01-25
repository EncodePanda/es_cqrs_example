package bloggers.domain

import akka.actor.Props


object BloggerAggregate {

  import AggregateRoot._

  case class Blogger(id: String, firstName: String, lastName: String, friends: List[String] = List()) extends State

  case class Initialize(firstName: String, lastName: String) extends Command
  case class Befriend(friendId: String) extends Command

  case class Unfriend(friendId: String) extends Command

  case class Initialized(firstName: String, lastName: String) extends Event
  case class Befriended(friendId: String) extends Event

  case class Unfriended(friendId: String) extends Event

  def props(id: String): Props = Props(new BloggerAggregate(id))
}


class BloggerAggregate(id: String) extends AggregateRoot {

  import BloggerAggregate._
  import AggregateRoot._

  override def persistenceId: String = id

  override def updateState(evt: Event): Unit = evt match {
    case Initialized(fn, ln) =>
      context become created
      state = new Blogger(id, fn, ln)
    case Befriended(fId) =>
      val blogger = state.asInstanceOf[Blogger]
      state = blogger.copy(friends = fId :: blogger.friends)
    case Unfriended(fId) =>
      val blogger = state.asInstanceOf[Blogger]
      state = blogger.copy(friends = blogger.friends.filter(_ != fId))

  }

  def init: Receive = {
    case Initialize(fn, ln) =>
      persist(Initialized(fn, ln))(afterEventPersisted)
  }

  def created: Receive = {
    case Befriend(friendId) =>
      persist(Befriended(friendId))(afterEventPersisted)
    case Unfriend(friendId) =>
      persist(Unfriended(friendId))(afterEventPersisted)
  }

  override def receiveCommand: Receive = init
}
