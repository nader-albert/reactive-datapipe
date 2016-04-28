package na.datapipe.model.social

import com.google.gson._

import org.joda.time.DateTime

import scala.util.{Success, Failure, Try, Random}

/**
 * @author nader albert
 * @since  19/11/2015.
 *
 */

trait Identifiable {
  val identifier: Long
  def identifierString = identifier.toString
}

/**
 * This trait is created to serve the PostSource id, coming with the Facebook API. It is always composed out of two
 * identifiers, where the first represents the parent post and the second represents the actual post identifier. They
 * are usually separated with '_'. We capture them as a list of Long, with a utility method 'identifierString', that is
 * able to return them back in the original composed string format. This gives the users of this canonical model to deal
 * with each of the two identifiers separately, or compose them together in one string.
 * */
trait ComposedIdentifiable{
  val identifiers: List[Long]

  def identifierString =
    identifiers.fold("")((acc,identity) => if (acc == "") identity.toString else acc + "_" + identity.toString).toString
}

trait Nameable {
  val name: String
}

case class Poster(override val identifier: Long, override val name: String, category: PosterCategory, followersCount: Int, friendsCount: Int)
  extends Identifiable with Nameable

object Poster {
  def empty = Poster(-1, "", PosterCategories.EMPTY,0, 0)
  def withNameOnly(name: String) = Poster(-1, name, PosterCategories.EMPTY, 0, 0)
}

case class PostSource(override val identifiers: List[Long]) extends ComposedIdentifiable

object PostSource {
  def empty = PostSource(List(-1.toLong))
  //def apply(identifiersText: List[String]):PostSource = PostSource(identifiersText.map(str => str.toLong))
}

case class Project(override val identifier: Long, override val name: String) extends Identifiable with Nameable

object Project {
  def empty = Project(-1, "")
}

trait Media {
  val mediaType: MediaType
  val link: URL
}

case class Photo(override val mediaType :MediaType = MediaTypes.PHOTO, override val link :URL, picture: URL)
  extends Media

case class Video(override val mediaType :MediaType = MediaTypes.VIDEO, override val link :URL, screenShot: URL)
  extends Media

/*case class Link(override val mediaType :MediaType = LINK, override val url :URL, picture: URL)
  extends Media(mediaType, url)*/

case class GeographicalLocation(longitude: Long, latitude: Long)

case class Reply(override val identifier: Long) extends Identifiable

case class Conversation(override val identifier: Long) extends Identifiable

object Conversation {
  def empty = Conversation(-1)
}

case class SocialPoster(override val identifier: Long, override val name: String) extends Identifiable with Nameable

case class HashTag(override val name: String) extends Nameable {
  //require(name.startsWith("#"))
}

case class URL(override val name: String) extends Nameable {
  require(name.startsWith("http://") || name.startsWith("https://"), "url requirement not satisfied")
}

object URL {
  def empty = URL("http://")
}

case class Mention(override val identifier: Long, override val name: String) extends Nameable with Identifiable

/**
 * Direct instantiation of SocialInteraction model is forbidden. It has to come through the exposed apply/empty methods,
 * provided by the associated companion object. An obvious scenario that is necessary to be avoided, and thus
 * required this class to be made private; is instantiating a SocialPost, with isEmpty = true, while the other attributes
 * are not made empty. Another case, would be to instantiate a SocialPost whose state, is not aligned with the enclosed
 * rawData. [How can we really prevent the second case from happening?]
 * Note that the private keyword has to come right before the constructor, so as to make the constructor
 * private while the class definition remains accessible from outside
 * */
case class SocialInteraction private(rawData: String, source_id: PostSource, text: String, creation_at: DateTime,
                                     channel: PostChannel, postType: PostType, url: URL, is_engagement: Boolean,
                                     is_reply: Boolean, media: List[Media], mentions: List[Mention],
                                     hashTags: List[HashTag], links: List[URL], project: Project, poster: Option[Poster],
                                     conversation: Option[Conversation], replyTo: Option[SocialInteraction],
                                     lang: String, is_parsed: Boolean, is_empty: Boolean)

  extends Identifiable {

  override val identifier = Random.nextLong
}

