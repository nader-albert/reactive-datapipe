package na.datapipe.transformer.model

import na.datapipe.sink.model.Pill

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

case class Transform(dataPill :Pill[String], id: Int) extends Command

case object KillChildren extends Command
