package na.datapipe.sink.producers.camel.jms

import akka.actor.Props
import com.typesafe.config.Config

/**
 * @author nader albert
 * @since  6/08/2015.
 */
class PostSink(rabbitMQConfig :Config) extends JmsCamelSink(rabbitMQConfig) {
  override def channel = rabbitMQConfig getConfig "facebook-channel"
}

object PostSink {
  def props(publishingConfig :Config) = Props(classOf[PostSink], publishingConfig)
}
