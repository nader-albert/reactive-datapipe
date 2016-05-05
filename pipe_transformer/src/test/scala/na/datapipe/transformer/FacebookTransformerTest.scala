package na.datapipe.transformer

import na.datapipe.model.social._
import na.datapipe.transformer.facebook.FacebookTransformer
import org.scalatest.FlatSpec

import scala.io.Source

/**
 * @author nader albert
 * @since  3/12/2015.
 */
class FacebookTransformerTest extends FlatSpec with JsonParserTest {

  import na.datapipe.transformer.facebook.FacebookTransformer._

  //TODO:
  // 1- Map the media for Video and Photo
  // 2- Map the Link post type
  // 3- Map the hashtags and links

  verifyMentionsExtraction

  verifyStatusPostExtractionWithoutLikes
  verifyStatusPostExtractionWithLikes

  verifyVideoPostExtraction
  verifyPhotoPostExtraction
  verifyLinkPostExtraction

  private def verifyMentionsExtraction = {
    "User Mentions" should "have  1 mention, whose name is: \"Kmart Australia\"" in {
      val jsonText =
        "{ \"to\":" +
          "{\"data\":" +
            "[{\"category\":\"Retail and consumer merchandise\",\"id\":\"426016044163988\",\"name\":\"Kmart Australia\"}]" +
          "} " +
        "}"

      val mentions = toMentions(extractJsonObjectFromText(jsonText,"to"))

      assert(mentions.size === 1)
      assert(mentions.head.name === "Kmart Australia")
    }
  }

  private def verifyStatusPostExtractionWithLikes = {
    "Status Facebook Post" should "be extracted correctly with 2 comments and 2 likes" in {

      val jsonString = loadFile("facebookapi-status_post_with_likes-sample.json")

      val postList = FacebookTransformer.generatePosts(parse(jsonString))

      assert(postList.size === 16)

      val wrappingPost = postList.head
      val enclosedPosts = postList.tail

      val likePosts = enclosedPosts.take(10)  //TODO: what about paging comments and likes .... there are more comments and posts that we cannot get from the above data, yet they have their own link, to retrieve them
      val commentPosts = enclosedPosts.drop(10)

      assert(!wrappingPost.is_engagement && !wrappingPost.is_reply && wrappingPost.is_parsed && !wrappingPost.is_empty)
      assert(wrappingPost.postType === PostTypes.STATUS)
      assert(wrappingPost.channel === PostChannels.FACEBOOK)
      assert(wrappingPost.url === URL("https://www.facebook.com/196094017082910/posts/1140886412603661"))
      assert(wrappingPost.creation_at.dayOfMonth.get === 22)
      assert(wrappingPost.creation_at.monthOfYear.get === 5)
      assert(wrappingPost.creation_at.getYear === 2015)

      assert(wrappingPost.mentions.size == 1)
      assert(wrappingPost.mentions.head.name == "Target Australia")
      assert(wrappingPost.mentions.head.identifierString == "196094017082910")

      verifyLikes(likePosts,wrappingPost)
      verifyComments(commentPosts,wrappingPost)
    }
  }

  private def verifyStatusPostExtractionWithoutLikes ={
    "Status Facebook Post" should "be extracted correctly with 2 comments and 0 likes" in {
      val jsonString = loadFile("facebookapi-status_post_without_likes-sample.json")

      val postList = FacebookTransformer.generatePosts(parse(jsonString))
      assert(postList.size === 3)

      val wrappingPost = postList.head
      val enclosedPosts = postList.tail

      //val likePosts = enclosedPosts.take(2)  //TODO: what about paging comments and likes .... there are more comments and posts that we cannot get from the above data, yet they have their own link, to retrieve them
      val commentPosts = enclosedPosts.drop(3)

      assert(!wrappingPost.is_engagement && !wrappingPost.is_reply && wrappingPost.is_parsed && !wrappingPost.is_empty)
      assert(wrappingPost.postType === PostTypes.STATUS)
      assert(wrappingPost.channel === PostChannels.FACEBOOK)
      assert(wrappingPost.url === URL("https://www.facebook.com/140125992680153/posts/1147024225323653"))
      assert(wrappingPost.creation_at.dayOfMonth.get === 20)
      assert(wrappingPost.creation_at.monthOfYear.get === 5)
      assert(wrappingPost.creation_at.getYear === 2015)

      //verifyLikes(likePosts,wrappingPost)
      verifyComments(commentPosts,wrappingPost)
    }
  }

