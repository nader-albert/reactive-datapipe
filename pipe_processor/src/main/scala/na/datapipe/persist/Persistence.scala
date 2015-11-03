package na.datapipe.persist

import akka.actor.{Actor, Props}
import na.datapipe.model.{TweetPill, Pill}
//import reactivemongo.api.MongoDriver
//import reactivemongo.api.collections.bson.BSONCollection
//import reactivemongo.bson.BSONDocument

import scala.concurrent.Future

/**
 * @author nader albert
 * @since  2/11/2015.
 */
object Persistence {
  case class Persist(dataPill: TweetPill, valueOption: Option[String], id: Long)
  case class Persisted(dataPill: TweetPill, id: Long)

  case class Read(pillId :Long)
  //case class Read(criteria: Filter)
  case class ReadAll()

  class PersistenceException extends Exception("Persistence failure")

  //def props(flaky: Boolean): Props = Props(classOf[Persistence], flaky)
}

/*abstract class Persistence(flaky: Boolean) extends Actor {

  import na.datapipe.persist.Persistence._

  var collection:BSONCollection

  override def preStart(): Unit ={
     connect
  }

  def receive = {
    case Persist(key, _, id) => {
      if (!flaky || Random.nextBoolean())
        sender ! Persisted(key, id)
      else {
        println("persistence failed !")
        throw new PersistenceException
      }
    }

    //case Read => listAllPills()
  }

  private def connect() {
    // gets an instance of the driver
    // (creates an actor system)
    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))

    // Gets a reference to the database "plugin"
    val db = connection("plugin")

    // Gets a reference to the collection "acoll"
    // By default, you get a BSONCollection.
    collection = db("acoll")
  }

  private def listAllPills = {
    // Select only the documents which field 'firstName' equals 'Jack'
    val query = BSONDocument("firstName" -> "Jack")

    // select only the fields 'lastName' and '_id'
    val filter = BSONDocument(
      "lastName" -> 1,
      "_id" -> 1)

    /* Let's run this query then enumerate the response and print a readable
     * representation of each document in the response */
   /* collection.
      find(query, filter).
      cursor[BSONDocument].
      enumerate().apply(Iteratee.foreach { doc =>
      println(s"found document: ${BSONDocument pretty doc}")
    })*/

    // Or, the same with getting a list
    val futureList: Future[List[BSONDocument]] =
      collection.
        find(query, filter).
        cursor[BSONDocument].
        collect[List]()

    futureList.map { list =>
      list.foreach { doc =>
        println(s"found document: ${BSONDocument pretty doc}")
      }
    }
  }*/
