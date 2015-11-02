package na.datapipe.sink.producers.camel.jms

import akka.actor.Actor
import akka.camel.Producer
import com.typesafe.config.Config

/**
 * @author nader albert
 * @since  10/08/2015.
 */
abstract class JmsCamelSink(rabbitMQConfig :Config) extends Actor with Producer {

  val username = rabbitMQConfig getString "username"
  val password = rabbitMQConfig getString "password"
  val host = rabbitMQConfig getString "host"
  val port = rabbitMQConfig getString "port"

  val exchange = channel getString "exchange"
  val queue = channel getString "queue"
  val routingKey = channel getString "routingKey"

  override def endpointUri =
    f"rabbitmq://$host:$port/$exchange?username=$username&password=$password&autoDelete=false&queue=$queue&routingKey=$routingKey"

  def channel: Config
}
