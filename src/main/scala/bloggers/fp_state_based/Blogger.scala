package bloggers.fp_state_based

import bloggers.build_on_top_ap.domain.AggregateRoot.{Event, Command}

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

