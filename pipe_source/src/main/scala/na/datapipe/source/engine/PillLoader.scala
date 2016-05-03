package na.datapipe.source.engine

import akka.actor.{ActorLogging, Terminated, Actor, ActorRef}
import na.datapipe.model.{Commands, Command, TextPill}
import na.datapipe.source.model.{LineLoaded, TransformerJoined}
import na.datapipe.transformer.model.PillTransformed

/**
 * @author nader albert
 * @since  4/08/2015.
 */
trait PillLoader extends Actor with ActorLogging {

  var transformers = IndexedSeq.empty[ActorRef]
  var jobCounter = 0

  override def preRestart(reason: Throwable, message: Option[Any]){
    log warning "actor is restarting because of a runtime failure while processing the following message: " + message.getOrElse(" EMPTY !")
  }

  override def postRestart(reason: Throwable){
    log info "actor commencing again after dropping the line that caused the error with " + reason.getMessage
  }

  override def receive: Receive = {
    case command :Command if Commands ? command == Commands.LoadCommand =>
      if (transformers isEmpty) log error "no available transformers !"
      else {
        jobCounter += 1

        log info s"loading.... $command.pill.body"

        transformers(jobCounter % transformers.size) ! Commands.TRANSFORM(command.pill.asInstanceOf[TextPill], 5)
      }
    case PillTransformed(id) => context.parent ! LineLoaded(id)

    case TransformerJoined if !transformers.contains(sender) =>
      log info "a new transformer added to the list !"
      context watch sender
      transformers = transformers :+ sender

    case Terminated(a) =>
      log info "one transformer has left cluster ! " + "[ " + a + " ]"
      transformers = transformers.filterNot(_ == a)
  }

  val transformCommand: (TextPill,Int) => Command
}
