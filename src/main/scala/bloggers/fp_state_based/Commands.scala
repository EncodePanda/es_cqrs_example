package bloggers.fp_state_based

import bloggers.fp_state_based.Events._

import scalaz._
import Scalaz._

object Events {

  trait Event

  case class Initialized(id: String, firstName: String, lastName: String) extends Event

  case class Befriended(friendId: String) extends Event

  case class Unfriended(friendId: String) extends Event

  case class MadeEnemy(enemyId: String) extends Event

  case class MadePeace(enemyId: String) extends Event

  case class Deactivated(reason: String) extends Event

  def applyEvent(e: Event) = State[Blogger, Event] { blogger =>
    e match {
      case Initialized(id, fn, ln) => (blogger.copy(id = id, firstName = fn, lastName = ln), e)
      case Befriended(friendId) => (blogger.copy(friends = friendId :: blogger.friends), e)
      case Unfriended(friendId) => (blogger.copy(friends = blogger.friends.filterNot(_ == friendId)), e)
      case MadeEnemy(enemyId) => (blogger.copy(enemies = enemyId :: blogger.enemies), e)
      case MadePeace(enemyId) => (blogger.copy(enemies = blogger.enemies.filterNot(_ == enemyId)), e)
      case Deactivated(reason) => (blogger.copy(active = false), e)
    }
  }

  def applyEvents(es: List[Event]) = State[Blogger, Unit] { blogger =>
    val applied: Blogger = es.foldLeft(blogger)((b, e) => applyEvent(e).run(b)._1)
    (applied, Unit)
  }

}


object Commands {

  def initialize(id: String, fn: String, ln: String) = State[Blogger, Event] { s =>
    applyEvent(Initialized(id, fn, ln)).run(s)
  }

  def befriend(friendId: String) = State[Blogger, Event] {
    applyEvent(Befriended(friendId)).run(_)
  }

  def unfriend(friendId: String) = State[Blogger, Event] {
    applyEvent(Unfriended(friendId)).run(_)
  }

  def makeEnemy(enemyId: String) = State[Blogger, Event] {
    applyEvent(MadeEnemy(enemyId)).run(_)
  }

  def makePeace(enemyId: String) = State[Blogger, Event] {
    applyEvent(MadePeace(enemyId)).run(_)
  }

  def deactivate(reason: String) = State[Blogger, Event] {
    applyEvent(Deactivated(reason)).run(_)
  }

}
