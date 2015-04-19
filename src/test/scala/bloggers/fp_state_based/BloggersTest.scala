package bloggers.fp_state_based

import bloggers.fp_state_based.Blogger.uninitialized
import bloggers.fp_state_based.Events.Deactivated
import bloggers.fp_state_based.Events._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, FunSuite}

class BloggersTest extends FunSuite with Matchers with BeforeAndAfterAll with BeforeAndAfter {

  var johnId: String = "johnId"
  var janeId: String = "janeId"

  test("that aggregate is initialized with initial state") {
    // when
    val john = Commands.initialize(johnId, "John", "Smith").run(uninitialized)
    // then
    john._1 should equal(Blogger(johnId, "John", "Smith", List(), List(), true))
    john._2 should equal(Initialized(johnId, "John", "Smith"))
  }

  test("that two bloggers can be befriended") {
    // given
    val recipe = for {
      init <- Commands.initialize(johnId, "John", "Smith")
      befriended <- Commands.befriend(janeId)
    } yield (befriended)
    // when
    val johnSocializing = recipe.run(uninitialized)
    // then
    johnSocializing._1 should equal(Blogger(johnId, "John", "Smith", List(janeId), List(), true))
    johnSocializing._2 should equal(Befriended(janeId))
  }

  test("that blogger can unfriend blogger") {
    // given
    val recipe = for {
      init <- Commands.initialize(johnId, "John", "Smith")
      befriended <- Commands.befriend(janeId)
      unfriended <- Commands.unfriend(janeId)
    } yield (unfriended)
    // when
    val johnUnsocializing = recipe.run(uninitialized)
    // then
    johnUnsocializing._1 should equal(Blogger(johnId, "John", "Smith", List(), List(), true))
    johnUnsocializing._2 should equal(Unfriended(janeId))
  }

  test("that two bloggers can become enemies") {
    // given
    val recipe = for {
      init <- Commands.initialize(johnId, "John", "Smith")
      atWar <- Commands.makeEnemy(janeId)
    } yield (atWar)
    // when
    val johnFighting = recipe.run(uninitialized)
    // then
    johnFighting._1 should equal(Blogger(johnId, "John", "Smith", List(), List(janeId), true))
    johnFighting._2 should equal(MadeEnemy(janeId))
  }

  test("that blogger can deactivate account") {
    val recipe = for {
      init <- Commands.initialize(johnId, "John", "Smith")
      deactivated <- Commands.deactivate("I'm out")
    } yield (deactivated)
    // when
    val johnFighting = recipe.run(uninitialized)
    // then
    johnFighting._1 should equal(Blogger(johnId, "John", "Smith", List(), List(), false))
    johnFighting._2 should equal(Deactivated("I'm out"))
  }
  //
  test("mr & mrs smith scenario") {
    val recipe = for {
      john <- Commands.initialize(johnId, "John", "Smith")
      johnInLove <- Commands.befriend(janeId)
      johnNotSoMuchInLove <- Commands.unfriend(janeId)
      johnBetrayed <- Commands.makeEnemy(janeId)
      johnForgave <- Commands.makePeace(janeId)
      johnBackInLove <- Commands.befriend(janeId)
      johnOnRetirement <- Commands.deactivate("Found love of my life")
    } yield (john :: johnInLove :: johnNotSoMuchInLove :: johnBetrayed :: johnForgave :: johnBackInLove :: johnOnRetirement :: Nil)
    val john = recipe.run(uninitialized)
    //then
    john._1 should equal(Blogger(johnId, "John", "Smith", List(janeId), List(), false))
    john._2 should equal(List(Initialized(johnId, "John", "Smith"), Befriended(janeId), Unfriended(janeId),
      MadeEnemy(janeId), MadePeace(janeId), Befriended(janeId),
      Deactivated("Found love of my life")))
  }
}