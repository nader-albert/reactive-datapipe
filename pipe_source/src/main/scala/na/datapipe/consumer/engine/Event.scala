package na.datapipe.consumer.engine

/**
 * @author nader albert
 * @since  4/08/2015.
 */
trait Event

case class ConnectedToSource(source: DataSource) extends Event
case class FailedToConnect(source: DataSource) extends Event


case object LoadComplete extends Event
case object TransformerJoined extends Event


case class LineLoaded(id: Int) extends Event

case class LoadingInterrupted(exception: Throwable) extends Event
case class LoadingFailed(exception: Throwable) extends Event

class LoadInterruptedException(interruption: String) extends Throwable
class LoadRuntimeException(exception: Throwable) extends Throwable
