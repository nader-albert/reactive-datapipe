package na.datapipe.sink.model

/**
 * @author nader albert
 * @since  6/08/2015.
 */
trait Command{
  type Header
}

case class Swallow(pill :Pill, channel :Channel) extends Command

