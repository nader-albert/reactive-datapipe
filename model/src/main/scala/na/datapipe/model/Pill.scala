package na.datapipe.model

/**
 * @author nader albert
 * @since  23/10/2015.
 */

/*trait DataContainer {
  val content :String
  val headers: Map[String, Any]
}*/

trait Pill {
  type Content
  var header: Option[Map[String, Any]]
  val body: Content
}

/*trait Body {
  type value
}

case class TextBody(content: value) extends Body {
  type value = String
}

trait IntegerBody extends Body {
  type value = Integer
}*/

trait Identifiable {
  val id: Int
}

case class TextPill(body: String, var header: Option[Map[String, Any]], id: Int)
  extends Pill with Identifiable {

  type Content = String
}

case class TweetPill(body: Tweet, var header: Option[Map[String, Any]], id: Int)
  extends Pill with Identifiable {

  type Content = Tweet
}

//case class Pill[T] (content :T, headers: Option[Map[String, Any]], id: Int)