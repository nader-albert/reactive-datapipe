package na.datapipe.processor.model

/**
 * @author nader albert
 * @since  15/10/2015.
 */

trait Event

case object ProcessorJoined extends Event
case object ProcessorRegistration extends Event


