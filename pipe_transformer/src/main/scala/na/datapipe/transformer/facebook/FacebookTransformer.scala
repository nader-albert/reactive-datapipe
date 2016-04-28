package na.datapipe.transformer.facebook

import akka.actor.{ActorRef, Props}
import com.google.gson.{JsonParser, JsonElement, JsonObject}

import na.datapipe.model.social._
import na.datapipe.transformer.DataTransformer
import na.datapipe.transformer.model.Transform
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.collection.immutable.HashSet
import scala.util.Random

/**
 * @author nader albert
 * @since  21/07/2015.
 */

case class ElementTransformed(lineId :Int)

class FacebookTransformer(requester: ActorRef) extends DataTransformer {

  val publisher = context.actorSelection("akka.tcp://publishers@" + "127.0.0.1" + ":" + "2556" + "/user/publisher")

  var posts = HashSet[String]()

  override protected def transformPost(msg: Transform): List[SocialInteraction] = {
    val jsonParser = new JsonParser
    val postJsonObject = jsonParser.parse(msg.dataPill.body).asInstanceOf[JsonObject]

    import FacebookTransformer._

    generatePosts(postJsonObject)
  }
}

object FacebookTransformer {
  def props(requester: ActorRef) =
    Props(classOf[FacebookTransformer], requester)

  def generatePosts(jsonPost: JsonObject): List[SocialInteraction] = {
    var socialPostList = List.empty[SocialInteraction]

    /**
     * Conversation is not set, and thus it will be defaulted to None by the toPost method, used indirectly by the
     * SocialPost apply method, invoked here. A new conversation will then be first created in this call. Will then be
     * used as a link between the parent post and comments and likes associated with this post.
     */
    val parentPost = SocialInteraction(jsonPost)
    val enclosingConversation = parentPost conversation

    val jsonComments = jsonPost getAsJsonObject "comments"

    if (!jsonComments.isJsonNull) {
      val iterator = jsonComments.getAsJsonArray("data").iterator

      while (iterator hasNext)  //shall comments be considered as another post and be recursive ?!
        socialPostList = socialPostList.::(
          SocialInteraction(iterator.next.getAsJsonObject, enclosingConversation.get, parentPost))
    }

    val jsonLikes = jsonPost getAsJsonObject "likes"

    if (null != jsonLikes && ! jsonLikes.isJsonNull) {
      val iterator = jsonLikes getAsJsonArray "data" iterator

      while (iterator hasNext) {
        val likeJsonObject = iterator.next.getAsJsonObject
        val postSource = PostSource(List(likeJsonObject.get("id").getAsLong))
        val poster = Poster.withNameOnly(likeJsonObject.get("name"))

        socialPostList = socialPostList.::(
          SocialInteraction.engagement(postSource, poster, enclosingConversation.get, parentPost))
      }
    }
    socialPostList.::(parentPost)
  }

  /**
   * @param jsonObject, corresponding to the 'to' section. could be null, if no mentions are in this post
   * @return, a list of Mention objects, each corresponding to one user mention or an empty list if no user mentions
   * appear in the given text,
   **/
  implicit def toMentions(jsonObject: JsonElement): List[Mention] = {
    var user_mentions = List.empty[Mention]

    if(null != jsonObject && jsonObject.isJsonObject) {
      val iterator = jsonObject.getAsJsonObject.getAsJsonArray("data").iterator

      while (iterator hasNext) {
        val mention = iterator.next
        user_mentions = user_mentions.::(
          Mention(mention.getAsJsonObject.get("id").getAsLong, mention.getAsJsonObject.get("name").getAsString))
      }
    }
    user_mentions
  }

  implicit def toTextString(jsonElement: JsonElement): String = {
    require(null != jsonElement && !jsonElement.isJsonNull, "post message text not supplied")
    jsonElement.getAsString
  }

  implicit def toPostType(jsonElement: JsonElement): PostType = {
    //require(null != jsonElement && !jsonElement.isJsonNull, "post type not supplied")
    if (null == jsonElement || jsonElement.isJsonNull)
      PostTypes.COMMENT
    else {
      jsonElement.getAsString match {
        case "status" => /*StatusPost*/ PostTypes.STATUS
        case "photo" => /*PhotoPost*/ PostTypes.PHOTO
        case "video" => /*VideoPost*/ PostTypes.VIDEO
        case "link" => /*LinkPost*/ PostTypes.LINK
        case _ => /*CommentPost*/ PostTypes.COMMENT  //TODO: check if the assumption of falling back to the Comment type is correct !
      }
    }
  }

