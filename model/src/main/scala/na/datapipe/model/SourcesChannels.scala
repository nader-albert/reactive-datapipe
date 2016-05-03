package na.datapipe.model

/**
 * @author nader albert
 * @since  28/04/16.
 */

object SourcesChannels {
  val TWITTER_FILE = Source(1,"twitter_file")
  val FACEBOOK_FILE = Source(2, "facebook_file")
  val TWITTER_API = Source(3, "twitter_api")
  val FACEBOOK_API = Source(4, "facebook_api")
  val GNIP_API = Source(5, "gnip_api")
  val DATASIFT_API = Source(6, "datasift_api")
}

case class StartLoad(source: Source)
case class StopLoad(source: Source)

case class Source(id: Int, name: String)
