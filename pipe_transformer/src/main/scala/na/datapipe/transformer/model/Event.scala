package na.datapipe.transformer.model

/**
 * @author nader albert
 * @since  22/10/2015.
 */
trait Event

case class ElementTransformed(id :Long) extends Event
case object TransformerRegistration
