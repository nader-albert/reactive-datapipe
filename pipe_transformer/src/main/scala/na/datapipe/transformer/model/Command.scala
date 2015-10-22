package na.datapipe.transformer.model

/**
 * @author nader albert
 * @since  22/10/2015.
 */
trait Command

case class TransformElement(text :String, id: Int) extends Command
case class TransformTweet(text :String, id: Int) extends Command

