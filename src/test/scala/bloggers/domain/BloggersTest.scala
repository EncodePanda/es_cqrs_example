package bloggers.domain

import java.io.File

import akka.actor.{Props, ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import bloggers.domain.BloggerAggregateManager.{AppCmd, Begin, Do}
import bloggers.readmodel.query.api.FindAllBloggersRM
import bloggers.readmodel.query.inmem.InMemFindAllBloggersRM
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._


class BloggersTest extends FunSuite with Matchers with BeforeAndAfterAll with BeforeAndAfter {

  import bloggers.domain.BloggerAggregate._

  implicit val actorySystem = ActorSystem("bloggerTestActorSystem")

  import scala.language.postfixOps

  implicit val timeout = Timeout(2 seconds)

  implicit val executionContext = actorySystem.dispatcher

  val findAllQuery: FindAllBloggersRM = new InMemFindAllBloggersRM

  before {
    findAllQuery.clear
  }

  override def afterAll = {
    actorySystem.shutdown
  }

  test("that manager internally creates BloggerAggregate") {
    // given
    implicit val manager = createManager
    val childrensCount = manager.children.size
    // when
    manager ! Begin(Initialize("paul", "szulc"))
    // then
    manager.children.size should equal(childrensCount + 2)
  }

  test("that aggregate is initialized with initial state") {
    // given
    implicit val manager = createManager
    // when
    val blogger = commanded(Begin(Initialize("paul", "szulc")))
    // then
    blogger match {
      case Blogger(_, "paul", "szulc", _, _) =>
      case sthElse => fail("not a blogger we've expected, got " + sthElse)
    }
  }

  test("that two bloggers can be befriended") {
    // given
    implicit val manager = createManager
    val paul = commanded(Begin(Initialize("paul", "szulc")))
    // when
    val magda = commanded(Begin(Initialize("magda", "szulc")), id => Seq(Do(id, Befriend(paul.id))))
    // then
    magda match {
      case Blogger(magda.id, "magda", "szulc", List(paul.id), List()) =>
    }
  }

  test("that blogger can unfriend blogger") {
    // given
    implicit val manager = createManager
    val paul = commanded(Begin(Initialize("paul", "szulc")))
    val magda = commanded(Begin(Initialize("magda", "szulc")), id => Seq(
      Do(id, Befriend(paul.id)),
      Do(id, Unfriend(paul.id))))
    // when
    // then
    magda match {
      case Blogger(magda.id, "magda", "szulc", List(), List()) =>
    }
  }

  test("that two bloggers can become enemies") {
    // given
    implicit val manager = createManager
    val eric = commanded(Begin(Initialize("eric", "cartman")))
    // when
    val paul = commanded(Begin(Initialize("paul", "szulc")), id => Seq(Do(id, MakeEnemy(eric.id))))
    // then
    paul match {
      case Blogger(paul.id, "paul", "szulc", List(), List(eric.id)) =>
    }
  }

  test("that can list all bloggers") {
    // given
    implicit val manager = createManager
    // when
    val paul = commanded(Begin(Initialize("paul", "szulc")))
    val magda = commanded(Begin(Initialize("magda", "szulc")))
    //    then
    // TODO can we do better then sleep?
    Thread.sleep(6000)
    // TODO think how to clean journal between tests
    findAllQuery.query.size should equal(9)
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

  private def createManager: TestActorRef[BloggerAggregateManager] = {
    TestActorRef(BloggerAggregateManager.props(findAllQuery))
  }
}
