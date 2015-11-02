package na.datapipe.persistance

import akka.actor.{Actor, Props}
import na.datapipe.model.Pill

import scala.util.Random

/**
 * @author nader albert
 * @since  2/11/2015.
 */
object Persistence {
  case class Persist[T](dataPill: Pill[T], valueOption: Option[String], id: Long)
  case class Persisted[T](dataPill: Pill[T], id: Long)

  class PersistenceException extends Exception("Persistence failure")

  def props(flaky: Boolean): Props = Props(classOf[Persistence], flaky)
}

class Persistence(flaky: Boolean) extends Actor {

  import Persistence._

  def receive = {
    case Persist(key, _, id) => {
      if (!flaky || Random.nextBoolean())
        sender ! Persisted(key, id)
      else {
        println("persistence failed !")
        throw new PersistenceException
      }
    }
  }
}