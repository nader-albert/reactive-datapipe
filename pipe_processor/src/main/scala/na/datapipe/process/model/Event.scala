package na.datapipe.process.model

/**
 * @author nader albert
 * @since  15/10/2015.
 */

trait Event

case object ProcessorJoined extends Event
case object ProcessorRegistration extends Event


