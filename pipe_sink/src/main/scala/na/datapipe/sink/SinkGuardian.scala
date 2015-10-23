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
    /*case PublishRawPost(post :Post) => {
      println("publishing raw post to transform jms queue")

      import spray.json._

      import SinkGuardian._ //TODO: Check why I am required to import this, it should be part of the scope that implicits look into

      //TODO: discern the right publisher to send this message to
      rawPublisher ! CamelMessage(post.toJson.toString, Map())
    }

    case publishToFirebase :PublishToFireBase => fireBaseProducer forward publishToFirebase*/

    case swallow :Swallow if swallow.channel.name == "camel" =>
      println("publishing raw post to transform jms queue")

      import spray.json._

      import SinkGuardian._ //TODO: Check why I am required to import this, it should be part of the scope that implicits look into

      //TODO: discern the right publisher to send this message to
      camelSink ! CamelMessage(swallow.pill.toJson.toString, Map())

    case swallow :Swallow if swallow.channel.name == "http" => httpSink forward swallow
  }
}

object SinkGuardian {
  def props(sinkConfig :Config) = Props(classOf[SinkGuardian], sinkConfig)

  import scala.language.implicitConversions

  implicit val postWriter: JsonWriter[Pill] = JsonWriter.func2Writer(toJSON)

  //implicit val engagementWriter: JsonWriter[EngagementEvent] = JsonWriter.func2Writer(toJSON)

  /**
   * much better to write your own JSON parser so as to avoid reflection that would otherwise be utilized to, in case we
   * resort to an out of the box JSON Parser
   * */
  def toJSON(post: Pill) :JsValue = //TODO provide a suitable transformation here for Pill
    JsObject(Map.empty[String, JsValue] updated ("conversation_id", JsString("post.getConversation_id"))) //post.getConversation_id)))
    // TODO: handle null values, by rewriting the Post model and write in scala and returning options instead of null

  //TODO: finsih the converter implementation down here

  /*updated ("post_channel", JsString("conversation_id"))
  updated ("post_url", JsString("conversation_id"))
  updated ("post_type", JsString("conversation_id"))
  updated ("datasource_id", JsString("conversation_id"))
  updated ("conversation_title", JsString("conversation_id"))
  updated ("conversation_start_date", JsString("conversation_id"))

  updated ("post_content", JsString("conversation_id"))
  updated ("post_date", JsString("conversation_id"))
  updated ("post_id", JsString("conversation_id"))
  updated ("post_reply_src_id", JsString("conversation_id"))


  updated ("post_reply", JsString("conversation_id"))
  updated ("post_sentiment", JsString("conversation_id")
  updated ("post_source_id", JsString("conversation_id"))
  updated ("brandPage", JsString("conversation_id"))


  updated ("themes", JsObject("themes", JsString()))
  updated ("engagements", JsObject("engagements", JsString()))
  updated ("organisations", JsObject("engagements", JsString()))
  updated ("taxonomies", JsObject("taxonomies", JsString()))*/
  //)

  /*def toJSON(engagementPost: EngagementEvent) :JsValue = JsObject(Map.empty[String, JsValue]
    updated ("conversation_id", JsString("post.getConversation_id")))*/
  /*
  *  String conversation_id;
     String post_channel;
    String post_type;
    String datasource_id;
    String post_id;
    String post_source_id;
    Date engagement_date;
    String engagement_id;
    String engagement_name;
    int engagement_poster_followers;
    String engagement_poster_id;
    String engagement_type;
*/
}