  implicit def toURL(source_id: PostSource): URL = {
    require(source_id.identifiers.size == 2, "source id doesn't conform to facebook api standards ")
    URL(
      String.format("https://www.facebook.com/%s/posts/%s", source_id.identifiers.head.toString, source_id.identifiers.last.toString))
  }

  implicit def toURL(urlText: JsonElement): URL = if (null != urlText && !urlText.isJsonNull) URL(urlText) else URL.empty

  implicit def toDate(dateTimeString: JsonElement): DateTime = {
    require(null != dateTimeString && !dateTimeString.isJsonNull)
    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").parseDateTime(dateTimeString.getAsString)
  }

  implicit def toPostSource(source_id: JsonElement): PostSource = PostSource(source_id.split("_").toList.map(_.toLong))

  /**
   **/
  implicit def toPoster(json: JsonObject): Poster = {
    require(null != json && !json.isJsonNull, "poster information not supplied")

    var posterCategory = PosterCategories.EMPTY

    if(null != json.get("category") && !json.get("category").isJsonNull) {
      posterCategory = json.get("category").getAsString match {
        case "Company" => PosterCategories.COMPANY
        case _ => PosterCategories.EMPTY //TODO: see what other possible categories exist and match them !
      }
    }

    Poster(json.get("id").getAsLong, json.get("name"), posterCategory, 0, 0)
  }

  /**
   * to be used by the SocialPost.apply(jsonPost: JsonObject) and SocialPost.apply(jsonText: String)
   **/
  implicit def toPost(source: String, jsonPost: JsonObject): SocialInteraction = toPost(source, jsonPost, None, None)

  /**
   * This is the sole json to facebook transformer.. All conversion actions, eventually  fall back to this method.
   * should support all 4 types of Facebook Posts, mainly, Video, Photo, Status and Link
   **/
  implicit def toPost(source: String, jsonPost: JsonObject, conversation: Option[Conversation] = None,
                      replyTo: Option[SocialInteraction] = None): SocialInteraction = {

    //if this post belongs to an existing conversation, then the enclosing parent post should be identified as a valid replyTo option.
    //if no conversation exists, then the replyTo shouldn't be defined as well..
    require(conversation.isDefined && replyTo.isDefined || conversation.isEmpty && replyTo.isEmpty,
      "enclosing post and conversation link don't match ")

    // TODO: According to the old implementation, in case the message field is empty:
    // 1- Text field could be captured from the caption, in case that's a PhotoPost
    // 2- Text field could be read from the description field, in case the current post is a LinkPost
    val text: String = jsonPost get "message"

    val postType: PostType = jsonPost get "type"

    val created_time: DateTime = jsonPost get "created_time"
    val channel: PostChannel = PostChannels.FACEBOOK
    val postSource: PostSource = jsonPost get "id"
    val poster: Poster = jsonPost getAsJsonObject "from"
    val url: URL = postSource // an implicit will be able to infer the correct post URL, from the source id.

    // if a conversation exists, i.e. Conversation is not None, then that means that this post is part of a broader
    // conversation and thus it is a reply and the replyTO can be retrieved from the given conversation.
    //val replyTo = //points to the parent post if this a Comment or a Like
    val media_links = postType match {
      case postType: PostType if postType.name == "video" =>
        (List(Video(MediaTypes.VIDEO, jsonPost.get("link"), jsonPost.get("picture"))), List.empty[URL])
      case postType: PostType if postType.name == "photo" =>
        (List(Photo(MediaTypes.PHOTO, jsonPost.get("link"), jsonPost.get("picture"))), List.empty[URL])
      //TODO: we are not mapping the 'name' field as the old implementation does
      case postType: PostType if postType.name == "link" =>
        (List.empty[Media], List(URL(jsonPost.get("link")))) //TODO: we are not mapping the 'name' field as the old implementation does
      case _ => (List.empty[Media], List.empty[URL]) //TODO: verify if that's the correct behavior.. If it is a Status or a Comment or a Like, ignore Media and Link...
    }

    val hashTags = Nil //TODO: check if this will be applicable for facebook posts
    val mentions = jsonPost get "to"
    val project = Project.empty
    val conversation_link = if (conversation.isDefined) conversation.get else Conversation(Random.nextLong)

    SocialInteraction(source, postSource, text, created_time, channel, postType, url, media_links._2, project, Some(poster),
      medias = media_links._1, hashTags, mentions, Some(conversation_link), replyTo, lang = "en")
  }
}