package na.datapipe.sink

import akka.actor.{Props, Actor}
import akka.camel.CamelMessage

import com.typesafe.config.Config
import na.datapipe.sink.model.{Pill, Swallow}
import na.datapipe.sink.producers.camel.TweetSink
import na.datapipe.sink.producers.spray.HttpSink
import spray.json.{JsonWriter, JsObject, JsValue, JsString}

/**
 * @author nader albert
 * @since  6/08/2015
 */

class SinkGuardian(sinkConfig :Config) extends Actor {

  val rabbitMQConfig = sinkConfig getConfig "destinations" getConfig "jms" getConfig "rabbit"

  /*val camelSink = context.actorOf(CamelSink.props(rabbitMQConfig),
    name = "processed-post-publisher") */

  val camelSink = context.actorOf(TweetSink.props(rabbitMQConfig), name = "tweet-sink")

  val httpSink = context.actorOf(HttpSink.props, name = "http-sink")

  override def receive: Receive = {
    case swallow :Swallow if swallow.channel.name == "camel" =>
      println("publishing raw post to transform jms queue")

      import spray.json._

      import SinkGuardian._ //TODO: Check why I am required to import this, it should be part of the scope that implicits look into

      //TODO: discern the right publisher to send this message to
      camelSink ! CamelMessage(swallow.pill.toJson.toString, Map())

    case swallow :Swallow if swallow.channel.name == "http" => httpSink forward swallow //TODO: get it from the Pill header instead
  }
}

object SinkGuardian {
  def props(sinkConfig :Config) = Props(classOf[SinkGuardian], sinkConfig)

  import scala.language.implicitConversions

  implicit val pillWriter: JsonWriter[Pill[String]] = JsonWriter.func2Writer(toJSON)

  /**
   * much better to write your own JSON parser so as to avoid reflection that would otherwise be utilized to, in case we
   * resort to an out of the box JSON Parser
   * */
  def toJSON(post: Pill[String]) :JsValue = //TODO provide a suitable transformation here for Pill
    JsObject(Map.empty[String, JsValue] updated ("conversation_id", JsString("post.getConversation_id"))) //post.getConversation_id)))
}




