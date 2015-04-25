package bloggers.fp_free_monad_based

import scalaz.{State, Free, Functor}
import scalaz.Free.liftF

case class Id(value: String) extends AnyVal

// ADT for Events
trait Event[+Next]
case class Initialized[Next](firstName: String, lastName: String, onInit: Id => Next) extends Event[Next]
case class Befriended[Next](friendId: String, next: Next) extends Event[Next]
//case class Unfriended[Next](friendId: String, next: Next) extends Event[Next]
//case class MadeEnemy[Next](enemyId: String, next: Next) extends Event[Next]
//case class MadePeace[Next](enemyId: String, next: Next) extends Event[Next]
case class Deactivated[Next](id: Id, reason: String, next: Next) extends Event[Next]

object Events {

  implicit def functor: Functor[Event] = new Functor[Event] {
    override def map[A, B](fa: Event[A])(f: (A) => B): Event[B] = fa match {
      case i @ Initialized(fn, ln, onInit) => i.copy(onInit = onInit andThen f)
      case b @ Befriended(fId, next) => b.copy(next = f(next))
      case d @ Deactivated(id, reason, next) => d.copy(next = f(next))
    }
  }

}

/*
trait ApplicableEvents {
  def apply[S](e: Event): State[S, Event] = ???
  def applyEvents[S](es: Seq[Event]): State[S, Event] = ???
}
*/

object Commands extends Commands

trait Commands {
  import Events._
  import scala.language.implicitConversions

  type Command[A] = Free[Event, A]

  private implicit def liftEvent[Next](event: Event[Next]): Command[Next] = liftF(event)

  // TODO: add validation
  def initialize(firtstName: String, lastName: String): Command[Id] = Initialized(firtstName, lastName, identity)
  def befriend(friendId: String): Command[Unit] = Befriended(friendId, ())
  def deactivate(id: Id, reason: String): Command[Unit] = Deactivated(id, reason, ())

}

object Scripts extends Commands {

  def initAndDeactivate(firtstName: String, lastName: String): Command[Unit] = for {
    id <- initialize(firtstName, lastName)
    _ <- deactivate(id, reason = "deactivated by default")
  } yield ()

}

object Domain {
  case class Blogger(id: Id, firstName: String, lastName: String, active: Boolean = true)
}

object PureInterpreter extends App {
  import Commands._
  import Events._
  import Domain._

  val ID_GENERATOR = Id("1")

  def interpret[A](c: Command[A], data: Map[Id, Blogger] = Map.empty): Map[Id, Blogger] = c.resume.fold({
    case Initialized(firstName, lastName, onInit) => {
      val id = ID_GENERATOR
      interpret(onInit(id), data + (id -> Blogger(id, firstName, lastName)))
    }
    case Befriended(friendId, next) => {
      interpret(next, data)
    }
    case Deactivated(id, reason, next) => {
      println(s"reason: $reason")
      interpret(next, data + (id -> data(id).copy(active = false)))
    }
  }, _ => data)


  val result = interpret(Scripts.initAndDeactivate("Mateusz", "Maciaszek"))
  println(s"result: $result")
}