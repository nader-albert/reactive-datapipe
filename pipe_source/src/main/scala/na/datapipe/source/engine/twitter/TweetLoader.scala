package na.datapipe.source.engine.twitter

import akka.actor.Props
import na.datapipe.sink.model.Pill
import na.datapipe.source.engine.PillLoader
import na.datapipe.transformer.model.Transform
/**
 * @author nader albert
 * @since  4/08/2015.
 */
class TweetLoader(transformersHost: String, transformersPort: String) extends PillLoader {
  override val host = transformersHost
  override val port = transformersPort

  override val transformCommand = (tweet :String, id :Int) => Transform(Pill(tweet, Map()), id)
}

object TweetLoader {
  def props(transformersHost: String, transformersPort: String) = Props(classOf[TweetLoader],transformersHost,transformersPort)
}
