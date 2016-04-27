package na.datapipe.sink.model

import na.datapipe.model.Pill

/**
 * @author nader albert
 * @since  6/08/2015.
 */
trait Command{
  type Header
}

case class Channel(name :String)

case class Swallow(pill :Pill, channel :Channel) extends Command

