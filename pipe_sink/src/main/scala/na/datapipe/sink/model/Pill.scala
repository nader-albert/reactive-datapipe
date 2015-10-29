package na.datapipe.sink.model

/**
 * @author nader albert
 * @since  23/10/2015.
 */

/*trait DataContainer {
  val content :String
  val headers: Map[String, Any]
}*/

case class Pill[T] (content :T, headers: Option[Map[String, Any]], id: Int)
