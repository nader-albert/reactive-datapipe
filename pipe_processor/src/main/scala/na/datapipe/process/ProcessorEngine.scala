package na.datapipe.process

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory


/**
 * @author nader albert
 * @since  17/09/2015.
 */
object ProcessorEngine extends App{

  val system = ActorSystem("ClusterSystem")

  val config = ConfigFactory load

  val loader = system.actorOf(ProcessingGuardian.props(), name = "processing-guardian")
}
