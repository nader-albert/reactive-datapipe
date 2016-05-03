package na.datapipe.sink.producers.db

import akka.actor.Actor
import na.datapipe.model.Pill
import na.datapipe.sink.producers.db.Persistence.{AddOne, Added, AddAll, ReadAll}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * @author nader albert
 * @since  2/11/2015.
 */
object Persistence {
  case class AddOne(dataPill: Pill, id: Long)
  case class Added(id: Long)

  case class AddAll(pills: List[Pill])
  case class ReadOne(pillId :Long)
  //case class Read(criteria :Filter)
  case object ReadAll

  case class Persisted(dataPill: Pill, id: Long)

  class PersistenceException extends Exception("Persistence failure")

  //def props(flaky: Boolean): Props = Props(classOf[Persistence], flaky)
}

/*abstract class Persistence extends Actor {

  type resourceCollection

  import scala.concurrent.ExecutionContext.Implicits.global

  override def preStart(): Unit = {
    println("connecting to mongo db")
    connect
  }

  def receive = {
    case AddOne(pill,id) =>
      val future1 = write_one(pill) //pill.header.flatMap() ; sender ! Persisted(key, id)

      future1.onComplete {
        case Failure(e) => throw e
        case Success(writeResult) =>
          println(s"successfully inserted document with result: $writeResult")
          //writeResult.code.getOrElse(1)
          sender ! Added(/*writeResult.code.getOrElse(1)*/33)
      }

    //case AddAll(pills) => write_all(pills)
    //case ReadOne(key: Long) => find_one(key)
    //case ReadAll => find_all
  }

  protected def connect

  protected def write_one(pill: Pill): Future[WriteResult]

  //protected def write_all(pills: List[Pill]): Future[List[Long]]

  //protected def find_one(key: Long): Future[Pill]

  //protected def find_all: Future[List[Pill]]
}*/
