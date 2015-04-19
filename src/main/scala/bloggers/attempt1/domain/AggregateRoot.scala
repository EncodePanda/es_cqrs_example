package bloggers.attempt1.domain

import akka.actor.Actor.Receive
import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SnapshotMetadata, SnapshotOffer}
import bloggers.attempt1.common.Acknowledge


object AggregateRoot {

  trait State

  case object Uninitialized extends State

  trait Event

  trait Command


  /**
   * We don't want the aggregate to be killed if it hasn't fully restored yet,
   * thus we need some non AutoReceivedMessage that can be handled by akka persistence.
   */
  case object KillAggregate extends Command

}

trait AggregateRoot extends PersistentActor with ActorLogging {

  import AggregateRoot._

  override def persistenceId: String

  protected var state: State = Uninitialized

  def updateState(evt: Event): Unit


  protected def afterEventPersisted(evt: Event): Unit = {
    updateState(evt)
    respond()
    publish(evt)
  }

  protected def respond(): Unit = {
    sender() ! state
    context.parent ! Acknowledge(persistenceId)
  }

  private def publish(event: Event) =
    context.system.eventStream.publish(event)

  override val receiveRecover: Receive = {
    case evt: Event => updateState(evt)
  }
}
