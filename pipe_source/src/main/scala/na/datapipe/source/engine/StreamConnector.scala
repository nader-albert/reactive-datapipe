package na.datapipe.source.engine

import akka.actor.{ActorLogging, Actor}
import na.datapipe.source.model._

/**
 * @author nader albert
 * @since  3/08/2015.
 */

trait StreamConnector extends Actor with ActorLogging{
  var connected = false

  override def postStop(){
    log warning "actor has stopped"
  }

  override def preRestart(reason: Throwable, message: Option[Any]){
    log warning "actor is restarting because of " + reason + "message " + message.getOrElse("")
  }

  override def postRestart(reason: Throwable) {
    log warning "actor has been restarted because of " + reason + "message "
    connected = true
  }

  override def receive: Receive = {
    case ConnectToSource(source) =>
      connect(source) map { src  =>
        connected = true
        context become connected(src)
        sender ! ConnectedToSource(src)
        src
      } getOrElse (sender ! FailedToConnect(source))
  }

  def connected(source: DataSource): Receive = {
    case ConsumeFromSource(source: DataSource) =>
      context.become(consuming(source))
      consume(source)
  }

  /**
   * at this stage the actor will just be listening to messages of type @DisconnectFromSource, which would stop the
   * loading process during its execution. the actor will be deaf to all other types of messages
   * */
  def consuming(source: DataSource): Receive = {

    case dscn :DisconnectFromSource => log warning "recived disconnect msg" ; disconnect(dscn.source)

    case LoadingInterrupted(exception) => throw new LoadInterruptedException("INTERRUPT SIGNAL !")

    case LoadingFailed(exception) => throw new LoadRuntimeException(exception)

    case TransformerJoined => context.children.foreach(_ forward TransformerJoined)

    case _ => log error "*****************************************************"
  }

  protected def connect(source: DataSource):Option[DataSource]

  protected def consume(source: DataSource)

  protected def disconnect(source: DataSource) = {
    log info f"disconnecting the loader from its source $source.path"
    connected = false
    source close
  }
}
