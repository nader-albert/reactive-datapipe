package na.datapipe.sink

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import akka.camel.CamelMessage
import akka.cluster.{Member, MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}

import com.typesafe.config.Config
import na.datapipe.model.{Commands, Command, Pill}
import na.datapipe.sink.model.SinkRegistration
import na.datapipe.sink.producers.camel.jms.TweetSink
import na.datapipe.sink.producers.db.mongo.MongoSink
import na.datapipe.sink.producers.ws.HttpSink
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

  //val pillDao = context.actorOf(Props[PillMongoDao], name = "pill-dao")

  val mongoSink = context.actorOf(MongoSink.props, name = "mongo-sink")

  override val supervisorStrategy = OneForOneStrategy() {
    case ex: NullPointerException =>
      log error "Null pointer exception during transformation....! Actor will be resumed !" + ex.getMessage + ex.printStackTrace
      Resume
  }

  override def preStart(): Unit = {
     cluster subscribe(self, classOf[MemberUp])
  }

  override def receive: Receive = {

    case command: Command if Commands ? command == Commands.SwallowCommand =>
      command.pill.header.fold()(map => map.get("persistence_channel").fold()
        (value =>
          if (value.startsWith("camel")) {
            log info "publishing raw post to transform jms queue"
            //camelSink ! CamelMessage(command.pill.toJson.toString, Map())
          }
          else if (value.equals("http://firebase"))
            httpSink forward command

          else if (value.equals("db://mongo"))
            mongoSink forward command
          ))

      import spray.json._

      import SinkGuardian._ //TODO: Check why I am required to import this, it should be part of the scope that implicits look into

      //TODO: discern the right publisher to send this message to

    //case swallow :Swallow if swallow.channel.name == "http://firebase" => httpSink forward swallow

    //case swallow :Swallow if swallow.channel.name == "db://mongo" => mongoSink forward swallow

    /**
     * Current snapshot state of the cluster. Sent to new subscriber
     * */
    case state: CurrentClusterState => println("current cluster state is: " + "active members are: " + state.members + " unreachable members are: "
      + state.unreachable + " leader is: " + state.leader + " this was seen by: " + state.seenBy)

      state.members.filter(_.status == MemberStatus.Up) foreach register

    case ActorIdentity(resolvedActor, refOption) if resolvedActor == "processor" && refOption.isDefined =>
      log info "******************* processor guardian resolved successfully ! ******************* " + refOption.get
      refOption.get ! SinkRegistration

    case ActorIdentity(resolvedActor, refOption) if resolvedActor == "transformer" && refOption.isDefined =>
      log info "******************* transformer guardian resolved successfully ! ******************* " + refOption.get
      refOption.get ! SinkRegistration

    /** only when the status of an existing member is changed to up */
    case MemberUp(m) => println("member: " + m + "is now up")
      register(m)

    case _ => println("incorrect swallow received ")
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

    if (member.hasRole("pipe_transformer")) {
      log info "******************* new transformer detected **************************** "
      context.actorSelection(RootActorPath(member.address) / "user" / "transformer-guardian") ! Identify("transformer")
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


