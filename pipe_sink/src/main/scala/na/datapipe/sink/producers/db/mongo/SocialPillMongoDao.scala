package na.datapipe.sink.producers.db.mongo

import na.datapipe.model.social._
import na.datapipe.model.{SocialPill, Pill}
import org.joda.time.DateTime
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{Macros, BSONDocumentReader, BSONDocumentWriter, BSONDocument}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * @author nader albert
 * @since  3/05/16.
 */
trait SocialPillMongoDao extends PillMongoDao {
  this: MongoConnector =>

  import SocialPillMongoDao._
  import scala.concurrent.ExecutionContext.Implicits.global

  override def save(pill: Pill) =
    pill match {
      case social: SocialPill => collectionMap.get(MongoCollections.SOCIAL_PILL_REPO)
        .fold(Future.failed[WriteResult]
        (new IllegalArgumentException("canonical post collection not initialized")))(collection => collection.insert[SocialPill](social))

      case other: Pill => super.save(other) //Future.failed[WriteResult](new IllegalArgumentException("pill type unrecognizable"))
    }
}

object SocialPillMongoDao {

  import PillMongoDao._
  implicit val pillBSONWriter :BSONDocumentWriter[SocialPill] = Macros.writer[SocialPill]

  implicit val posterCategoryBSONHandler = Macros.handler[PosterCategory]
  implicit val posterBSONHandler = Macros.handler[Poster]
  implicit val projectBSONHandler = Macros.handler[Project]
  implicit val hashTagsBSONHandler = Macros.handler[HashTag]
  implicit val mentionsBSONHandler = Macros.handler[Mention]

  implicit val conversationBSONHandler = Macros.handler[Conversation]

  implicit val urlBSONHandler = Macros.handler[URL]

  /*implicit val videoBSONHandler = Macros.handler[Video]
  implicit val photoBSONHandler = Macros.handler[Photo]
  implicit val linkPostBSONHandler = Macros.handler[LinkPost]
  implicit val photoPostBSONHandler = Macros.handler[PhotoPost]
  implicit val videoPostBSONHandler = Macros.handler[VideoPost]
  implicit val statusPostBSONHandler = Macros.handler[StatusPost] */

  implicit val mediaBSONHandler = Macros.handler[MediaType]
  implicit val postTypeBSONHandler = Macros.handler[PostType]
  implicit val sourceBSONHandler = Macros.handler[PostSource]
  implicit val channelBSONHandler = Macros.handler[PostChannel]

  implicit val dateTimeWriter :BSONDocumentWriter[DateTime] = new BSONDocumentWriter[DateTime] {
    override def write(dateTime: DateTime) = BSONDocument("concatenated" -> dateTime.toDate.toString)
  }
  implicit object dateTimeReader extends BSONDocumentReader[DateTime] {
    def read(doc: BSONDocument): DateTime = {
      DateTime.parse(doc.getAs[String]("concatenated").get)
      //doc.getAs[Int]("releaseYear").get,
      //doc.getAs[String]("hiddenTrack"),
      //doc.getAs[Double]("allMusicRating"))
    }
  }

  implicit val mediaWriter :BSONDocumentWriter[Media] = new BSONDocumentWriter[Media] {
    override def write(media: Media) = media match {
      case video: Video => BSONDocument("mediaType" -> MediaTypes.VIDEO, "link" -> video.link, "screenShot" -> video.screenShot)
      case photo: Photo => BSONDocument("mediaType" -> MediaTypes.PHOTO, "link" -> photo.link, "screenShot" -> photo.picture)
    }
  }

  implicit object mediaReader extends BSONDocumentReader[Media] {
    def read(doc: BSONDocument): Media = {
      doc.getAs[MediaType]("mediaType").getOrElse(MediaTypes.UNKNOWN) match {
        case media: MediaType if media.name == "video" =>
          Video(link = doc.getAs[String]("link").fold(URL.empty)(URL(_)), screenShot = doc.getAs[String]("screenShot").fold(URL.empty)(URL(_)))
        case media: MediaType if media.name == "photo" =>
          Photo(link = doc.getAs[String]("link").fold(URL.empty)(URL(_)), picture = doc.getAs[String]("picture").fold(URL.empty)(URL(_)))
      }
    }
  }

  /**
   * the type of the writer and the reader had to be mentioned explicitly as it caused implicit type ambiguity, since a
   * SocialPost encloses a Social Post
   * */
  implicit val socialPostBSONWriter :BSONDocumentWriter[SocialInteraction] = Macros.writer[SocialInteraction]

  /**
   * had to manually write the BSONReader for SocialPost as it has a private constructor and hence cannot be accessed by the Macro
   * */
  //Macros.reader[SocialPost]

  implicit object socialPostBSONReader extends BSONDocumentReader[SocialInteraction] {
    def read(doc: BSONDocument): SocialInteraction =
      SocialInteraction(
        rawData = doc.getAs[String]("rawData").getOrElse(""),
        source_id = doc.getAs[PostSource]("source_id").getOrElse(PostSource(List(-1))),
        text = doc.getAs[String]("text").getOrElse(""),
        created_time = doc.getAs[DateTime]("creation_at").getOrElse(DateTime.now()),
        channel = doc.getAs[PostChannel]("channel").getOrElse(PostChannels.UNDEFINED),
        postType = doc.getAs[PostType]("postType").getOrElse(PostTypes.UNDEFINED),
        url = doc.getAs[URL]("url").getOrElse(URL.empty),
        //is_engagement = doc.getAs[Boolean]("is_engagement").getOrElse(false),
        //is_reply = doc.getAs[Boolean]("is_reply").getOrElse(false),
        links = doc.getAs[List[URL]]("links").getOrElse(Nil),
        project = doc.getAs[Project]("project").getOrElse(Project.empty),
        poster = doc.getAs[Poster]("poster"),
        medias = doc.getAs[List[Media]]("media").getOrElse(Nil),
        hashTags = doc.getAs[List[HashTag]]("hashTags").getOrElse(Nil),
        mentions = doc.getAs[List[Mention]]("mentions").getOrElse(Nil),
        conversation = doc.getAs[Conversation]("conversation"),
        replyTo = doc.getAs[SocialInteraction]("replyTo"),
        lang = doc.getAs[String]("lang").getOrElse("en"))
  }
}
