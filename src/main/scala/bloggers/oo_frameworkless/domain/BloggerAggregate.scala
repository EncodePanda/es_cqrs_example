package bloggers.oo_frameworkless.domain


case class Blogger(id: String = "",
                   firstName: String = "",
                   lastName: String = "",
                   friends: List[String] = List(),
                   enemies: List[String] = List(),
                   active: Boolean = true) {
}

object Blogger {
  def uninitialized = Blogger()
}

trait Event

case class Initialized(id: String, firstName: String, lastName: String) extends Event

case class Befriended(friendId: String) extends Event

case class Unfriended(friendId: String) extends Event

case class MadeEnemy(enemyId: String) extends Event

case class MadePeace(enemyId: String) extends Event

case class Deactivated(reason: String) extends Event


case class BloggerAggregate(val blogger: Blogger = Blogger.uninitialized) {

  def updateState(e: Event): Blogger = e match {
    case Initialized(id, fn, ln) => blogger.copy(id = id, firstName = fn, lastName = ln)
    case Befriended(friendId) => blogger.copy(friends = friendId :: blogger.friends)
    case Unfriended(friendId) => blogger.copy(friends = blogger.friends.filterNot(_ == friendId))
    case MadeEnemy(enemyId) => blogger.copy(enemies = enemyId :: blogger.enemies)
    case MadePeace(enemyId) => blogger.copy(enemies = blogger.enemies.filterNot(_ == enemyId))
    case Deactivated(reason) => blogger.copy(active = false)
  }

  def initialize(id: String, fn: String, ln: String): (BloggerAggregate, Event) = {
    val event = Initialized(id, fn, ln)
    (new BloggerAggregate(updateState(event)), event)
  }

  def befriend(friendId: String): (BloggerAggregate, Event) = {
    val event = Befriended(friendId)
    (new BloggerAggregate(updateState(event)), event)
  }

  def unfriend(friendId: String): (BloggerAggregate, Event) = {
    val event = Unfriended(friendId)
    (new BloggerAggregate(updateState(event)), event)
  }

  def makeEnemy(enemyId: String): (BloggerAggregate, Event) = {
    val event = MadeEnemy(enemyId)
    (new BloggerAggregate(updateState(event)), event)
  }

  def makePeace(enemyId: String): (BloggerAggregate, Event) = {
    val event = MadePeace(enemyId)
    (new BloggerAggregate(updateState(event)), event)
  }

  def deactivate(reason: String): (BloggerAggregate, Event) = {
    val event = Deactivated(reason)
    (new BloggerAggregate(updateState(event)), event)
  }

}


