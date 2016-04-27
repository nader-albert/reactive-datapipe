package na.datapipe.transformer.twitter

import akka.actor.{Terminated, ActorRef, Props}
import com.google.gson._
import com.google.gson.JsonNull

import na.datapipe.process.model.{ProcessPill, ProcessorJoined}
import na.datapipe.transformer.DataTransformer
import na.datapipe.transformer.model.Transform
import na.datapipe.model._

import languageFeature.implicitConversions._
import scala.util.Random

/**
 * @author nader albert
 * @since  4/08/2015.
 */
class TwitterTransformer(/*processingEngines: Seq[ActorRef],*/analyzersSystemHost: String, analyzersSystemPort: String )
  extends DataTransformer {

  var processingEngines = IndexedSeq.empty[ActorRef]
  var jobCounter = 0

  //val analyzer = context.actorSelection("akka.tcp://nlp@" + analyzersSystemPort + ":" + analyzersSystemPort + " /user/analyzer")

  //val publisher = context.actorSelection("akka.tcp://publishers@" + "127.0.0.1" + ":" + "2556" + "/user/publisher")

  //val sparkProcessorUrl = "akka.tcp://sparkDriver@127.0.0.1:7777/user/Supervisor0/processor"
  //val timeout = 20 seconds
  //val sparkProcessor: ActorRef = Await.result(context.actorSelection(sparkProcessorUrl).resolveOne(timeout), timeout)

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = println("twitter transformer is restarting because of: " + reason)

  override def postRestart(reason: Throwable): Unit = println("twitter transformer has been restarted")

  override def receive: Receive = {

    case transformTweet :Transform if processingEngines.isEmpty => println ("no available processors !")

    case Transform(pill, id) =>
      val text = pill.body

      log info (s"transforming ... $text ")
      /**
       * send the transformed post directly to the publisher, so that it can be passed to the
       * relevant listeners as is, without any further processing */
      //publisher ! PublishRawPost(convertedPost)

      import TwitterTransformer._

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
  }
}

object TwitterTransformer {
  def props(analyzersSystemHost: String, analyzersSystemPort: String) =
      Props(classOf[TwitterTransformer], analyzersSystemHost, analyzersSystemPort)

  import languageFeature.implicitConversions

  private def convert(source: String): JsonObject = {
    val parser: JsonParser = new JsonParser
    parser.parse(source).asInstanceOf[JsonObject]
  }

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
  }
}
