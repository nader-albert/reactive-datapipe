package na.datapipe.model

/**
 * @author nader albert
 * @since  1/10/2015.
 */
case class GeographicalLocation(longitude: Long, latitude: Long)
case class User(location: String, description: String, followersCount: Int, friendsCount: Int)
case class Text(content: String, lang: String)
case class Tweet(id: Long, user: User, geography: Option[GeographicalLocation], text: Text, reTweeted: Boolean,
                 reTweetOf: Option[Tweet], hashTags: List[String]) //retweet will be a some of Tweet, only if the current tweet is a retweet
