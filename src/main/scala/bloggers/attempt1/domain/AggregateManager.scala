package bloggers.attempt1.domain

import java.util.UUID

import akka.actor._

object AggregateManager {

  trait Command

  val maxChildren = 40
  val childrenToKillAtOnce = 20

  case class PendingCommand(sender: ActorRef, targetProcessorId: String, command: AggregateRoot.Command)

}


trait AggregateManager extends Actor with ActorLogging {

  import AggregateRoot._
  import AggregateManager._

  import scala.collection.immutable._

  private var childrenBeingTerminated: Set[ActorRef] = Set.empty
  private var pendingCommands: Seq[PendingCommand] = Nil

  def processCommand: Receive

  override def receive = processCommand orElse defaultProcessCommand

  def generateGlobalId = UUID.randomUUID().toString()

  private def defaultProcessCommand: Receive = {
    case Terminated(actor) =>
      childrenBeingTerminated = childrenBeingTerminated filterNot (_ == actor)
      val (commandsForChild, remainingCommands) = pendingCommands partition (_.targetProcessorId == actor.path.name)
      pendingCommands = remainingCommands
      log.debug("Child termination finished. Applying {} cached commands.", commandsForChild.size)
      for (PendingCommand(commandSender, targetProcessorId, command) <- commandsForChild) {
        val child = findOrCreate(targetProcessorId)
        child.tell(command, commandSender)
      }
  }

  def processAggregateCommand(aggregateId: String, command: AggregateRoot.Command) = {
    val maybeChild = context child aggregateId
    maybeChild match {
      case Some(child) if childrenBeingTerminated contains child =>
        log.debug("Received command for aggregate currently being killed. Adding command to cache.")
        pendingCommands :+= PendingCommand(sender(), aggregateId, command)
      case Some(child) =>
        child forward command
      case None =>
        val child = create(aggregateId)
        child forward command
    }
  }

  protected def findOrCreate(id: String): ActorRef =
    context.child(id) getOrElse create(id)

  protected def create(id: String): ActorRef = {
    killChildrenIfNecessary()
    val agg = context.actorOf(aggregateProps(id), id)
    context watch agg
    viewProps(id).map(context.actorOf(_))
    agg
  }

  def aggregateProps(id: String): Props

  def viewProps(id: String): Option[Props]

  private def killChildrenIfNecessary() = {
    val childrenCount = context.children.size - childrenBeingTerminated.size
    if (childrenCount >= maxChildren) {
      log.debug(s"Max manager children exceeded. Killing ${childrenToKillAtOnce} children.")
      val childrenNotBeingTerminated = context.children.filterNot(childrenBeingTerminated.toSet)
      val childrenToKill = childrenNotBeingTerminated take childrenToKillAtOnce
      childrenToKill foreach (_ ! KillAggregate)
      childrenBeingTerminated ++= childrenToKill
    }
  }

}