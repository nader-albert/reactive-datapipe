package na.datapipe.sink.producers.camel

import akka.actor.Props
import com.typesafe.config.Config

/**
 * @author nader albert
 * @since  6/08/2015.
 */
class TweetSink(rabbitMQConfig :Config) extends CamelSink(rabbitMQConfig){
  override def channel = rabbitMQConfig getConfig "tweet-channel"
}

object TweetSink {
  def props(sinkConfig :Config) = Props(classOf[TweetSink], sinkConfig)
}