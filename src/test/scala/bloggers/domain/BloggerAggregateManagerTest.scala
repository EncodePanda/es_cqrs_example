package bloggers.domain

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestActorRef
import akka.util.Timeout
import bloggers.domain.AggregateRoot.Command
import bloggers.domain.blogger.{BloggerAggregateManager, BloggerAggregate}
import BloggerAggregateManager.{Do, Begin, AppCmd}
import bloggers.domain.blogger.BloggerAggregate
import scala.concurrent.duration._
import scala.concurrent.{Future, Await}
import org.scalatest.{Matchers, BeforeAndAfterAll, FunSuite}
import akka.pattern.ask

class BloggerAggregateManagerTest extends FunSuite with Matchers with BeforeAndAfterAll {

  import BloggerAggregate._

  implicit val actorySystem = ActorSystem("bloggerTestActorSystem")

  import scala.language.postfixOps

  implicit val timeout = Timeout(2 seconds)

  implicit val executionContext = actorySystem.dispatcher

  override def afterAll = {
    actorySystem.shutdown
  }

  test("that manager internally creates BloggerAggregate") {
    // given
    val manager = TestActorRef(BloggerAggregateManager.props)
    val childrensCount = manager.children.size
    // when
    manager ! Begin(Initialize("paul", "szulc"))
    // then
    manager.children.size should equal(childrensCount + 1)
  }

  test("that aggregate is initialized with initial state") {
    // given
    implicit val manager = TestActorRef(BloggerAggregateManager.props)
    // when
    val blogger = commanded(Begin(Initialize("paul", "szulc")))
    // then
    blogger match {
      case Blogger(_, "paul", "szulc", _) =>
      case sthElse => fail("not a blogger we've expected, got " + sthElse)
    }
  }

  test("that two bloggers can be befriended") {
    // given
    implicit val manager = TestActorRef(BloggerAggregateManager.props)
    val paul = commanded(Begin(Initialize("paul", "szulc")))
    // when
    val magda = commanded(Begin(Initialize("magda", "szulc")), id => Seq(Do(id, Befriend(paul.id))))
    // then
    magda match {
      case Blogger(magda.id, "magda", "szulc", List(paul.id)) =>
    }
  }

  test("that blogger can unfriend blogger") {
    // given
    implicit val manager = TestActorRef(BloggerAggregateManager.props)
    val paul = commanded(Begin(Initialize("paul", "szulc")))
    val magda = commanded(Begin(Initialize("magda", "szulc")), id => Seq(Do(id, Befriend(paul.id)), Do(id, Unfriend(paul.id))))
    // when
    // then
    magda match {
      case Blogger(magda.id, "magda", "szulc", List()) =>
    }
  }

  private def commanded(initial: AppCmd, seq: (String) => Seq[AppCmd] = (id => Seq.empty))
                       (implicit manager: ActorRef): Blogger = {
    val future = (manager ? initial).mapTo[Blogger]
    val initialState = Await.result(future, 2 seconds)
    seq(initialState.id).foldLeft(initialState) {
      case (state, cmd) =>
        val future = (manager ? cmd).mapTo[Blogger]
        Await.result(future, 2 seconds)
    }
  }
}