  private def verifyVideoPostExtraction ={
    "Video Facebook Post" should "have " in {
      val jsonString = loadFile("facebookapi-video_post-sample.json")

      val postList = FacebookTransformer.generatePosts(parse(jsonString))

      assert(postList.size === 51) //25 comments and 25 likes + the main post

      val wrappingPost = postList.head
      val enclosedPosts = postList.tail

      val likePosts = enclosedPosts.take(25)  //TODO: what about paging comments and likes .... there are more comments and posts that we cannot get from the above data, yet they have their own link, to retrieve them
      val commentPosts = enclosedPosts.drop(25)

      assert(!wrappingPost.is_engagement && !wrappingPost.is_reply && wrappingPost.is_parsed && !wrappingPost.is_empty)
      assert(wrappingPost.postType === PostTypes.VIDEO)

      assert(wrappingPost.media.size === 1)
      assert(wrappingPost.media.head.mediaType == MediaTypes.VIDEO)
      assert(wrappingPost.media.head.link == URL("https://www.facebook.com/KmartAustralia/videos/772078792891043/"))
      assert(wrappingPost.media.head.asInstanceOf[Video].screenShot == URL("https://fbcdn-vthumb-a.akamaihd.net/hvthumb-ak-xtf1/v/t15.0-10/p130x130/11189058_772080042890918_2057559468_n.jpg?oh=797f68e17b20b05af8bc7859f5254793&oe=55F2CA40&__gda__=1442013222_769b276479ca1c187ea0269c251491e6"))

      assert(wrappingPost.channel === PostChannels.FACEBOOK)
      assert(wrappingPost.url === URL("https://www.facebook.com/426016044163988/posts/772078792891043"))
      assert(wrappingPost.creation_at.dayOfMonth.get === 14)
      assert(wrappingPost.creation_at.monthOfYear.get === 5)
      assert(wrappingPost.creation_at.getYear === 2015)

      verifyLikes(likePosts,wrappingPost)
      verifyComments(commentPosts,wrappingPost)
    }
  }

  private def verifyPhotoPostExtraction ={
    "Photo Facebook Post" should "be parsed correctly and should enclose 2 comments and 22 like" in {

      val jsonString = loadFile("facebookapi-photo_post-sample.json")

      val postList = FacebookTransformer.generatePosts(parse(jsonString))

      val wrappingPost = postList.head
      val enclosedPosts = postList.tail
      val likePosts = enclosedPosts.take(25)
      val commentPosts = enclosedPosts.drop(25)

      assert(postList.size === 28)
      //First element in the returned list, is supposed to be the parent photo post, enclosing comments and ikes
      assert(!wrappingPost.is_engagement && !wrappingPost.is_reply && wrappingPost.is_parsed && !wrappingPost.is_empty)
      assert(wrappingPost.postType === PostTypes.PHOTO)

      assert(wrappingPost.media.size === 1)
      assert(wrappingPost.media.head.mediaType == MediaTypes.PHOTO)
      assert(wrappingPost.media.head.link == URL("https://www.facebook.com/davidjonesstore/photos/a.235128889834738.80370.233154863365474/1058075307540088/?type=1"))
      assert(wrappingPost.media.head.asInstanceOf[Photo].picture == URL("https://scontent.xx.fbcdn.net/hphotos-xpa1/v/t1.0-9/s130x130/11050286_1058075307540088_5038672955397666569_n.png?oh=ceb9032f9f0ff039a200cbf54a301cda&oe=55C1F8C5"))

      assert(wrappingPost.channel === PostChannels.FACEBOOK)
      assert(wrappingPost.url === URL("https://www.facebook.com/233154863365474/posts/1058075464206739"))
      assert(wrappingPost.creation_at.dayOfMonth.get === 9)
      assert(wrappingPost.creation_at.monthOfYear.get === 3)
      assert(wrappingPost.creation_at.getYear === 2015)

      assert(enclosedPosts.forall(post => post.channel == PostChannels.FACEBOOK))

      verifyLikes(likePosts, wrappingPost)
      verifyComments(commentPosts, wrappingPost)
    }
  }

