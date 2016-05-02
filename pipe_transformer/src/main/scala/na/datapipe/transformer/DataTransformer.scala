package na.datapipe.transformer

import akka.actor.{Terminated, ActorRef, ActorLogging, Actor}
import na.datapipe.model._
import na.datapipe.process.model.ProcessorJoined
import na.datapipe.sink.model.SinkJoined

import na.datapipe.model.social.SocialInteraction

/**
 * @author nader albert
 * @since  5/08/2015.
 */
trait DataTransformer extends Actor with ActorLogging {


  var processingEngines = IndexedSeq.empty[ActorRef]
  var publisherEngines = IndexedSeq.empty[ActorRef]

  var jobCounter = 0

  override def postStop = log info "number of messages processed so far " + jobCounter

  override def preRestart(reason: Throwable, message: Option[Any]) =
    log error s"transformer is restarting because of: $reason"

  override def postRestart(reason: Throwable) = log error "transformer has been restarted"

  override def receive: Receive = {

    case command :Command if Commands ? command == Commands.TransformCommand =>
      jobCounter += 1

      //TODO: uncomment the processor access !
      /*
      if(processingEngines isEmpty) log warning "no processors available at the moment.. post mights still be persisted " +
        "for later processing, when at least one processor becomes available"
      else processingEngines(jobCounter % processingEngines.size) ! ProcessPost(SocialPost(rawData), msg.projectChannel.id)
      */
      if(publisherEngines isEmpty) log warning "no publishers available at the moment.. post couldn't be persisted for " +
        "later processing "
      else {
        //Send a SaveCanonicalPost message for each SocialPost returned by the transformer. Social Posts returned
        //are randomly distributed among the available publishes.. A Twitter Api Transformer always returns one post,
        //as a retweet is always enclosed with its parent tweet, into the same SocialPost (in the replyTo)

        transformPost(command.pill.asInstanceOf[TextPill]) //Assuming that the TransformCommand only accepts TextPills, so this must always be successful !
          .foreach(socialInteraction => publisherEngines(jobCounter % publisherEngines.size)
          ! Commands.SWALLOW(SocialPill(socialInteraction, Some(Map.empty.updated("persistence_channel","db/mongo")),0)))
      }

    //log info jobCounter.toString
    case ProcessorJoined if !processingEngines.contains(sender) =>
      log info "new processor just been added to the list !"
      context watch sender
      processingEngines = processingEngines :+ sender

      log info "number of processors is now: " + processingEngines.size

    case SinkJoined if !publisherEngines.contains(sender) =>
      log info "new publisher just added been added to the list !"
      context watch sender
      publisherEngines = publisherEngines :+ sender

      log info "number of publishers is now: " + publisherEngines.size

    case Terminated(actor) =>
      log info "either a processor or a publisher has just left the cluster ! " + "[ " + actor.path.name + " ]"

      //TODO: check the name of the actor that has been terminated, by referring to its path, rather than attempting to blindly remove it from both lists
      processingEngines = processingEngines.filterNot(_ == actor)
      publisherEngines = publisherEngines.filterNot(_ == actor)
  }

  protected def transformPost(pill: TextPill): List[SocialInteraction]
}
