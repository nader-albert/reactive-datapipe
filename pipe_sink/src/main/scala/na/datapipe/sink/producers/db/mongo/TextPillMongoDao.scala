package na.datapipe.sink.producers.db.mongo

import na.datapipe.model.{TextPill, Pill}
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{Macros, BSONDocumentWriter}

import scala.concurrent.Future

/**
 * @author nader albert
 * @since  3/05/16.
 */
trait TextPillMongoDao extends PillMongoDao {
  this: MongoConnector =>

  import TextPillMongoDao._
  import scala.concurrent.ExecutionContext.Implicits.global

  override def save(pill: Pill) =
    pill match {
      case raw_pill: TextPill => collectionMap.get(MongoCollections.TEXT_PILL_REPO)
        .fold(Future.failed[WriteResult]
        (new IllegalArgumentException("raw pill collection not initialized")))(collection => collection.insert[TextPill](raw_pill))

      case other: Pill => super.save(other) //Future.failed[WriteResult](new IllegalArgumentException("pill type unrecognizable"))
    }
}

object TextPillMongoDao {
  import PillMongoDao._

  implicit val pillBSONWriter :BSONDocumentWriter[TextPill] = Macros.writer[TextPill]
}