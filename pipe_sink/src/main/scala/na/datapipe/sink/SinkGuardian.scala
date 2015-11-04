package na.datapipe.sink

import akka.actor._
import akka.camel.CamelMessage
import akka.cluster.{Member, MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}

import com.typesafe.config.Config
import na.datapipe.model.Pill
import na.datapipe.sink.model.{SinkRegistration, Swallow}
import na.datapipe.sink.producers.camel.jms.TweetSink
import na.datapipe.sink.producers.spray.HttpSink
import spray.json.{JsonWriter, JsObject, JsValue, JsString}

/**
 * @author nader albert
 * @since  6/08/2015
 */

class SinkGuardian(sinkConfig :Config) extends Actor with ActorLogging{

  val cluster: Cluster = Cluster(context.system)

  val rabbitMQConfig = sinkConfig getConfig "destinations" getConfig "jms" getConfig "rabbit"

  val camelSink = context.actorOf(TweetSink.props(rabbitMQConfig), name = "tweet-sink")

  val httpSink = context.actorOf(HttpSink.props, name = "http-sink")

  override def preStart(): Unit = {
     cluster subscribe(self, classOf[MemberUp])
  }

  override def receive: Receive = {
    case swallow :Swallow[String] if swallow.channel.name.startsWith("camel") =>
      println("publishing raw post to transform jms queue")

      import spray.json._

      import SinkGuardian._ //TODO: Check why I am required to import this, it should be part of the scope that implicits look into

      //TODO: discern the right publisher to send this message to
      camelSink ! CamelMessage(swallow.pill.toJson.toString, Map())

    case swallow :Swallow[String] if swallow.channel.name.startsWith("http") => httpSink forward swallow
    //TODO: get it from the Pill header instead

    /** Current snapshot state of the cluster. Sent to new subscriber */
    case state: CurrentClusterState => println("current cluster state is: " + "active members are: " + state.members + " unreachable members are: "
      + state.unreachable + " leader is: " + state.leader + " this was seen by: " + state.seenBy)

      state.members.filter(_.status == MemberStatus.Up) foreach register

    case ActorIdentity(resolvedActor, refOption) if resolvedActor == "processor" && refOption.isDefined =>
      log info "******************* processor guardian resolved successfully ! ******************* " + refOption.get
      refOption.get ! SinkRegistration

    /** only when the status of an existing member is changed to up */
    case MemberUp(m) => println("member: " + m + "is now up")
      register(m)
  }

  /**
   * if a new processor node comes into play, we have to notify it that we do exist in the cluster, which will then
   * allow us to receive messages from processors
   * */
  def register(member: Member): Unit = {
    if (member.hasRole("pipe_processor")) {
      log info "******************* pipe processor detected **************************** "
      context.actorSelection(RootActorPath(member.address) / "user" / "processing-guardian") ! Identify("processor")
    }

    println(member.roles)
  }
}

object SinkGuardian {
  def props(sinkConfig :Config) = Props(classOf[SinkGuardian], sinkConfig)

  import scala.language.implicitConversions

  implicit val pillWriter: JsonWriter[Pill] = JsonWriter.func2Writer(toJSON)

  /**
   * much better to write your own JSON parser so as to avoid reflection that would otherwise be utilized to, in case we
   * resort to an out of the box JSON Parser
   * */
  def toJSON(post: Pill) :JsValue = //TODO provide a suitable transformation here for Pill
    JsObject(Map.empty[String, JsValue] updated ("conversation_id", JsString("post.getConversation_id"))) //post.getConversation_id)))
}


