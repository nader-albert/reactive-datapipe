package na.datapipe.sink.producers.db.mongo

import reactivemongo.api.{MongoConnectionOptions, MongoDriver}
import reactivemongo.api.collections.bson.BSONCollection

/**
 * @author nader albert
 * @since  27/04/16.
 */
trait MongoConnector {

  import scala.concurrent.ExecutionContext.Implicits.global

  var collectionMap = Map.empty[String, BSONCollection]

  def connect(databaseName: String, collections :List[String] = Nil, host: String = "localhost", port: String = "27017") = {
    val driver = new MongoDriver //gets an instance of the driver - under the cover it manages an Actor System

    val connectionOptions = MongoConnectionOptions(connectTimeoutMS = 10)

    val connection = driver.connection(List(host), connectionOptions) //a connection manages a pool of connections

    // Gets a reference to the database "data-pipe"
    val database = connection(databaseName)

    collections match {
      case Nil =>  collections.::(MongoCollections.TEXT_PILL_REPO).::(MongoCollections.SOCIAL_PILL_REPO)
        .foreach(collectionName => collectionMap = collectionMap.updated(collectionName,database(collectionName)))

      case head::tail => collections
        .foreach(collectionName => collectionMap = collectionMap.updated(collectionName,database(collectionName)))
    }
  }
}

object MongoCollections {
  val TEXT_PILL_REPO = "raw_pill"
  val SOCIAL_PILL_REPO = "canonical_pill"
}
