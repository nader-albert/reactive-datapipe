package na.datapipe.sink.producers.db.mongo

import akka.actor.{Actor, ActorLogging, Props}
import na.datapipe.model.{Commands, Command}

/**
 * @author nader albert
 * @since  5/11/2015.
 */
class MongoSink extends Actor with ActorLogging with MongoConnector with PillMongoDao {

  override def preStart() = connect("customer_mind")

  override def receive: Receive = {
    case command: Command if Commands ? command == Commands.SwallowCommand =>
      log info ("swallow msg received !" + command)
      save(command.pill)
  }
}

object MongoSink {
  def props = Props(classOf[MongoSink])
}
