package na.datapipe.sink.producers.db.mongo

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import na.datapipe.sink.model.Swallow
import na.datapipe.sink.producers.db.Persistence.AddOne

/**
 * @author nader albert
 * @since  5/11/2015.
 */
class MongoSink extends Actor with ActorLogging with MongoConnector with PillMongoDao {

  override def preStart() = connect("customer_mind")

  override def receive: Receive = {
    case swallow: Swallow =>
      log info ("swallow msg received !" + swallow)
      save(swallow.pill)
  }
}

object MongoSink {
  def props = Props(classOf[MongoSink])
}
