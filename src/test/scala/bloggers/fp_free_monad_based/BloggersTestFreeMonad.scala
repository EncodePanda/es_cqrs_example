package bloggers.fp_free_monad_based

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, FunSuite}
import scalaz.Free
import scalaz.Free.{Return, Suspend}
import bloggers.fp_free_monad_based._
import bloggers.fp_free_monad_based.Domain._
import bloggers.fp_free_monad_based.Commands._

class BloggersTestFreeMonad extends FunSuite with Matchers with BeforeAndAfterAll with BeforeAndAfter {
  import EventImplicits._

  test("that command is initialized with initial state") {
    // init
    val script = Scripts.initAndDeactivate("John", "Doe")
    // when
    val result = TestIntepreter.interpret(script)
    // then
    result should equal(List("Initialized", "Deactivated"))
  }

  test("that Pure Interpreter processes events correctly") {
    // init
    val script = Scripts.initAndDeactivate("John", "Doe")
    // when
    val result = PureInterpreter.interpret(script)
    // then
    result should equal(Map(Id("1") -> Blogger(Id("1"),"John","Doe",false)))
  }

}

object TestIntepreter {
  import EventImplicits._

  def interpret[A](c: Free[Event, A], data: List[String] = List.empty): List[String] = c.resume.fold({
    case x @ Initialized(firstName, lastName, onInit) => interpret(onInit(Id("1")), "Initialized" :: data)
    case y @ Befriended(friendId, next) => interpret(next, "Befriended" :: data)
    case z @ Deactivated(id, reason, next) => interpret(next, "Deactivated" :: data)
  }, _ => data.reverse)
}