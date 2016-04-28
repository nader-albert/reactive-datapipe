package na.datapipe.transformer.transform

import com.google.gson.JsonParser
import na.datapipe.model.social._
import org.scalatest.FlatSpec

import scala.util.Random

/**
 * @author nader albert
 * @since  20/11/2015.
 */
class TwitterTransformerTest extends FlatSpec with JsonParserTest {

  import na.datapipe.transformer.twitter.TwitterTransformer._

  verifyHashTagsExtraction
  verifyMentionsExtraction
  verifyLinksExtraction
  verifyMediaExtraction
  verifyTextExtraction
  verifyPosterExtraction
  verifyReplyToExtraction
  verifySocialPostExtraction

  private def verifyPosterExtraction = {
    "Poster" should "exist and must have name: \"Jessica\", followers_count: 660 and friends_count: 266, " +
      "and identifier as '2881098398'" in {

      val jsonText = "{ \"user\":{\"id\":2881098398,\"id_str\":\"2881098398\"," +
        "\"name\":\"jessica\"," +
        "\"screen_name\":\"MendesGuhlinsky\"," +
        "\"location\":\"shawn followed10.14.15|#matica\"," +
        "\"url\":\"https:\\/\\/twitter.com\\/shawnmendes\\/status\\/432020069116162048\"," +
        "\"description\":\"Just me, him and the moon\"," +
        "\"protected\":false," +
        "\"verified\":false," +
        "\"followers_count\":660," +
        "\"friends_count\":266," +
        "\"listed_count\":1," +
        "\"favourites_count\":6813," +
        "\"statuses_count\":8026," +
        "\"created_at\":\"Tue Oct 28 18:21:15 +0000 2014\",\"utc_offset\":-21600," +
        "\"time_zone\":\"Central Time (US & Canada)\"," +
        "\"geo_enabled\":true,\"lang\":\"en\"," +
        "\"contributors_enabled\":false," +
        "\"is_translator\":false," +
        "\"profile_background_color\":\"887799\"," +
        "\"profile_background_image_url\":\"http:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\"," +
        "\"profile_background_image_url_https\":\"https:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\"," +
        "\"profile_background_tile\":false,\"profile_link_color\":\"000000\"," +
        "\"profile_sidebar_border_color\":\"C0DEED\"," +
        "\"profile_sidebar_fill_color\":\"DDEEF6\"," +
        "\"profile_text_color\":\"333333\"," +
        "\"profile_use_background_image\":false," +
        "\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/667036406611472384\\/L0h3QBmq_normal.jpg\"," +
        "\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/667036406611472384\\/L0h3QBmq_normal.jpg\"," +
        "\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/2881098398\\/1447868847\"," +
        "\"default_profile\":false," +
        "\"default_profile_image\":false,\"following\":null," +
        "\"follow_request_sent\":null,\"notifications\":null} " +
        "}"

      /*
      val jsonParser = new JsonParser
      val posterObject = jsonParser.parse(jsonText).getAsJsonObject.getAsJsonObject("user")*/

      val poster = toPoster(extractJsonObjectFromText(jsonText, "user"))

      assert(poster.name === "jessica")
      assert(poster.identifier.toString === "2881098398")
      assert(poster.followersCount === 660)
      assert(poster.friendsCount === 266)
    }
  }

  private def verifyTextExtraction= {
    "Text" should "exist and must start with \"Which of the new.... : " in {
      val jsonText = "{ \"text\":\"Which of the new songs took longer to write? And why? Love you \\ud83d\\udc96 #RevisitedAtMidnight @ShawnMendes @Shawn_Access 36\" }"

      val jsonParser = new JsonParser
      val textObject = jsonParser.parse(jsonText).getAsJsonObject.get("text")

      assert(toTextString(textObject)
        startsWith  "Which of the new songs took longer to write?")
    }
  }

