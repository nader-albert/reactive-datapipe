package na.datapipe.sink.model

/**
 * @author nader albert
 * @since  6/08/2015.
 */
trait Command{
  type Header
}

/*case class PublishRawPost(msg: AnyRef)
case class PublishProcessedPost(msg: AnyRef)
case class PublishEngagementPost(msg: AnyRef)

case class PublishToFireBase(msg: HTTPMessage)
case class PublishToNLP() */

case class Swallow(pill :Pill, channel :Channel) extends Command

