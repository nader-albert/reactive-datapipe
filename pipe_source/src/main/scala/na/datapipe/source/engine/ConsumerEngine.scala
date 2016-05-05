package na.datapipe.source.engine

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import na.datapipe.model.{SourcesChannels, StopLoad, StartLoad}

/**
 * @author nader albert
 * @since  15/10/2015.
 */

object ConsumerEngine extends App {

  println("Hey am a consumer micro-service coming into play ! Please welcome me !")

  val system = ActorSystem("ClusterSystem")

  val config = ConfigFactory load

  val applicationConfig: Config = config getConfig "source_app"

  val loader = system.actorOf(LoadingGuardian.props(applicationConfig), name = "source-guardian")

  loader ! StartLoad(SourcesChannels.TWITTER_API)
  Thread.sleep(10000)
  loader ! StopLoad(SourcesChannels.TWITTER_API)




  //Thread.sleep(50000)

  //loader ! StartLoad(SourcesChannels.FACEBOOK_FILE)
  //Thread.sleep(5000)
  //loader ! StopLoad(SourcesChannels.FACEBOOK_FILE)

  /*
    loader ! StartTwitterLoad("twitter-stream")
    Thread.sleep(550000)
    loader ! StopTwitterLoad("twitter-stream")
  */
}