  private def verifyHashTagsExtraction= {
    "HashTags" should "be empty" in {
      val jsonText_Empty = " { \"entities\":{\"hashtags\":[],\"urls\":[],\"user_mentions\":[],\"symbols\":[]} }"

      /*
      val jsonParser = new JsonParser
      val jsonObject = jsonParser.parse(entities).getAsJsonObject.getAsJsonObject("entities")
      */
      assert(toHashTags(extractJsonObjectFromText(jsonText_Empty, "entities")).size === 0)
    }

    it should "have size 1 and its name is : \"RevisitedAtMidnight\" " in {
      val jsonText_Full = "{\"entities\"" +
        ":{\"hashtags\":[{\"text\":\"RevisitedAtMidnight\",\"indices\":[65,85]}]," +
        "\"urls\":[]," +
        "\"user_mentions\":" +
        "[" +
        "{\"screen_name\":\"ShawnMendes\",\"name\":\"Shawn Mendes\",\"id\":379408088,\"id_str\":\"379408088\",\"indices\":[86,98]}," +
        "{\"screen_name\":\"Shawn_Access\",\"name\":\"Shawn Access\",\"id\":2975496784,\"id_str\":\"2975496784\",\"indices\":[99,112]}" +
        "]," +
        "\"symbols\":[]} }"

      /*
      val jsonParser = new JsonParser
      val jsonObject = jsonParser.parse(entities).getAsJsonObject.getAsJsonObject("entities")
      */

      val hashTags = toHashTags(extractJsonObjectFromText(jsonText_Full, "entities"))

      assert(hashTags.size === 1)
      assert(hashTags.head.name === "RevisitedAtMidnight")
    }
  }

  private def verifyMediaExtraction= {
    "Media List" should "have only one item, of type video, and its URL should be: " +
      "\"http://pbs.twimg.com/ext_tw_video_thumb/667209345520668672/pu/img/t6e2pXi8a4dGFGGh.jpg \" " in {

      val jsonText = "{ \"extended_entities\":{\"media\":[{\"id\":667209345520668672,\"id_str\":\"667209345520668672\"," +
        "\"indices\":[75,98]," +
        "\"media_url\":\"http:\\/\\/pbs.twimg.com\\/ext_tw_video_thumb\\/667209345520668672\\/pu\\/img\\/t6e2pXi8a4dGFGGh.jpg\"," +
        "\"media_url_https\":\"https:\\/\\/pbs.twimg.com\\/ext_tw_video_thumb\\/667209345520668672\\/pu\\/img\\/t6e2pXi8a4dGFGGh.jpg\"," +
        "\"url\":\"https:\\/\\/t.co\\/G7WEKVJDp7\",\"display_url\":\"pic.twitter.com\\/G7WEKVJDp7\"," +
        "\"expanded_url\":\"http:\\/\\/twitter.com\\/alexbootysta\\/status\\/667209554157895680\\/video\\/1\",\"type\":\"video\"," +
        "\"sizes\":{\"small\":{\"w\":340,\"h\":604,\"resize\":\"fit\"},\"large\":{\"w\":720,\"h\":1280,\"resize\":\"fit\"}," +
        "\"thumb\":{\"w\":150,\"h\":150,\"resize\":\"crop\"},\"medium\":{\"w\":600,\"h\":1067,\"resize\":\"fit\"}}," +
        "\"source_status_id\":667209554157895680,\"source_status_id_str\":\"667209554157895680\",\"source_user_id\":2561033286," +
        "\"source_user_id_str\":\"2561033286\",\"video_info\":{\"aspect_ratio\":[9,16]," +
        "\"duration_millis\":20007,\"variants\":[{\"bitrate\":320000,\"content_type\":\"video\\/mp4\"," +
        "\"url\":\"https:\\/\\/video.twimg.com\\/ext_tw_video\\/667209345520668672\\/pu\\/vid\\/180x320\\/k41tQjVnLtpKWzl2.mp4\"}," +
        "{\"bitrate\":832000,\"content_type\":\"video\\/webm\",\"url\":\"https:\\/\\/video.twimg.com\\/ext_tw_video\\/667209345520668672\\/pu\\/vid\\/360x640\\/fjyC7yWSu2NC070n.webm\"}," +
        "{\"content_type\":\"application\\/dash+xml\",\"url\":\"https:\\/\\/video.twimg.com\\/ext_tw_video\\/667209345520668672\\/pu\\/pl\\/jW4nLZBlYGXv8Y6p.mpd\"}," +
        "{\"content_type\":\"application\\/x-mpegURL\",\"url\":\"https:\\/\\/video.twimg.com\\/ext_tw_video\\/667209345520668672\\/pu\\/pl\\/jW4nLZBlYGXv8Y6p.m3u8\"}," +
        "{\"bitrate\":2176000,\"content_type\":\"video\\/mp4\",\"url\":\"https:\\/\\/video.twimg.com\\/ext_tw_video\\/667209345520668672\\/pu\\/vid\\/720x1280\\/56QtHN4uqtrZffDy.mp4\"}," +
        "{\"bitrate\":832000,\"content_type\":\"video\\/mp4\",\"url\":\"https:\\/\\/video.twimg.com\\/ext_tw_video\\/667209345520668672\\/pu\\/vid\\/360x640\\/fjyC7yWSu2NC070n.mp4\"}]}}]}" +
        "}"

      /*
      val jsonParser = new JsonParser
      val mediaObject = jsonParser.parse(jsonText).getAsJsonObject.getAsJsonObject("extended_entities").getAsJsonArray("media")
      */
      val mediaList = toMedia(extractJsonObjectFromText(jsonText, "extended_entities").getAsJsonArray("media"))

      assert(mediaList.size === 1)
      assert(mediaList.head.mediaType === MediaTypes.VIDEO)
      assert(mediaList.head.link.name === "http://pbs.twimg.com/ext_tw_video_thumb/667209345520668672/pu/img/t6e2pXi8a4dGFGGh.jpg")
    }
  }

