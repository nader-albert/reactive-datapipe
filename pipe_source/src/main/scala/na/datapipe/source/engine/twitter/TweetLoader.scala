package na.datapipe.source.engine.twitter

import akka.actor.Props
import na.datapipe.model.{Commands, TextPill, Pill}
import na.datapipe.source.engine.PillLoader
/**
 * @author nader albert
 * @since  4/08/2015.
 */
class TweetLoader extends PillLoader {
  override val transformCommand = (tweetPill :TextPill, id :Int) => Commands.TRANSFORM(tweetPill, id)
}

object TweetLoader {
  def props = Props(classOf[TweetLoader])
}
