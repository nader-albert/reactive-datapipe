package na.datapipe.processor

import akka.actor._
import akka.cluster.{Member, MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.event.LoggingReceive
import na.datapipe.model.Tweet
import na.datapipe.processor.model.{ProcessPill, ProcessorRegistration}

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author nader albert
 * @since  15/10/2015.
 */
class ProcessingGuardian extends Actor with ActorLogging {

  var sparkPipes: Set[ActorRef] = Set.empty

  val cluster: Cluster = Cluster(context.system)
  val sparkPath = "akka.tcp://sparkDriver@127.0.0.1:7777/user/Supervisor0/spark-pipe"

  import scala.concurrent.duration._
  val timeout = 20 seconds

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = {
    cluster subscribe(self, classOf[MemberUp])

    context.system.actorSelection(sparkPath) ! Identify("spark")

    /*cluster joinSeedNodes List(Address("akka.tcp", "ClusterSystem", "127.0.0.1" , 2551),
      Address("akka.tcp", "ClusterSystem", "127.0.0.1" , 2552))*/
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = LoggingReceive {
    //locate the sparkDriver processor actor here and forward this message to it so that it can store the tweet
    case ActorIdentity(resolvedActor, refOption) if resolvedActor == "spark" && refOption.isDefined =>
      log info "spark pipe resolved successfully ! " + refOption.get
      context watch refOption.get
      sparkPipes = sparkPipes + refOption.get //since we could have different futures trying to identify the spark pipe at the same time
      // we may end up having many of them identified here but the enclosing data structure is a set,
      // an identified sparkPipe will not be duplicated.

    case ActorIdentity(resolvedActor, refOption) if resolvedActor == "transformer" && refOption.isDefined =>
      log info "transformer guardian resolved successfully ! " + refOption.get
      refOption.get ! ProcessorRegistration

    // Assuming that spark processing engine sits in its own cluster island, and that we don't have control over it !
    // if we can't locate the spark driver system, for any reason here, we can then degrade the level of processing we
    // provide to a lower level. temporarily until the spark driver cluster comes in again !
    case processTweet :ProcessPill[_] if sparkPipes isEmpty => log info "empty spark-pipe, will try to resolve another one "
      context.system.actorSelection(sparkPath) ! Identify("spark") /*resolveOne(10 seconds) onComplete {
        case Success(spark) =>
          sparkPipes = sparkPipes.::(spark) //don't try to look it up next time !
          spark forward processTweet
          println("sparkPipe size now has become " + sparkPipes.size)
        case Failure(failure) =>
          //TODO: Trivial type of processing that doesn't require spark. can be considered as the minimum level of service
          println("no available spark pipes at the moment ! downgrading the service and doing trivial computations ")
      }*/

    case processTweet :ProcessPill[_] if sparkPipes nonEmpty => log info "non empty sparkPipe" ; sparkPipes.head forward processTweet //assuming only one spark pipe at the moment.

    /** Current snapshot state of the cluster. Sent to new subscriber */
    case state: CurrentClusterState => println("current cluster state is: " + "active members are: " + state.members + " unreachable members are: "
      + state.unreachable + " leader is: " + state.leader + " this was seen by: " + state.seenBy)

      state.members.filter(_.status == MemberStatus.Up) foreach register

    /** only when the status of an existing member is changed to up */
    case MemberUp(m) => println("member: " + m + "is now up")
      register(m)

    case Terminated(a) => log info "spark pipe disconnected !"
      sparkPipes = sparkPipes.filterNot(_ == a)
      log debug "spark pipe is now empty with size = " + sparkPipes.size
  }

  /**
   * if a new transformer node comes into play, we have to notify it that we do exist in the cluster, which will then
   * allow us to receive messages from transformers
   * */
  def register(member: Member): Unit = {
    if (member.hasRole("pipe_transformer"))
      context.actorSelection(RootActorPath(member.address) / "user" / "transformer-guardian") ! Identify("transformer")

    println(member.roles)
  }
}

object ProcessingGuardian {
  def props() = Props(classOf[ProcessingGuardian])
}
