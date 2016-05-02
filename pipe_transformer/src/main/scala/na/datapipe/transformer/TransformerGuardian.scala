package na.datapipe.transformer

import akka.actor.SupervisorStrategy.{Restart, Resume, Stop}
import akka.actor._
import akka.cluster.{Member, MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import na.datapipe.model.{Sources, Commands, Command}
import na.datapipe.process.model.{ProcessorJoined, ProcessorRegistration}
import na.datapipe.sink.model.{SinkJoined, SinkRegistration}
import na.datapipe.transformer.model.{TransformerRegistration, KillChildren}
import na.datapipe.transformer.twitter.TwitterTransformer

import scala.concurrent.Await
import scala.language.postfixOps
import scala.util.Random

/**
 * @author nader albert
 * @since  21/07/2015.
 */

class TransformerGuardian(analyzersSystemHost: String, analyzersSystemPort:String) extends Actor with ActorLogging {
  var twitterTransformer: ActorRef = null

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = {
    twitterTransformer = context.actorOf(TwitterTransformer.props,
      "twitter-transformer" + Random.nextInt) //TODO: send an Identity Message and wait for the response
    cluster.subscribe(self, classOf[MemberUp])

    /*cluster.joinSeedNodes(List(Address("akka.tcp", "ClusterSystem", "127.0.0.1" , 2551),
      Address("akka.tcp", "ClusterSystem", "127.0.0.1" , 2552)))*/
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  override val supervisorStrategy = OneForOneStrategy(){
    case ex :NullPointerException =>
      log error "Null pointer exception during transformation....! Actor will be resumed !" + ex.getMessage + ex.printStackTrace
      Resume

    case missingRequirement :IllegalArgumentException =>
      log error("Illegal argument exception. Happens because of a corrupted input model ! " +
        "Transformer Actor will be restarted ", missingRequirement)
      Restart //TODO: Check if that's the correct behavior. The associated post will be dropped and will never be processed

    case unsupportedOperation :UnsupportedOperationException =>
      log error "unsupported operation exception" + unsupportedOperation.printStackTrace
      log error "transformer actor will be restarted "
      Restart

    case randomException: Exception => log error ("exception during transformation....! Actor will be stopped !" + randomException)
      Stop
  }

  override def receive: Receive = {
   /* case TransformLine(text, id) => {
      val facebookTransformer = context.actorOf(FacebookTransformer.props(facebookProcessor, analyzersSystemHost, analyzersSystemPort,
        /*sentimentMap, organisationMap, taxMap,*/ sender), "facebook-transformer" + Random.nextInt)
      facebookTransformer forward TransformLine(text, id) //my child will reply to the current sender directly without requiring the DataTransformer to intervene again !
    }*/

    //case Transformed(id) => senders.get(id).get ! Transformed()
    case KillChildren => context.children.foreach(_ ! PoisonPill)

    case command :Command if Commands ? command == Commands.TransformCommand =>
      if (command.pill.header.fold(false)(_.find(_._1 == "source").fold(false)(_._2 == Sources.TWITTER_API.name))) {
        println ("twitter pill received... passing to the relevant transformer.... ")
        twitterTransformer forward command
      }

    /*case Transform(pill, id) if pill.header.fold(false)(_.find(_._1 == "source").fold(false)(_._2 == "twitter")) =>
      println ("twitter pill received... passing to the relevant transformer.... ")
      twitterTransformer forward Transform(pill, id) */

    /** Current snapshot state of the cluster. Sent to new subscriber */
    case state: CurrentClusterState =>
      println("current cluster state is: " + "active members are: " + state.members + " unreachable members are: "
        + state.unreachable + " leader is: " + state.leader + " this was seen by: " + state.seenBy)

      state.members.filter(_.status == MemberStatus.Up) foreach register

    /** only when the status of an existing member is changed to up */
    case MemberUp(m) =>
      println("member: " + m + "is now up")
      register(m)

    case ProcessorRegistration => println("Transformation Guardian --> processor Registration " + "message disseminated to children: " + context.children)
      // spread the news, disseminate the update of having a new processor coming into play.. basically all existing
      // transformers in the cluster will send this message to register themselves with a newly inserted processor.
      context.children.foreach(_ forward ProcessorJoined) //keep the sender as is, don't change it... as it will be watched by the fine grained ElementLoader !

    case SinkRegistration =>
      log info "Transformer Registration Event. message disseminated to children: " + context.children
      context.children.foreach(_ forward SinkJoined)
  }

  /**
   * if a new transformer node comes into play, we have to notify it that we do exist in the cluster, which will then
   * allow us to receive messages from transformers
   **/
  def register(member: Member): Unit = {

  if (member.hasRole("pipe_source")) {
    import scala.concurrent.duration._

    val loader = Await.result(context.actorSelection(RootActorPath(member.address) / "user" / "source-guardian") //TODO: should be done using an IdentityResolver, in order to be non blocking
      .resolveOne(10 seconds), 10 seconds)

    loader ! TransformerRegistration
  }
    println(member.roles)
  }
}

object TransformerGuardian {
  def props = Props(classOf[TransformerGuardian])
}