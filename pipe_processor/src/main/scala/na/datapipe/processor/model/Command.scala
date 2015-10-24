package na.datapipe.processor.model

import na.datapipe.sink.model.Pill

/**
 * @author nader albert
 * @since  24/10/2015.
 */
trait Command

case class Process(pill :Pill)
