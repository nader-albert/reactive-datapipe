package na.datapipe.sink.producers.spray

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext

import akka.io.IO
import akka.util.Timeout
import akka.pattern.ask
import akka.cluster.{Member, MemberStatus}
import akka.actor.{RootActorPath, Props, Actor}
import akka.cluster.ClusterEvent.{MemberUp, CurrentClusterState}

import spray.can.Http
import spray.http.{HttpRequest, HttpResponse}

import na.datapipe.sink.producers.spray.model.HttpPill
import na.datapipe.sink.model.{SinkRegistration, Swallow}

/**
 * @author nader albert
 * @since  28/09/2015.
 */
class HttpSink extends Actor {

  import context.system

  implicit val timeout = Timeout(15 seconds)
  import ExecutionContext.Implicits.global

  override def receive: Receive = {
    case Swallow (_ , httpPill :HttpPill) =>

    // this is a bit tricky... if sender is called directly from inside the onComplete block,
    // it will be referring to the dead letters actor.. take care :) !
    val caller = sender

      if (httpPill.method.isEmpty || httpPill.url.isEmpty)
        println("request rejected !" + "can't swallow an http pill that hasn't got a method or a url ")
      else {
        (IO(Http) ? HttpRequest(httpPill.method.get,httpPill.url.get, Nil, httpPill.body))
          .mapTo[HttpResponse]
          .onComplete {
            case Success(response: HttpResponse) => {
              println {
                " [status] => " + response.status +
                " [headers] => " + response.headers.foreach(println) +
                " [body] => " + response.entity.data.asString }
              caller ! response
            }
            case Failure(exception) => println(exception getMessage)
          }
      }

    case state: CurrentClusterState =>
      println("state: " + state)
      state.members.filter(_.status == MemberStatus.Up) foreach register

    case MemberUp(m) =>
      println ("member up message")
      register(m)
  }

  /**
   * if a new transformer node comes into play, we have to notify it that we do exist in the cluster, which will then
   * allow us to receive messages from transformers
   * */
  def register(member: Member): Unit =
    if (None == member.roles.find(_.equals("sink")))
    //assuming that it has just one role and that the actor path has exactly the same name. quite brittle, isn't it ?
      context.actorSelection(RootActorPath(member.address) / "user" / member.roles.head) ! SinkRegistration
}

object HttpSink {
  def props = Props(classOf[HttpSink])
}

