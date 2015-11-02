package na.datapipe.source.engine

import akka.actor.{Terminated, Actor, ActorRef}
import na.datapipe.model.Pill
import na.datapipe.source.model.{Load, LineLoaded, TransformerJoined}
import na.datapipe.transformer.model.{Command, PillTransformed}

/**
 * @author nader albert
 * @since  4/08/2015.
 */
//case class Element(line: String, id: Int)

trait PillLoader extends Actor {

  var transformers = IndexedSeq.empty[ActorRef]
  var jobCounter = 0

  override def preRestart(reason: Throwable, message: Option[Any]){
    println("actor is restarting because of a runtime failure while processing the following message: "
      + message.getOrElse(" EMPTY !"))
  }

  override def postRestart(reason: Throwable){
    println("actor commencing again after dropping the line that caused the error with " + reason.getMessage)
  }

  override def receive: Receive = {

     case load :Load if transformers isEmpty => println("no available transformers !")

     case Load(pill,id) =>

      /** The old way to try to locate the remote transformer.

      val transformer: ActorSelection = context.actorSelection("akka.tcp://transformers@" + host + ":"
        + port + "/user/transformer")

      transformer ! transformCommand(elem,id) //TransformLine (elem, id)
      */

      /**
       * Checking all Transformers currently in the cluster and picking one of them randomly (based on a sequential list)
       * */
       jobCounter += 1

       val text = pill.content

       println(s"loading.... $text")

       transformers(jobCounter % transformers.size) ! transformCommand(pill, id)

      // This is a trial code, to send the post to the processor directly without passing through the transformer...
      // should be deleted

      /*
      import scala.concurrent.duration._

      val timeout = 20 seconds

      val processorUrl = "akka.tcp://sparkDriver@127.0.0.1:7777/user/Supervisor0/processor"
      val processor: ActorRef = Await.result(context.actorSelection(processorUrl).resolveOne(timeout), timeout)
      if(Random.nextBoolean) throw new LoadRuntimeException(new RuntimeException(" just a random dummy exception! "))

      processor ! ProcessCommand(elem) */

    case PillTransformed(id) => context.parent ! LineLoaded(id)

    case TransformerJoined if !transformers.contains(sender) =>
      println ("a new transformer added to the list !")
      context watch sender
      transformers = transformers :+ sender

    case Terminated(a) =>
      println("one transformer has left cluster ! " + "[ " + a + " ]")
      transformers = transformers.filterNot(_ == a)
  }

  //val host :String

  //val port :String

  val transformCommand: (Pill[String],Int) => Command
}
