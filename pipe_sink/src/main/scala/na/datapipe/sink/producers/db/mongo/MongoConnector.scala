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

    // Gets a reference to the database "customer_mind"
    val database = connection(databaseName)

    collections match {
      case Nil =>  collections.::(MongoCollections.RAW_POST).::(MongoCollections.CANONICAL_POST)
        .foreach(collectionName => collectionMap = collectionMap.updated(collectionName,database(collectionName)))

      case head::tail => collections
        .foreach(collectionName => collectionMap = collectionMap.updated(collectionName,database(collectionName)))
    }
  }
}

// http://127.0.0.1:28017/customer_mind/$cmd/?filter_count=canonical_post_twitter_file&limit=1
// http://127.0.0.1:28017/customer_mind/canonical_post_twitter_file/

object MongoCollections {
  val RAW_POST = "raw_post"
  val CANONICAL_POST = "canonical_post_twitter_file"
}
