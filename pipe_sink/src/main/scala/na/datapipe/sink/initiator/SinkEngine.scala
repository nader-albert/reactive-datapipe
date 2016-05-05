package na.datapipe.sink.initiator

import akka.actor.ActorSystem
import akka.camel.CamelExtension
import com.typesafe.config.{Config, ConfigFactory}
import na.datapipe.model.TextPill
import na.datapipe.sink.SinkGuardian
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{MongoConnectionOptions, MongoDriver}
import reactivemongo.bson.{Macros, BSONDocument}

import scala.util.{Success, Failure, Random}

import scala.concurrent.Future
import reactivemongo.bson.{ BSONDocument, BSONDocumentReader, Macros }
import reactivemongo.api.collections.bson.BSONCollection
/**
 * @author nader albert
 * @since  6/08/2015.
 */
object SinkEngine extends App {
  implicit val system = ActorSystem("ClusterSystem")

  val config = ConfigFactory load

  val appConfig: Config = config getConfig "sink_app"

  val camel = CamelExtension(system)

  implicit val camelContext = camel context

  //Initializer.initialize(camelContext)
  //val p: Post = new Post

  //camelContext.setLazyLoadTypeConverters(true)

  //val producerTemplate = camel template

  //val engBean: RabbitTemplate = ctx.getBean("engagementQueue", classOf[RabbitTemplate])

  //println(Some("nader") flatMap(value => Some(value + "noha")) flatMap (value2 => Some (value2 + "Rami")) get)
  //println(Some("nader") flatMap(value => Some(value + "noha") flatMap (value2 => Some (value2 + "Rami"))) get)

  val sink = system.actorOf(SinkGuardian.props(appConfig), name = "sink")

  import scala.concurrent.ExecutionContext.Implicits.global

  //val pill = TextPill("NADER", Some(Map("NADER" -> "NOHA")), 1)

  var pillCollection: BSONCollection = connect

  /*val pillDocument = BSONDocument(
    "body" -> pill.body.toString,
    "header" -> pill.header.toString, // TODO: header should be inserted as another document...
    "id" -> Random.nextInt)

  val insertFuture = pillCollection.insert(pillDocument)

  insertFuture.onComplete {
    case Failure(e) => throw e
    case Success(writeResult) =>
      println(s"successfully inserted document with result: $writeResult")
  } */

  import scala.concurrent.ExecutionContext.Implicits.global

  val findAndUpdateFuture = pillCollection.findAndUpdate(
    BSONDocument("body" -> "NADER"),
    BSONDocument("$set" -> BSONDocument("header" -> "YOUSSEF")),
    fetchNewObject = true)

  findAndUpdateFuture.onComplete {
    case Failure(e) => throw e
    case Success(writeResult) =>
      println(s"successfully find and modify document with result: $writeResult")
  }

  val query1 = BSONDocument("body" -> BSONDocument("$gt" -> "NADER"))
  val query2 = BSONDocument("body" -> "NADER")

  val findFuture1 = pillCollection.find(query1).cursor[BSONDocument].collect[List]()
  val findFuture2 = pillCollection.find(query2).cursor[BSONDocument].collect[List]()

  findFuture1.onComplete {
    case Failure(e) => throw e
    case Success(writeResult) =>
      println(s"successfully find with query 1 document with result: $writeResult")
  }

  findFuture2.onComplete {
    case Failure(e) => throw e
    case Success(docList) =>
      println("successfully find with query 2 document with result:" + docList.size)
      docList.foreach { doc =>
        println("body is: " + doc.get("body")); println("header is:" + doc.get("header"))
      }
  }

  protected def connect = {
    val driver = new MongoDriver //gets an instance of the driver - under the cover it manages an Actor System

    val connectionOptions = MongoConnectionOptions(connectTimeoutMS = 10)

    val connection = driver.connection(List("localhost"), connectionOptions) //a connection manages a pool of connections

    // Gets a reference to the database "datapipe"
    val database = connection("datapipe")

    // Gets a reference to the collection "pills". we get a BSONCollection by default.
    database("pills")
  }
}

