package na.datapipe.source.engine

import akka.actor.Props
import na.datapipe.model.{Commands, TextPill}
/**
 * @author nader albert
 * @since  4/08/2015.
 */
class TextPillLoader extends PillLoader {
  override val transformCommand = (tweetPill :TextPill, id :Int) => Commands.TRANSFORM(tweetPill, id)
}

object TextPillLoader {
  def props = Props(classOf[TextPillLoader])
}