object SocialInteraction {

  /*implicit def toPost(json: JsonObject): SocialPost =
    SocialPost(json.get("id"), json.getAsJsonObject("user"),
      if (json.get("geo") == null || json.get("geo").isInstanceOf[JsonNull]) None else Some(json.getAsJsonObject("geo")),
      toText(json.get("text"), json.get("lang")), json.get("retweeted"), None
      /*json.getAsJsonObject("retweeted_status")*/, json.get("entities"))
*/
  /**
   * @param evidence, an implicit evidence has to be supplied, to confirm that this raw data, can be converted into a
   *                SocialInteraction
   * @return SocialPost, enclosing the raw input data in json format, and the model state populated with the parsed data,
   *         if json parsing succeeds. If json parsing fails due to IllegalArgumentException or UnsupportedException,
   *         an empty version of the SocialPost will be returned, enclosing the rawData un-parsed. Json parsing can be
   *         attempted later one. The parsing could later succeed, if we manage to solve the problem that led to this
   *         failure, and then rerun the failing models... They can later be distinguished via their is_empty flag.
   * */
  def apply(rawData: String)(implicit evidence: (String, JsonObject) => SocialInteraction): SocialInteraction = {
    val jsonParser = new JsonParser
    Try {
      evidence(rawData, jsonParser.parse(rawData).asInstanceOf[JsonObject])
    } match {
      case Failure(exception: JsonSyntaxException) => println("malformed json text")
        raw(rawData) //Just return an empty SocialPost, that just has the rawData un-parsed in case the JSON parsing fails.
      case Failure(exception: IllegalArgumentException) => println("input not supplied: or inproperly supplied " + exception.getMessage)
        raw(rawData)
      case Failure(exception: UnsupportedOperationException) => println(exception.getStackTrace.mkString)
        raw(rawData)
      case Failure(exception: Exception) => println(exception.getStackTrace.mkString)
        raw(rawData)
      case Success(post) => post
    }
  }

  /**
   * @param evidence, an implicit evidence has to be supplied, that is capable of converting the given jsonObject into
   *                a SocialPost
   * */
  def apply(jsonPost: JsonObject)(implicit evidence: (String, JsonObject) => SocialInteraction): SocialInteraction = {
    val rawData = jsonPost.toString
    Try {
      evidence(rawData, jsonPost)
    } match {
      case Failure(exception: JsonSyntaxException) => println("malformed json text")
        raw(rawData) //Just return an empty SocialPost, that just has the rawData un-parsed in case the JSON parsing fails.
      case Failure(exception: IllegalArgumentException) => println("input not satisfied: " + exception.getMessage)
        raw(rawData)
      case Failure(exception: UnsupportedOperationException) => println(exception.getStackTrace.mkString)
        raw(rawData)
      case Failure(exception: Exception) => println(exception.getStackTrace.mkString)
        raw(rawData)
      case Success(post) => post
    }
  }

  /**
   * @param evidence, an implicit evidence has to be supplied, that is capable of converting the given jsonObject into
   *                a SocialPost
   * */
  def apply(jsonPost: JsonObject, conversation: Conversation, parentPost: SocialInteraction)
           (implicit evidence: (String, JsonObject, Option[Conversation], Option[SocialInteraction]) => SocialInteraction): SocialInteraction = {
    val rawData = jsonPost.toString
    Try {
      evidence(rawData, jsonPost, Some(conversation), Some(parentPost))
    } match {
      case Failure(exception: JsonSyntaxException) => println("malformed json text")
        raw(rawData) //Just return an empty SocialPost, that just has the rawData un-parsed in case the JSON parsing fails.
      case Failure(exception: IllegalArgumentException) => println("input not satisfied: " + exception.getMessage)
        raw(rawData)
      case Failure(exception: UnsupportedOperationException) => println(exception.getStackTrace.mkString)
        raw(rawData)
      case Failure(exception: Exception) => println(exception.getStackTrace.mkString)
        raw(rawData)
      case Success(post) => post
    }
  }

  /**
   * This factory method is used by non-engagement posts (status, video, photo, tweet, re-tweet, comment).
   * Engagement Posts similar to Facebook Like, shouldn't be created through this method. That's why the is_engagement is
   * set to false
   * */
  def apply(rawData: String, source_id: PostSource, text: String, created_time: DateTime,
            channel: PostChannel, postType: PostType, url: URL = URL.empty,
            links: List[URL]= Nil, project: Project = Project.empty, poster: Option[Poster]= None,
            medias: List[Media]= Nil, hashTags: List[HashTag]= Nil, mentions: List[Mention]= Nil,
            conversation: Option[Conversation]= None, replyTo: Option[SocialInteraction]= None,
            lang: String = "en") = {

    //if no source raw data, this instance will be considered an empty one, and all given parameters will be ignored
    if (rawData.isEmpty) empty

    new SocialInteraction(rawData, source_id, text, created_time, channel, postType, url, is_engagement = false,
      is_reply = replyTo.isDefined, medias, mentions, hashTags, links, project, poster, conversation, replyTo,
      lang, is_parsed = true, is_empty = false)
  }

  def raw(rawText: String) =
    new SocialInteraction(rawText, PostSource(List(-1.toLong)), "", DateTime.now, PostChannels.UNDEFINED, PostTypes.UNDEFINED,
      URL("http://"), false, false, Nil, Nil, Nil, Nil, Project(-1,""), None, None, None, "N/A",
      is_parsed = false, is_empty = false)

  /**
   * defines what an engagement post really is. This is the sole point responsible for creating an engagement post,
   * basically a Like in Facebook Language. Since the actual SocialPost constructor is made private, no one except the
   * companion object will ever have access to it. According to the FacebookApi, a Like engagement, only wraps a unique
   * identifier and the name of the person who made the Like Action. This definition is likely to change with other data
   * sources(datasift) and other data channels (instagram) for example. This would involve a change to the
   * method signature, to reflect these changes. Irrespective of the Source and the Channel, an engagement post is considered
   * as a subsidiary post that must have a Conversation Link and Parent SocialInteraction, set as a replyTo.
   * */
  def engagement(postSource: PostSource, poster: Poster, conversationLink: Conversation, parentPost: SocialInteraction) =
    new SocialInteraction("", postSource, "", parentPost.creation_at, PostChannels.FACEBOOK, PostTypes.LIKE,
      URL("http://"), is_engagement= true, is_reply= true, Nil, Nil, Nil, Nil, Project(-1,""), None,
      conversation = Some(conversationLink), replyTo = Some(parentPost), "N/A", is_parsed = true, is_empty = false)

  def empty =
    new SocialInteraction("", PostSource(List(-1.toLong)), "", DateTime.now, PostChannels.UNDEFINED, PostTypes.UNDEFINED,
      URL("http://"), is_engagement = false, is_reply = false, Nil, Nil, Nil, Nil, Project(-1,""), None,
      conversation = None, replyTo = None, "N/A", is_parsed = false, is_empty = true)
}

object MediaTypes{
  val VIDEO = MediaType("video")
  val PHOTO = MediaType("photo")
  val UNKNOWN = MediaType("")
}

case class MediaType(name: String)

object PostTypes {
  val STATUS = /*StatusPost*/ PostType("status")
  val VIDEO = /*VideoPost*/ PostType("video")
  val PHOTO = /*PhotoPost*/ PostType("photo")
  val LINK = /*LinkPost*/ PostType("link")

  val COMMENT = /*CommentPost*/ PostType("comment")
  val LIKE = /*LikePost*/ PostType("like")
  val TWEET = /*TweetPost*/ PostType("tweet")
  val RE_TWEET = /*RetweetPost*/ PostType("re-tweet")
  val UNDEFINED = PostType("undefined")
}

case class PostType(name: String)


object PostChannels {
  val FACEBOOK = PostChannel("Facebook")
  val TWITTER = PostChannel("Twitter")
  val UNDEFINED = PostChannel("undefined")
}

object PosterCategories {
  val COMPANY = PosterCategory("company")
  //val TWITTER = PostChannel("Twitter")
  //val UNDEFINED = PostChannel("undefined")
  val EMPTY = PosterCategory("")
}

case class PosterCategory(name: String)

case class PostChannel(name: String)