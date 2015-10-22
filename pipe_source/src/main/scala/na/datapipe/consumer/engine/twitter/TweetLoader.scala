package na.datapipe.consumer.engine.twitter

import akka.actor.Props
import au.com.pwc.socialmedia.transformer.TransformTweet
import na.datapipe.consumer.engine.ElementLoader

/**
 * @author nader albert
 * @since  4/08/2015.
 */
class TweetLoader(transformersHost: String, transformersPort: String) extends ElementLoader {
  override val host = transformersHost
  override val port = transformersPort

  override val transformCommand = (tweet :String, id :Int) => TransformTweet(tweet, id)
}

object TweetLoader {
  def props(transformersHost: String, transformersPort: String) = Props(classOf[TweetLoader],transformersHost,transformersPort)
}
