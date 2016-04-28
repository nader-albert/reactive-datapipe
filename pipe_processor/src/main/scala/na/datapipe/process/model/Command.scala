package na.datapipe.process.model

import na.datapipe.model.SocialPill

/**
 * @author nader albert
 * @since  24/10/2015.
 */
trait Command

case class ProcessPill(pill :SocialPill) extends Command
