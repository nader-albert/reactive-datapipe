package na.datapipe.sink.model

/**
 * @author nader albert
 * @since  23/10/2015.
 */
trait Event

case object SinkJoined extends Event
case object SinkRegistration extends Event