  private def verifyMentionsExtraction= {
    "User Mentions" should "have  2 mentions, whose names are: \"Shawn Mendes\" and \"\"Shawn Access\" " in {
      val jsonText = "{\"entities\"" +
        ":{\"hashtags\":[{\"text\":\"RevisitedAtMidnight\",\"indices\":[65,85]}]," +
        "\"urls\":[]," +
        "\"user_mentions\":" +
        "[" +
        "{\"screen_name\":\"ShawnMendes\",\"name\":\"Shawn Mendes\",\"id\":379408088,\"id_str\":\"379408088\",\"indices\":[86,98]}," +
        "{\"screen_name\":\"Shawn_Access\",\"name\":\"Shawn Access\",\"id\":2975496784,\"id_str\":\"2975496784\",\"indices\":[99,112]}" +
        "]," +
        "\"symbols\":[]} }"

      /* val jsonParser = new JsonParser
      val entitiesObject = jsonParser.parse(jsonText).getAsJsonObject.getAsJsonObject("entities")
      */

      extractJsonObjectFromText(jsonText, "entities")

      val mentions = toMentions(extractJsonObjectFromText(jsonText, "entities"))

      assert(mentions.size === 2)
      assert(mentions.head.name === "Shawn Access")
      assert(mentions.last.name === "Shawn Mendes")
    }

    it should "have no mentions" in {
      val jsonText = "{\"entities\"" +
        ":{\"hashtags\":[{\"text\":\"RevisitedAtMidnight\",\"indices\":[65,85]}]," +
        "\"urls\":[]," +
        "\"user_mentions\":" + "[" + "],"+
        "\"symbols\":[]} " +
        "}"
      /* val jsonParser = new JsonParser
      val entitiesObject = jsonParser.parse(jsonText).getAsJsonObject.getAsJsonObject("entities") */

      assert(toMentions(extractJsonObjectFromText(jsonText, "entities")) isEmpty)
    }
  }

