package na.datapipe.sink.producers.db.mongo

import akka.actor.{Actor, ActorLogging, Props}
import na.datapipe.model.{Commands, Command}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

/**
 * @author nader albert
 * @since  5/11/2015.
 */
class MongoSink extends Actor with ActorLogging with MongoConnector with PillMongoDao with TextPillMongoDao with SocialPillMongoDao {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def preStart() = connect("data-pipe")

  override def receive: Receive = {
    case command: Command if Commands ? command == Commands.SwallowCommand =>
      log info ("swallow msg received !" + command)
      save(command.pill).fallbackTo(Future.failed(new IllegalArgumentException))foreach {
        case exception: IllegalArgumentException => log error "Failed to communicate with Mongo Database"
        case result: WriteResult if result.hasErrors => log error "Errors encountered while attempting to persist to document database " + result.errmsg
        case result: WriteResult =>
      }
  }
}

object MongoSink {
  def props = Props(classOf[MongoSink])
}
