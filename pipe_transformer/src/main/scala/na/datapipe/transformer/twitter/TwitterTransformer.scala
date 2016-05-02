package na.datapipe.transformer.twitter

import akka.actor.{Terminated, Props}
import com.google.gson._
import na.datapipe.model.{TweetPill, TextPill, SocialPill, Command}
import na.datapipe.model.social.SocialInteraction

import na.datapipe.model.social._
import na.datapipe.transformer.DataTransformer

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.util.Random

import languageFeature.implicitConversions._

/**
 * @author nader albert
 * @since  4/08/2015.
 */
class TwitterTransformer extends DataTransformer {

  //val analyzer = context.actorSelection("akka.tcp://nlp@" + analyzersSystemPort + ":" + analyzersSystemPort + " /user/analyzer")

  //val publisher = context.actorSelection("akka.tcp://publishers@" + "127.0.0.1" + ":" + "2556" + "/user/publisher")

  //val sparkProcessorUrl = "akka.tcp://sparkDriver@127.0.0.1:7777/user/Supervisor0/processor"
  //val timeout = 20 seconds
  //val sparkProcessor: ActorRef = Await.result(context.actorSelection(sparkProcessorUrl).resolveOne(timeout), timeout)

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = println("twitter transformer is restarting because of: " + reason)

  override def postRestart(reason: Throwable): Unit = println("twitter transformer has been restarted")

  protected def transformPost(pill: TextPill): List[SocialInteraction] = {

    import TwitterTransformer._

    // Since the evidence this TwitterApiTransformer provides can only generate a single SocialPost, then it has to be
    // wrapped in a list, so as to comply to the transformPost method signature

    List(SocialInteraction(pill.body))
  }

  /*override def receive: Receive = {

    case transformTweet :Transform if processingEngines.isEmpty => println ("no available processors !")

    case Transform(pill, id) =>
      val text = pill.body

      log info (s"transforming ... $text ")
      /**
       * send the transformed post directly to the publisher, so that it can be passed to the
       * relevant listeners as is, without any further processing */
      //publisher ! PublishRawPost(convertedPost)

      /**
       * send the transformed post to spark engine for further processing
       * */

      jobCounter += 1

      processingEngines(jobCounter % processingEngines.size) ! ProcessPill(pill)

      log info "tweet transformed and sent to processor"

    //sparkProcessor ! ProcessTweet(text)

    //TODO: The two cases below should be moved to the parent DataTransformer
    case ProcessorJoined if !processingEngines.contains(sender) =>
      log debug "Twitter Transformer: -> a new processor added to the list !"
      context watch sender
      processingEngines = processingEngines :+ sender

      println("number of processors is now: " + processingEngines.size)

    case Terminated(a) =>
      println("one processor has left cluster ! " + "[ " + a + " ]")
      processingEngines = processingEngines.filterNot(_ == a)
  }*/
}

object TwitterTransformer {
  def props =
      Props(classOf[TwitterTransformer])

  import languageFeature.implicitConversions

  private def convert(source: String): JsonObject = {
    val parser: JsonParser = new JsonParser
    parser.parse(source).asInstanceOf[JsonObject]
  }

  import scala.languageFeature.implicitConversions

  implicit def toHashTags(jsonElement: JsonObject): List[HashTag] = {
    require(null != jsonElement)

    var hashTags = List.empty[HashTag]
    val iterator = jsonElement.getAsJsonArray("hashtags").iterator

    while(iterator hasNext)
      hashTags = hashTags.::(HashTag(iterator.next.getAsJsonObject.get("text").getAsString)) //TODO: How to append instead of prepend

    hashTags
  }

  implicit def toMentions(jsonObject: JsonObject): List[Mention]= {
    require(null != jsonObject && !jsonObject.isJsonNull)

    var user_mentions = List.empty[Mention]
    val iterator = jsonObject.getAsJsonArray("user_mentions").iterator

    while(iterator hasNext) {
      val mention = iterator.next
      if(null != mention && !mention.isJsonNull && mention.getAsJsonObject.get("id") != null)
        user_mentions = user_mentions.::(Mention(mention.getAsJsonObject.get("id").getAsLong,
          mention.getAsJsonObject.get("name").getAsString))
    }

    user_mentions
  }

