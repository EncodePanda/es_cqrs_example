package bloggers.domain

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Future, Await}
import org.scalatest.{Matchers, BeforeAndAfterAll, FunSuite}
import akka.pattern.ask

class BloggerAggregateManagerTest extends FunSuite with Matchers with BeforeAndAfterAll {

  import bloggers.domain.BloggerAggregate._

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
    manager ! Initialize("paul", "szulc")
    // then
    manager.children.size should equal(childrensCount + 1)
  }

  test("that aggregate is initialized with initial state") {
    // given
    val manager = TestActorRef(BloggerAggregateManager.props)
    // when
    val future = (manager ? Initialize("paul", "szulc")).mapTo[Blogger]
    val blogger = Await.result(future, 2 seconds)
    // then
    blogger match {
      case Blogger(_, "paul", "szulc") =>
      case sthElse => fail("not a blogger we've expected, got " + sthElse)
    }
  }
}