  private def verifyLinksExtraction = {
    "Urls" should "be of size 2, with URL's: https://t.co/qZjMdWPSPl and https://t.co/pspeBq9Psa" in {
      val jsonText = "{\"entities\"" +
        ":{\"hashtags\":[{\"text\":\"RevisitedAtMidnight\",\"indices\":[65,85]}]," +
        "\"urls\":[" +
        "{\"url\":\"https:\\/\\/t.co\\/pspeBq9Psa\",\"expanded_url\":\"http:\\/\\/2015mama.com\",\"display_url\":\"2015mama.com\",\"indices\":[102,125]}," +
        "{\"url\":\"https:\\/\\/t.co\\/qZjMdWPSPl\",\"expanded_url\":\"https:\\/\\/amp.twimg.com\\/v\\/009998de-1c78-4555-b848-ce90e8be0753\",\"display_url\":\"amp.twimg.com\\/v\\/009998de-1c7\\u2026\",\"indices\":[126,140]}]," +
        "\"user_mentions\":" +
        "[" +
        "{\"screen_name\":\"ShawnMendes\",\"name\":\"Shawn Mendes\",\"id\":379408088,\"id_str\":\"379408088\",\"indices\":[86,98]}," +
        "{\"screen_name\":\"Shawn_Access\",\"name\":\"Shawn Access\",\"id\":2975496784,\"id_str\":\"2975496784\",\"indices\":[99,112]}" +
        "]," +
        "\"symbols\":[]} }"

      /*val jsonParser = new JsonParser
      val entitiesObject = jsonParser.parse(jsonText).getAsJsonObject.getAsJsonObject("entities") */

      val links = toLinks(extractJsonObjectFromText(jsonText, "entities"))

      assert(links.size === 2)
      assert(links.head.name === "https://t.co/qZjMdWPSPl")
      assert(links.last.name === "https://t.co/pspeBq9Psa")
    }

    it should "be empty" in {
      val jsonText = "{\"entities\"" +
        ":{\"hashtags\":[{\"text\":\"RevisitedAtMidnight\",\"indices\":[65,85]}]," +
        "\"urls\":[" + "]," +
        "\"user_mentions\":" +
        "[" +
        "{\"screen_name\":\"ShawnMendes\",\"name\":\"Shawn Mendes\",\"id\":379408088,\"id_str\":\"379408088\",\"indices\":[86,98]}," +
        "{\"screen_name\":\"Shawn_Access\",\"name\":\"Shawn Access\",\"id\":2975496784,\"id_str\":\"2975496784\",\"indices\":[99,112]}" +
        "]," +
        "\"symbols\":[]} }"

      /*val jsonParser = new JsonParser
      val entitiesObject = jsonParser.parse(jsonText).getAsJsonObject.getAsJsonObject("entities") */


      val links = toLinks(extractJsonObjectFromText(jsonText, "entities"))

      assert(links.size === 0)
    }
  }

