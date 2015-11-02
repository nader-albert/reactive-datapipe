package na.datapipe.processor.model

import na.datapipe.model.{Pill, Tweet}

/**
 * @author nader albert
 * @since  24/10/2015.
 */
trait Command

case class ProcessPill[T](pill :Pill[T]) extends Command
