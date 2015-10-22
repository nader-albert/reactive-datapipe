package na.datapipe.consumer

/**
 * @author nader albert
 * @since  19/10/2015.
 */
trait Transformer {
  this: Pipe =>

  def transform[B](implicit evidence: A=>B) :Pipe = ???
}
