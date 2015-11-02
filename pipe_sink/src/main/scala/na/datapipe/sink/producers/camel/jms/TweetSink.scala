package na.datapipe.sink.producers.camel.jms

import akka.actor.Props
import com.typesafe.config.Config

/**
 * @author nader albert
 * @since  6/08/2015.
 */
class TweetSink(rabbitMQConfig :Config) extends JmsCamelSink(rabbitMQConfig){
  override def channel = rabbitMQConfig getConfig "tweet-channel"
}

object TweetSink {
  def props(sinkConfig :Config) = Props(classOf[TweetSink], sinkConfig)
}