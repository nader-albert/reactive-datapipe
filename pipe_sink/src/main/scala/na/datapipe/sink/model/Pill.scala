package na.datapipe.sink.model

/**
 * @author nader albert
 * @since  23/10/2015.
 */

//http method and uri should both go into the header
trait Pill {
  type headers
  type body
}