  implicit def toLinks(jsonObject: JsonObject): List[URL]= {
    require(null != jsonObject && !jsonObject.isJsonNull)
    var user_links = List.empty[URL]
    val iterator = jsonObject.getAsJsonArray("urls").iterator

    while(iterator hasNext)
      user_links = user_links.::(URL(iterator.next.getAsJsonObject.get("url").getAsString))

    user_links

    //collectionExtractor[URL](jsonObject.getAsJsonArray("urls"), )
  }

  implicit def toMedia(jsonArray: JsonArray): List[Media] = {
    require(null != jsonArray && !jsonArray.isJsonNull)
    var mediaList = List.empty[Media]

    val mediaIterator = jsonArray.iterator
    while (mediaIterator hasNext) {
      val mediaJsonObject = mediaIterator.next.getAsJsonObject

      if (null != mediaJsonObject.get("type") && ! mediaJsonObject.isJsonNull)
        mediaList =
          mediaJsonObject.get("type").getAsString match {
            case v if v == "video" => mediaList.::(Video(/*mediaJsonObject.get("type")*/link = URL(mediaJsonObject.get("media_url")), screenShot = URL.empty))
            case p if p == "photo" => mediaList.::(Photo(/*mediaJsonObject.get("type")*/link = URL(mediaJsonObject.get("media_url")), picture = URL.empty))
            case _ => mediaList
          }
      //mediaList = mediaList.::(Photo(MediaTypes.PHOTO,/*mediaJsonObject.get("type")*/URL.empty, URL(mediaJsonObject.get("media_url"))))
    }
    mediaList
  }

  /*private def collectionExtractor[T] (jsonArray: JsonArray, extractor: (JsonElement => T)): List[T] = {
    require(null != jsonArray && !jsonArray.isJsonNull)
    var extracted_collection = List.empty[T]

    val iterator = jsonArray.iterator

    while(iterator hasNext)
      extracted_collection = extracted_collection.::(extractor(iterator.next))

    extracted_collection
  }*/

  implicit def toPoster(json: JsonObject): Poster = {
    require(null != json && !json.isJsonNull, "poster information not supplied")

    Poster(
      if (null == json.get("id")) 0 else json.get("id").getAsLong,
      if (null == json.get("name")) "" else json.get("name").getAsString,
      PosterCategories.EMPTY, //Not clear how to distinguish the type in a Tweet from TwitterApi
      if (null == json.get("followers_count")) 0 else json.get("followers_count").getAsInt,
      if (null == json.get("friends_count")) 0 else json.get("friends_count").getAsInt)
  }

  implicit def toReply(jsonObject: JsonObject)(implicit conversation_link: Conversation): Option[SocialInteraction]= {
    if(null == jsonObject || jsonObject.isJsonNull) None
    else Some(toPost(jsonObject.toString, jsonObject, Some(conversation_link)))
  }

  implicit def toTextString(jsonElement: JsonElement): String = {
    require(null != jsonElement && !jsonElement.isJsonNull, "tweet text not supplied")
    jsonElement.getAsString
  }

  implicit def toDate(dateTimeString: JsonElement): DateTime = {
    require(null != dateTimeString && ! dateTimeString.isJsonNull)
    DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy").parseDateTime(dateTimeString.getAsString)
  }

  implicit def toPost(source: String, jsonPost: JsonObject): SocialInteraction = toPost(source,jsonPost,None)

