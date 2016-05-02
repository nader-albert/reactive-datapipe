package na.datapipe.transformer.model

import na.datapipe.model.{SocialPill, TextPill, Pill}

/**
 * @author nader albert
 * @since  22/10/2015.
 */
trait Command

/*trait TransformElement extends Command {
  val text :String
  val id :Int
}*/

//case class TransformTweet(text :String, id: Int) extends TransformElement

case object KillChildren extends Command
