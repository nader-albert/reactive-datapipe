package na.datapipe.sink.producers.db

import na.datapipe.model.Pill
import scala.concurrent.Future

/**
 * @author nader albert
 * @since  27/04/16.
 */
trait PillDao [T] {
  def save(post: Pill): Future[T]
  def find(query: Map[String, AnyRef])
}
