package na.datapipe.sink.producers.db.mongo

import na.datapipe.model.{Pill, TextPill}
import na.datapipe.sink.producers.db.PillDao
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{Macros, BSONDocumentWriter, BSONDocument}

import scala.concurrent.Future

/**
 * @author nader albert
 * @since  5/11/2015.
 */
trait PillMongoDao extends PillDao [WriteResult] {
  this: MongoConnector =>

  import scala.concurrent.ExecutionContext.Implicits.global

  import PillMongoDao._

  override def save(pill: Pill) = collectionMap.get(MongoCollections.CANONICAL_POST)
    .fold(Future.failed[WriteResult]
    (new IllegalArgumentException("canonical post collection not initialized")))(collection =>
      pill match {
        case text: TextPill => collection.insert[TextPill](text)
        case _ => Future.failed[WriteResult] (new IllegalArgumentException("canonical post collection not initialized"))
      })

  def save(collection: BSONCollection, document: BSONDocument) = collection.insert(document)

  /** purges the given collection, from all the documents in it
    * @param collectionName: name of the collection, required to be purged. the name has to match one of the collection
    * names in the enclosed [collectionMap]
    * */
  def clear(collectionName: String) = collectionMap.get(collectionName).get.remove(BSONDocument.empty)

  def find(query: Map[String, AnyRef]) = {
    //TODO: collect the query items from the map and put them into a MongoDocument.
    val mongoQuery = BSONDocument("age" -> BSONDocument("$gt" -> 27))

    collectionMap.get(MongoCollections.CANONICAL_POST).get.find(mongoQuery).cursor[BSONDocument]().collect[List] ()
  }

  def findPost(query: BSONDocument): Future[List[BSONDocument]] = {
    //val query = BSONDocument("age" -> BSONDocument("$gt" -> 27))

    collectionMap.get(MongoCollections.CANONICAL_POST)
      .fold(Future.failed[List[BSONDocument]](new IllegalArgumentException("canonical post collection not initialized")))(collection => collection.find(query).cursor[BSONDocument]().collect[List] ())
  }

  /*override protected def write_one(pill: Pill): Future[WriteResult] = {
    println("********** a request to add one********* ")
    val pillDocument = BSONDocument(
      "body" -> pill.body.toString,
      "header" -> pill.header.toString, // TODO: header should be inserted as another document...
      "id" -> Random.nextInt)

    pillCollection.insert(pillDocument)
  }*/

 /* override protected def write_all(pills: List[Pill]): Future[List[Long]]= {
    Future {
      Nil
    }
  }

  /*override protected def filter() = {
    val query = BSONDocument("firstName" -> "Jack")
  }*/


  override protected def find_all: List[TextPill] = {
    // Select only the documents which field 'firstName' equals 'Jack'
    val query = BSONDocument("firstName" -> "Jack")

    // select only the fields 'lastName' and '_id'
    val filter = BSONDocument("lastName" -> 1, "_id" -> 1)

    /* Let's run this query then enumerate the response and print a readable
     * representation of each document in the response */
    /* collection.
      find(query, filter).
      cursor[BSONDocument].
      enumerate().apply(Iteratee.foreach { doc =>
      println(s"found document: ${BSONDocument pretty doc}")
    })*/

    var textPills: List[TextPill] = Nil

    // Or, the same with getting a list
    val futureList: Future[List[BSONDocument]] =
      pillCollection.
        find(query, filter).
        cursor[BSONDocument].
        collect[List]()

    futureList.map { list =>
      /*list.foreach { doc =>
        println(s"found document: ${BSONDocument pretty doc}")
      }*/
      // map it to a pill
      list.map((pillDoc: BSONDocument) => TextPill("content", None, 1)) //TODO: return a proper TextPill
    } onSuccess {
        case pills: List[TextPill] => textPills = pills
    }

    textPills
  }*/
}

object PillMongoDao {

  implicit val headerWriter :BSONDocumentWriter[Map[String,String]] = new BSONDocumentWriter[Map[String,String]] {
    override def write(header: Map[String,String]) = {

      val documents = for {
        entry <- header
      } yield BSONDocument((entry._1, entry._2))

      documents.fold(BSONDocument.empty)((zero, current) => zero ++ current)

      documents.reduce((zero, current) => zero ++ current)
    }
  }

  implicit val pillBSONWriter :BSONDocumentWriter[TextPill] = Macros.writer[TextPill]
}