  private def verifyReplyToExtraction = {
    "Reply To" should "be " in {

      val jsonText = " { \"retweeted_status\"" +
        ":{\"created_at\":\"Thu Nov 19 02:45:11 +0000 2015\"," +
        "\"id\":667171734118797312," +
        "\"id_str\":\"667171734118797312\"," +
        "\"text\":\"Tonight we are back at it again. carpool karaoke with @jkcorden on the @latelateshow #littleboxbigboxcardboardbox https:\\/\\/t.co\\/L2xt0p6bXB\"," +
        "\"source\":\"\\u003ca href=\\\"http:\\/\\/twitter.com\\/download\\/iphone\\\" rel=\\\"nofollow\\\"\\u003eTwitter for iPhone\\u003c\\/a\\u003e\"," +
        "\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null," +
        "\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":27260086,\"id_str\":\"27260086\"," +
        "\"name\":\"Justin Bieber\",\"screen_name\":\"justinbieber\",\"location\":null,\"url\":\"http:\\/\\/smarturl.it\\/JBPurpose\"," +
        "\"description\":\"Let's make the world better. Join @officialfahlo and add me on @shots 'justinbieber'. OUR new single SORRY out now. OUR new album PURPOSE out NOW\",\"protected\":false," +
        "\"verified\":true,\"followers_count\":69860706,\"friends_count\":243594,\"listed_count\":626882," +
        "\"favourites_count\":2511,\"statuses_count\":30232,\"created_at\":\"Sat Mar 28 16:41:22 +0000 2009\"," +
        "\"utc_offset\":-18000,\"time_zone\":\"Eastern Time (US & Canada)\",\"geo_enabled\":false,\"lang\":\"en\"," +
        "\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"FFFFFF\"," +
        "\"profile_background_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_background_images\\/460851381025267712\\/RU-xit8T.png\"," +
        "\"profile_background_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_background_images\\/460851381025267712\\/RU-xit8T.png\"," +
        "\"profile_background_tile\":false,\"profile_link_color\":\"89C9FA\",\"profile_sidebar_border_color\":\"FFFFFF\"," +
        "\"profile_sidebar_fill_color\":\"C0DFEC\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":false," +
        "\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/652596362073272320\\/Zv6K-clv_normal.jpg\"," +
        "\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/652596362073272320\\/Zv6K-clv_normal.jpg\"," +
        "\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/27260086\\/1447387637\",\"default_profile\":false," +
        "\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null," +
        "\"coordinates\":null,\"place\":null,\"contributors\":null, \"is_quote_status\":false,\"retweet_count\":26,\"favorite_count\":27," +
        "\"entities\":" +
          "{\"hashtags\":[{\"text\":\"Deserving\",\"indices\":[119,129]}]," +
          "\"urls\":[" +
              "{\"url\":\"https:\\/\\/t.co\\/R5BFUdrW9p\",\"expanded_url\":\"http:\\/\\/biletnikoffaward.com\\/fan-vote\",\"display_url\":\"biletnikoffaward.com\\/fan-vote\",\"indices\":[57,80]}]," +
          "\"user_mentions\":[" +
            "{\"screen_name\":\"SuccessfulQuon\",\"name\":\"Laquon Treadwell\",\"id\":234905409,\"id_str\":\"234905409\",\"indices\":[33,48]}]" +
          ",\"symbols\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"filter_level\":\"low\",\"lang\":\"en\"} " +
        "}"

      /*val jsonParser = new JsonParser
      val entitiesObject = jsonParser.parse(jsonText).getAsJsonObject.getAsJsonObject("retweeted_status") */

      implicit val conversation_link = Conversation(Random.nextLong)

      val replyTo = toReply(extractJsonObjectFromText(jsonText, "retweeted_status"))

      assert(replyTo isDefined)
      val parentPost: SocialInteraction = replyTo.get

      assert(parentPost.channel === PostChannels.TWITTER)
      assert(parentPost.lang === "en")
      assert(parentPost.creation_at.dayOfMonth.get === 19)
      assert(parentPost.creation_at.monthOfYear.get === 11)
      assert(parentPost.creation_at.getYear === 2015)
      assert(parentPost.postType === PostTypes.TWEET)

      assert(parentPost.poster === Some(Poster(27260086,"Justin Bieber",PosterCategories.EMPTY, 69860706,243594)))
      assert(parentPost.links.size === 1)
      assert(parentPost.hashTags.size === 1)
      assert(parentPost.mentions.size === 1)
    }

    it should "be None" in {
      implicit val conversation_link = Conversation(Random.nextLong)

      val replyTo = toReply(null)
      assert(!replyTo.isDefined)
    }
  }