  private def verifyLinkPostExtraction = {
    "Link Facebook Post" should "have " in {

      val jsonString = loadFile("facebookapi-link_post-sample.json")

      val postList = FacebookTransformer.generatePosts(parse(jsonString))

      assert(postList.size === 21)

      val wrappingPost = postList.head
      val enclosedPosts = postList.tail
      val likePosts = enclosedPosts.take(18)
      val commentPosts = enclosedPosts.drop(18)

      assert(!wrappingPost.is_engagement && !wrappingPost.is_reply && wrappingPost.is_parsed && !wrappingPost.is_empty)
      assert(wrappingPost.postType === PostTypes.LINK)
      assert(wrappingPost.channel === PostChannels.FACEBOOK)
      assert(wrappingPost.url === URL("https://www.facebook.com/577349558988559/posts/878059662250879"))

      assert(wrappingPost.mentions.size == 4)

      assert(wrappingPost.mentions.head.name == "Target Australia")
      assert(wrappingPost.mentions.head.identifierString == "196094017082910")

      assert(wrappingPost.mentions.tail.head.name == "Dannii Minogue")
      assert(wrappingPost.mentions.tail.head.identifierString == "8367908606")

      assert(wrappingPost.mentions.drop(2).head.name == "Engaging Women")
      assert(wrappingPost.mentions.drop(2).head.identifierString == "358007390992588")

      assert(wrappingPost.mentions.last.name == "Natalie Bassingthwaighte")
      assert(wrappingPost.mentions.last.identifierString == "61627456263")

      assert(wrappingPost.links.size == 1)
      assert(wrappingPost.links.head.name == "http://chikhi.co/press/post/nat-bass-on-the-nappy-collective")

      verifyLikes(likePosts,wrappingPost)
      verifyComments(commentPosts,wrappingPost)
    }
  }

  private def verifyLikes(likePosts: List[SocialInteraction], parentPost: SocialInteraction)= {
    verifyEnclosedPosts(likePosts,parentPost)

    assert(likePosts.forall(post => post.postType === PostTypes.LIKE))
    assert(likePosts.forall(post => post.rawData.isEmpty))
    assert(likePosts.forall(post => post.text.isEmpty))
    assert(likePosts.forall(post => post.is_engagement))

    assert(likePosts.forall(post => post.replyTo.nonEmpty && post.replyTo.get === parentPost))

    assert(likePosts.forall(post => post.conversation.nonEmpty && post.conversation === parentPost.conversation))

    //Unlike the comments, Likes come with a unique identifier, that is not linked to its parent post !!
    assert(likePosts.forall(post => post.source_id.identifiers.size === 1))
  }

  private def verifyComments(commentPosts: List[SocialInteraction], parentPost: SocialInteraction)= {
    verifyEnclosedPosts(commentPosts, parentPost)

    assert(commentPosts.forall(post => post.postType == PostTypes.COMMENT))
    assert(commentPosts.forall(post => post.rawData.nonEmpty))
    assert(commentPosts.forall(post => post.text.nonEmpty))
    assert(commentPosts.forall(post => !post.is_engagement))

    assert(commentPosts.forall(post => post.replyTo.nonEmpty && post.replyTo.get === parentPost))
    assert(commentPosts.forall(post => post.conversation.nonEmpty && post.conversation === parentPost.conversation))

    // the first id of a comment or a like, should be that of its parent, which comes second in the list of ID's of the
    // enclosing post
    assert(commentPosts.forall(post => post.source_id.identifiers.head === parentPost.source_id.identifiers.last))
  }

  private def verifyEnclosedPosts(enclosedPosts :List[SocialInteraction], parentPost: SocialInteraction)= {
    assert(enclosedPosts.forall(post => post.is_reply))
    assert(enclosedPosts.forall(post => post.is_parsed))
    assert(enclosedPosts.forall(post => !post.is_empty))
  }

  private def loadFile(fileName: String) = {
    val photo_post_file = Source fromURL getClass.getClassLoader.getResource(fileName)

    var jsonText = ""

    photo_post_file.getLines().foreach(currentLine => jsonText = jsonText + currentLine)
    jsonText
  }
}