  //TODO: What to do with the case where we have for instance 100 retweets for the same tweet. all 100 retweets will have the same tweet as their parent post, each couple will have a unique conversation id though. The same parent tweet, will be posted to NLP 100 times.. that's a problem
  implicit def toPost(source: String, json: JsonObject, conversation: Option[Conversation]= None): SocialInteraction = {
    //TODO: This is a hack to support the file extracted from mongo.. needs to be changed

    var hackedJson :JsonObject = null

    if (json.get("text") == null || json.get("text").isJsonNull) {
      hackedJson = json.getAsJsonObject("tweet")
      if (hackedJson == null || hackedJson.isJsonNull)
        hackedJson = json
    } else hackedJson = json

    val text = hackedJson.get("text")
    val lang = hackedJson.get("lang")
    val created_time: DateTime = hackedJson.get("created_at")

    val postSource = PostSource(List(hackedJson.get("id").getAsLong))
    val poster:Poster = hackedJson.getAsJsonObject("user")
    val channel = PostChannels.TWITTER

    val url = URL("http://")  //TODO: locate the Tweet URL in the incoming model
    val project = Project(1,"") //Project name should come as a parameter in the message received

    //TODO: shall we link the post with its nested parents with the same conversation?
    implicit val conversation_link = if(conversation.isDefined) conversation.get else Conversation(Random.nextLong) //for the parent post, this will be generated, for the underneath posts (retweeted

    //The implicit that serves this call is the 'toReply' one.
    //The retweeted_status section of a Tweet, represents the parent original tweet, that this tweet has been retweeting.
    //i.e. the parent tweet is actually enclosed in the retweet, not the other way around
    val replyTo: Option[SocialInteraction] = json getAsJsonObject "retweeted_status" //TODO: check why the 'retweeted' is always set to false, if the Tweet was actually retweeted

    val entitiesInJson = hackedJson getAsJsonObject "entities"
    val hashTags: List[HashTag] = entitiesInJson
    val links: List[URL] = entitiesInJson
    val mentions: List[Mention] = entitiesInJson

    val extendedEntities = hackedJson getAsJsonObject "extended_entities"
    val media: List[Media]= if(null == extendedEntities || extendedEntities.isJsonNull) Nil else extendedEntities.getAsJsonArray("media")

    val theme = None
    //val sentiment =

    SocialInteraction(source, postSource, text, created_time, channel, if (replyTo.isDefined) PostTypes.RE_TWEET else PostTypes.TWEET
      ,url, links, project, Some(poster), medias = media, hashTags, mentions,
      Some(conversation_link) , replyTo, lang= lang)
  }

  /*
    OLD Twitter model Transformation ***

  implicit def toTweetPill(source: TextPill): TweetPill = convert(source.body)

  implicit def toBoolean (jsonElement: JsonElement): Boolean = jsonElement.getAsBoolean

  implicit def toInt (jsonElement: JsonElement): Int = if (null == jsonElement || jsonElement.isJsonNull) 0 else jsonElement.getAsInt

  implicit def toString (jsonElement: JsonElement): String = if (null == jsonElement || jsonElement.isJsonNull) "" else jsonElement.getAsString

  //implicit def toDouble (jsonElement: JsonElement): Double = if (jsonElement.isInstanceOf[JsonNull]) 0.0 else jsonElement.getAsDouble

  implicit def toLong (jsonElement: JsonElement): Long = if (null == jsonElement || jsonElement.isJsonNull) 0 else jsonElement.getAsLong

  implicit def toText (jsonText: JsonElement, jsonLang: JsonElement): Text = Text(jsonText, jsonLang)

  implicit def toArray (jsonArray: JsonArray): List[String] = {
    val hashTags = List[String] ()

    val arrayIterator = jsonArray.iterator

    while (arrayIterator.hasNext) {
      //println("hashtags .... " + arrayIterator.next.getAsJsonObject.get("text"))
      val nextHashTag = arrayIterator.next

     // if (null !=  && null != arrayIterator.next.getAsJsonObject && null != arrayIterator.next.getAsJsonObject.get("text"))
        hashTags.::(nextHashTag.getAsJsonObject.get("text").getAsString) // a JSon Element will be automatically converted to a String because of the implicit type conversion above.
    }
    hashTags
  }

  implicit def toHashTags(jsonElement: JsonElement): List[String] = jsonElement.getAsJsonObject.getAsJsonArray("hashtags")

  //implicit private def toOptionalRetweet(jsonElement: JsonObject): Option[Tweet] = Some()

  implicit def toGeographicalLocation(jsonObject: JsonObject): GeographicalLocation =
    GeographicalLocation(jsonObject.get("longitude"), jsonObject.get("latitude"))

  implicit def toUser (json: JsonObject): User =
    User(
      if(null == json || null == json.get("location") || json.isJsonNull) "" else json.get("location"),
      if(null == json || null == json.get("description") || json.isJsonNull) "" else json.get("description"),
      if(null == json || null == json.get("followers_count") || json.isJsonNull) 0 else json.get("followers_count"),
      if(null == json || null == json.get("friends_count") || json.isJsonNull) 0 else json.get("friends_count"))

  implicit def toTweetPill(json: JsonObject): TweetPill = {
    val tweet = Tweet(json.get("id"), json.getAsJsonObject("user"), if (json.get("geo") == null || json.get("geo").isInstanceOf[JsonNull]) None else Some(json.getAsJsonObject("geo")),
      toText(json.get("text"), json.get("lang")), json.get("retweeted"), None
      /*json.getAsJsonObject("retweeted_status")*/ , json.get("entities"))

    TweetPill(tweet, Some(Map.empty[String,String].updated("source", "twitter")), Random.nextInt(10000))
  } */

}
