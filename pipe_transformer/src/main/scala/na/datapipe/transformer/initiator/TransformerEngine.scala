package na.datapipe.transformer.initiator

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import na.datapipe.transformer.TransformerGuardian

/**
 * @author nader albert
 * @since  21/07/2015.
 */
object TransformerEngine extends App {
  implicit val system = ActorSystem("ClusterSystem")

  val config = ConfigFactory load

  val applicationConfig = config getConfig "transformer_app"
  val analyzersConfig = applicationConfig getConfig "analyzers"
  val analyzersSystemHost = analyzersConfig getString "analyzers_host"
  val analyzersSystemPort = analyzersConfig getString "analyzers_port"

  val transformer = system actorOf(TransformerGuardian.props, name = "transformer-guardian")
}