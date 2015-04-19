
package bloggers.oo_frameworkless.domain

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, FunSuite}

class BloggersTest extends FunSuite with Matchers with BeforeAndAfterAll with BeforeAndAfter {

  var johnId: String = "johnId"
  var janeId: String = "janeId"

  test("that aggregate is initialized with initial state") {
    // when
    val initialized = BloggerAggregate().initialize(johnId, "John", "Smith")
    // then
    initialized._1 should equal(BloggerAggregate(Blogger(johnId, "John", "Smith", List(), List(), true)))
    initialized._2 should equal(Initialized(johnId, "John", "Smith"))
  }

  test("that two bloggers can be befriended") {
    // given
    val john = BloggerAggregate().initialize(johnId, "John", "Smith")._1
    // when
    val johnSocializing = john.befriend(janeId)
    // then
    johnSocializing._1 should equal(BloggerAggregate(Blogger(johnId, "John", "Smith", List(janeId), List(), true)))
    johnSocializing._2 should equal(Befriended(janeId))
  }

  test("that blogger can unfriend blogger") {
    // given
    val john = BloggerAggregate().initialize(johnId, "John", "Smith")._1
    // when
    val johnUnsocializing = john.befriend(janeId)._1.unfriend(janeId)
    // then
    johnUnsocializing._1 should equal(BloggerAggregate(Blogger(johnId, "John", "Smith", List(), List(), true)))
    johnUnsocializing._2 should equal(Unfriended(janeId))
  }

  test("that two bloggers can become enemies") {
    // given
    val john = BloggerAggregate().initialize(johnId, "John", "Smith")._1
    // when
    val johnFighting = john.makeEnemy(janeId)
    // then
    johnFighting._1 should equal(BloggerAggregate(Blogger(johnId, "John", "Smith", List(), List(janeId), true)))
    johnFighting._2 should equal(MadeEnemy(janeId))
  }

  test("that blogger can deactivate account") {
    // given
    val john = BloggerAggregate().initialize(johnId, "John", "Smith")._1
    // when
    val johnDeactivated = john.deactivate("I'm out")
    // then
    johnDeactivated._1 should equal(BloggerAggregate(Blogger(johnId, "John", "Smith", List(), List(), false)))
    johnDeactivated._2 should equal(Deactivated("I'm out"))
  }

  test("mr & mrs smith scenario") {
    val john = BloggerAggregate().initialize(johnId, "John", "Smith")
    val johnInLove = john._1.befriend(janeId)
    val johnNotSoMuchInLove = johnInLove._1.unfriend(janeId)
    val johnBetrayed = johnNotSoMuchInLove._1.makeEnemy(janeId)
    val johnForgave = johnBetrayed._1.makePeace(janeId)
    val johnBackInLove = johnForgave._1.befriend(janeId)
    val johnOnRetirement = johnBackInLove._1.deactivate("Found love of my life")

    // then
    johnOnRetirement._1 should equal(BloggerAggregate(Blogger(johnId, "John", "Smith", List(janeId), List(), false)))
    List(john._2, johnInLove._2, johnNotSoMuchInLove._2,
      johnBetrayed._2, johnForgave._2, johnBackInLove._2,
      johnOnRetirement._2) should
      equal(List(Initialized(johnId, "John", "Smith"), Befriended(janeId), Unfriended(janeId),
        MadeEnemy(janeId), MadePeace(janeId), Befriended(janeId),
        Deactivated("Found love of my life")))
  }
}
