package na.datapipe.processor.model

import na.datapipe.model.{TextPill, TweetPill, Pill, Tweet}

/**
 * @author nader albert
 * @since  24/10/2015.
 */
trait Command

case class ProcessPill(pill :TextPill) extends Command
