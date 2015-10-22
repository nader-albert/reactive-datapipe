package na.datapipe.source.engine

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

/**
 * @author nader albert
 * @since  15/10/2015.
 */

object ConsumerEngine extends App {

  println("Hey am a consumer micro-service coming into play ! Please welcome me !")

  val system = ActorSystem("ClusterSystem")

  val config = ConfigFactory load

  val applicationConfig: Config = config getConfig "loader_app"

  val transformersConfig = applicationConfig getConfig "transformers"

  val loader = system.actorOf(LoadingGuardian.props(applicationConfig), name = "loader-guardian")

  /*var x =0
  do {
    println("sending another load command !")

    loader ! StartFileLoad("facebook-demo") //"target_demo_fb_raw_all_28May-sorted.out")

    Thread.sleep(5000)

    loader ! StopFileLoad("target_demo_fb_raw_all_28May-sorted.out")
    x +=1
  } while(x < 100) */

  loader ! StartTwitterLoad("twitter-stream")

  Thread.sleep(550000)

  loader ! StopTwitterLoad("twitter-stream")

}
