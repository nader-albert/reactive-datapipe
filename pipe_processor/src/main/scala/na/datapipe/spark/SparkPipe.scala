package na.datapipe.spark

import akka.actor.{ActorLogging, Actor}
import na.datapipe.process.model.ProcessPill
import org.apache.spark.streaming.receiver.ActorHelper

import scala.concurrent.Await

/**
 * @author nader albert
 * @since  17/09/2015.
 */
//case class ProcessTweet (tweet: Tweet)
//case class PublishCommand (rDD: RDD[String])

class SparkPipe extends Actor with ActorHelper {

  import scala.concurrent.duration._

  //val timeout = 20 seconds
  //val url = "akka.tcp://publishers@" + "127.0.0.1" + ":" + "3000" + "/user/publisher"
  //val publisher: ActorRef = Await.result(context.actorSelection(url).resolveOne(timeout), timeout)

  override def receive: Receive = {

    case ProcessPill(pill) => store(pill)

    /*case publish :PublishCommand => publisher ! PublishToFireBase (
      HTTPMessage(PUT,
        Uri("https://customer-mind.firebaseio.com/melbournecup/horses/users.json"),
        //Some("{ \"alanisawesome\": { \"name\": \"Alan Turing\", \"birthday\": \"June 23, 1912\" } }")
        Some (publish.rDD.first().toString)
      )
    )*/
  }
}
