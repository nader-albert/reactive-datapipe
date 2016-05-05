package na.datapipe.sink.producers.db.mongo

import akka.actor.{Actor, ActorLogging, Props}
import na.datapipe.model.{Commands, Command}
import reactivemongo.api.commands.WriteResult
import reactivemongo.core.errors.ConnectionNotInitialized

/**
 * @author nader albert
 * @since  5/11/2015.
 */
class MongoSink extends Actor with ActorLogging with MongoConnector with PillMongoDao with TextPillMongoDao with SocialPillMongoDao {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def preStart() = connect("data_pipe")

  override def receive: Receive = {
    case command: Command if Commands ? command == Commands.SwallowCommand =>

      save(command.pill).collect {
        case result: WriteResult if result.hasErrors => log error "Errors encountered while attempting to persist to document database " + result.errmsg
      } onFailure {
          case connectionException: ConnectionNotInitialized => log error ("mongo connection problem" + connectionException)
      }
  }
}

object MongoSink {
  def props = Props(classOf[MongoSink])
}
