package na.datapipe.sink.initiator

import akka.actor.ActorSystem
import akka.camel.CamelExtension
import com.typesafe.config.{Config, ConfigFactory}
import na.datapipe.sink.SinkGuardian

/**
 * @author nader albert
 * @since  6/08/2015.
 */
object SinkEngine extends App {
  implicit val system = ActorSystem("sinks")

  val config = ConfigFactory load

  val appConfig: Config = config getConfig "sink_app"

  //val ctx: ApplicationContext = new ClassPathXmlApplicationContext("applicationContext-rabbitMQ.xml")

  //val session: SemantriaSession = ctx.getBean("semantriaSession").asInstanceOf[SemantriaSession]

  val camel = CamelExtension(system)

  implicit val camelContext = camel context

  //Initializer.initialize(camelContext)
  //val p: Post = new Post

  //camelContext.setLazyLoadTypeConverters(true)

  //val producerTemplate = camel template

  //val engBean: RabbitTemplate = ctx.getBean("engagementQueue", classOf[RabbitTemplate])

  val sink = system.actorOf(SinkGuardian.props(appConfig), name = "sink")

  /*publisher ! PublishToFireBase(
    HTTPMessage(PUT,
    Uri("https://customer-mind.firebaseio.com/melbournecup/horses/users.json"),
    Some("{ \"alanisawesome\": { \"name\": \"Alan Turing\", \"birthday\": \"June 23, 1912\" } }")))*/
}

