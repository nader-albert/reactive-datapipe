package na.datapipe.process.model

import na.datapipe.model.TweetPill

/**
 * @author nader albert
 * @since  24/10/2015.
 */
trait Command

case class ProcessPill(pill :TweetPill) extends Command
