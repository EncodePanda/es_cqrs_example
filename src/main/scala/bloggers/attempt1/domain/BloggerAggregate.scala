package bloggers.attempt1.domain

import akka.actor.Props


object BloggerAggregate {

  import AggregateRoot._

  case class Blogger(id: String,
                     firstName: String,
                     lastName: String,
                     friends: List[String] = List(),
                     enemies: List[String] = List(),
                     active: Boolean = true
  ) extends State

  case class Initialize(firstName: String, lastName: String) extends Command
  case class Befriend(friendId: String) extends Command
  case class Unfriend(friendId: String) extends Command
  case class MakeEnemy(enemyId: String) extends Command

  case class MakePeace(enemyId: String) extends Command

  case class Deactivate(reason: String) extends Command

  case class Initialized(firstName: String, lastName: String) extends Event
  case class Befriended(friendId: String) extends Event
  case class Unfriended(friendId: String) extends Event
  case class MadeEnemy(enemyId: String) extends Event

  case class MadePeace(enemyId: String) extends Event

  case class Deactivated(reason: String) extends Event

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
    case MadeEnemy(eId) =>
      val blogger = state.asInstanceOf[Blogger]
      state = blogger.copy(enemies = eId :: blogger.enemies)
    case Deactivated(reasoen) =>
      context become deactivated
      val blogger = state.asInstanceOf[Blogger]
      state = blogger.copy(active = false)
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
    case MakeEnemy(enemyId) =>
      persist(MadeEnemy(enemyId))(afterEventPersisted)
    case Deactivate(reason) =>
      persist(Deactivated(reason))(afterEventPersisted)
  }

  def deactivated: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = init
}
