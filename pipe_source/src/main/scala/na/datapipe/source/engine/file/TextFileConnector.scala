package na.datapipe.source.engine.file

import akka.actor.{Props, OneForOneStrategy}
import akka.actor.SupervisorStrategy.Restart
import na.datapipe.model.{SourcesChannels, TextPill, Commands}
import na.datapipe.source.engine.{TextPillLoader, StreamConnector}
import na.datapipe.source.model._

import scala.collection.immutable.HashSet
import scala.concurrent.Future
import scala.io.Source
import scala.util.{Random, Success, Failure, Try}

/**
 * @author nader albert
 * @since  3/05/16.
 */
class TextFileConnector extends StreamConnector {

  var lines = HashSet.empty[Int]

  override def supervisorStrategy = OneForOneStrategy() {
    case runtimeException: LoadRuntimeException =>
      log error "runtime exception received.. attempting to restart the associated line loader"
      Restart // TODO: doesn't probably make sense to restart the associated message loader, cos it was meant to process a single message anyway
  }

  /**
   * returns another DataSource with the connection established
   * */
  override def connect(source: DataSource) ={
    val fileSource = source match {
      case _ :FileSource => Some(FileSource(source.name, source.path, None))
      case _ => None
    }

    fileSource flatMap{ file =>
      Try {
        Source fromURL getClass.getClassLoader.getResource(file.path)
      } match {
        case Failure(exception) => log.error (exception.toString) ; None
        case Success(src) => Some(file copy(connection = Some(src)))
      }
    }
  }

  override def consume(source: DataSource) = {
    val fileSource = source match {
      case fileSrc: FileSource => Some(fileSrc)
      case _ => None
    }

    import scala.concurrent.ExecutionContext.Implicits.global

    val sink = context.actorOf(Props[TextPillLoader], "facebook-stream-consumer")

    if (fileSource.isDefined && fileSource.get.connection.isDefined) {

      val future = Future {
        var linesCount = 0

        log info "staying idle, to ensure cluster is well established first"
        Thread.sleep(90000) //TODO: change this to a proper handling to this issue.. waiting here for 30 seconds until the cluster is well estabilished
        val inputLines = fileSource.get.connection.get.getLines

        //val getLines = () => Source.fromFile("").getLines
        //Source(getLines)

        //log info "number of lines in the file: " + inputLines.size

        while (inputLines.hasNext && connected) {
          val lineId = Random.nextInt(10000)
          lines = lines + lineId

          linesCount += 1
          if (linesCount % 1000 == 0)
            Thread.sleep(300)
          //lineLoader.get ! Element(inputLines.next, lineId, ProjectChannels.TELCO, dataChannel)

          //sink ! Element(inputLines.next, lineId, ProjectChannels.TELCO, dataChannel)

          sink ! Commands.LOAD(
            TextPill(inputLines.next, Some(Map.empty[String, String].updated("source", SourcesChannels.FACEBOOK_API.name)), 1), 1) //TODO: change the constant sequence numbers
        }

        if (inputLines.hasNext || !connected) {
          //promise.tryFailure(new LoadInterruptedException("loading process has been interrupted in the middle of its execution! "))
          throw new LoadInterruptedException("interrupt")
        }
        else {
          //promise.trySuccess(source)
          linesCount
        }
      }

      future onComplete {
        case Success(numberOfLines) =>
          log info ("load completed successfully! " + numberOfLines)
          context.parent ! LoadComplete

        case Failure(exception: Throwable) => {
          exception match {
            case interrupt: LoadInterruptedException =>
              log warning "interrupt signal received ! "
              self ! LoadingInterrupted(interrupt)

            case runtimeException: RuntimeException =>
              log error("runtime exception ", runtimeException)
              self ! LoadingFailed(runtimeException)
          }
        }
      }
    }
  }
}

object TextFileConnector {
  def props = Props(classOf[TextFileConnector])
}