  private def verifySocialPostExtraction= {
    "Social Post" should "have 2 mentions, 1 hash tag, 1 link and a ReplyTo " in {
      val jsonText = "{\"created_at\":\"Thu Nov 19 03:34:38 +0000 2015\"," +
        "\"id\":667184178849406977,\"id_str\":\"667184178849406977\"," +
        "\"text\":\"RT @TNADixie: Please take a moment to vote for @SuccessfulQuon for the https:\\/\\/t.co\\/R5BFUdrW9p. Amazing season for Laquon Treadwell. #Deser\\u2026\"," +
        "\"source\":\"\\u003ca href=\\\"http:\\/\\/twitter.com\\/download\\/iphone\\\" rel=\\\"nofollow\\\"\\u003eTwitter for iPhone\\u003c\\/a\\u003e\"," +
        "\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null," +
        "\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":425938523,\"id_str\":\"425938523\"," +
        "\"name\":\"Adam Woods\",\"screen_name\":\"swoljerwoods\",\"location\":null,\"url\":null,\"description\":\"Awesome... Nuff said\"," +
        "\"protected\":false,\"verified\":false,\"followers_count\":105,\"friends_count\":133,\"listed_count\":1," +
        "\"favourites_count\":398,\"statuses_count\":1251,\"created_at\":\"Thu Dec 01 16:43:57 +0000 2011\",\"utc_offset\":null," +
        "\"time_zone\":null,\"geo_enabled\":false,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false," +
        "\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\"," +
        "\"profile_background_image_url_https\":\"https:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_tile\":false," +
        "\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\"," +
        "\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/584740930680303616\\/T1QdRHxY_normal.jpg\"," +
        "\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/584740930680303616\\/T1QdRHxY_normal.jpg\"," +
        "\"default_profile\":true,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null}," +
        "\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null," +
          "\"retweeted_status\":{\"created_at\":\"Thu Nov 19 02:38:17 +0000 2015\",\"id\":667169997375021056," +
          "\"id_str\":\"667169997375021056\",\"text\":\"Please take a moment to vote for @SuccessfulQuon for the https:\\/\\/t.co\\/R5BFUdrW9p. Amazing season for Laquon Treadwell. #Deserving\"," +
          "\"source\":\"\\u003ca href=\\\"http:\\/\\/www.echofon.com\\/\\\" rel=\\\"nofollow\\\"\\u003eEchofon\\u003c\\/a\\u003e\"," +
          "\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null," +
          "\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":84368747,\"id_str\":\"84368747\"," +
          "\"name\":\"Dixie Carter\",\"screen_name\":\"TNADixie\",\"location\":\"Nashville\",\"url\":\"http:\\/\\/www.impactwrestling.com\"," +
          "\"description\":\"President of TNA Wrestling. Watch @IMPACTWRESTLING on @DestAmerica at 9 e\\/p. Proud Ole Miss Rebel...\"," +
          "\"protected\":false,\"verified\":true,\"followers_count\":342391,\"friends_count\":92,\"listed_count\":3215," +
          "\"favourites_count\":230,\"statuses_count\":9146,\"created_at\":\"Thu Oct 22 16:51:50 +0000 2009\",\"utc_offset\":-21600," +
          "\"time_zone\":\"Central Time (US & Canada)\",\"geo_enabled\":false,\"lang\":\"en\",\"contributors_enabled\":false," +
          "\"is_translator\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_background_images\\/360131928\\/dixietwitter4.jpg\",\"profile_background_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_background_images\\/360131928\\/dixietwitter4.jpg\"," +
          "\"profile_background_tile\":false,\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/500052707265482753\\/PdSjNBgP_normal.jpeg\"," +
          "\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/500052707265482753\\/PdSjNBgP_normal.jpeg\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/84368747\\/1421782960\"," +
          "\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null," +
        "\"is_quote_status\":false,\"retweet_count\":26,\"favorite_count\":27," +
        "\"entities\":{" +
          "\"hashtags\":[{\"text\":\"Deserving\",\"indices\":[119,129]}]," +
          "\"urls\":[{\"url\":\"https:\\/\\/t.co\\/R5BFUdrW9p\"," + "\"expanded_url\":\"http:\\/\\/biletnikoffaward.com\\/fan-vote\",\"display_url\":\"biletnikoffaward.com\\/fan-vote\",\"indices\":[57,80]}]," +
          "\"user_mentions\":[{\"screen_name\":\"SuccessfulQuon\",\"name\":\"Laquon Treadwell\",\"id\":234905409,\"id_str\":\"234905409\",\"indices\":[33,48]}]," +
          "\"symbols\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"filter_level\":\"low\"," +
          "\"lang\":\"en\"}," +
        "\"is_quote_status\":false,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[{\"text\":\"Deserving\",\"indices\":[133,140]}],\"urls\":[{\"url\":\"https:\\/\\/t.co\\/R5BFUdrW9p\"," +
        "\"expanded_url\":\"http:\\/\\/biletnikoffaward.com\\/fan-vote\",\"display_url\":\"biletnikoffaward.com\\/fan-vote\",\"indices\":[71,94]}],\"user_mentions\":[{\"screen_name\":\"TNADixie\",\"name\":\"Dixie Carter\",\"id\":84368747," +
        "\"id_str\":\"84368747\",\"indices\":[3,12]},{\"screen_name\":\"SuccessfulQuon\",\"name\":\"Laquon Treadwell\",\"id\":234905409,\"id_str\":\"234905409\",\"indices\":[47,62]}],\"symbols\":[]},\"favorited\":false," +
        "\"retweeted\":false,\"possibly_sensitive\":false,\"filter_level\":\"low\",\"lang\":\"en\",\"timestamp_ms\":\"1447904078577\"}"

      val post: SocialInteraction = toPost(jsonText,parse(jsonText))

      assert(post.source_id.identifiers.head === "667184178849406977".toLong)
      assert(post.source_id.identifierString === "667184178849406977")

      assert(post.channel === PostChannels.TWITTER)
      assert(post.creation_at.dayOfMonth.get === 19)
      assert(post.creation_at.monthOfYear.get === 11)
      assert(post.creation_at.getYear === 2015)
      assert(post.creation_at.hourOfDay.get === 14) //03 in GMT + 11:00 (difference between GMT and Australia time in DayLight savings

      assert(post.lang === "en")
      assert(post.postType === PostTypes.RE_TWEET)
      assert(post.replyTo.isDefined)
      assert(post.is_reply)
      assert(post.poster === Some(Poster(425938523,"Adam Woods",PosterCategories.EMPTY, 105,133)))
      assert(post.links.size === 1)
      assert(post.hashTags.size === 1)
      assert(post.mentions.size === 2)
      assert(post.media.isEmpty)

      assert(post.replyTo isDefined)
      val parentPost: SocialInteraction = post.replyTo.get

      assert(parentPost.lang === "en")
      assert(parentPost.postType === PostTypes.TWEET)
      assert(parentPost.channel === PostChannels.TWITTER)
      assert(parentPost.creation_at.dayOfMonth.get === 19)
      assert(parentPost.creation_at.monthOfYear.get === 11)
      assert(parentPost.creation_at.getYear === 2015)
      assert(parentPost.creation_at.hourOfDay.get === 13) //02 in GMT + 11:00 (difference between GMT and Australia time in DayLight savings

      assert(parentPost.text.startsWith("Please take a moment to"))
      assert(parentPost.poster === Some(Poster(84368747,"Dixie Carter",PosterCategories.EMPTY, 342391,92)))
      assert(parentPost.links.size === 1)
      assert(parentPost.hashTags.size === 1)
      assert(parentPost.mentions.size === 1)

      assert(parentPost.conversation.get === post.conversation.get)
    }

    //TODO: Test the case where we have more than two nested tweets
  }
}