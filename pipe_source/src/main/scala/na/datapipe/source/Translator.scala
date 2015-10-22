package na.datapipe.source

/**
 * @author nader albert
 * @since  19/10/2015.
 */
trait Translator{
  this :Pipe =>

  def translate[B] (implicit evidence: A=>B) :Pipe = ???
}
