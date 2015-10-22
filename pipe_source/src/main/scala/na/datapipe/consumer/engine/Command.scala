package na.datapipe.consumer.engine

/**
 * @author nader albert
 * @since  4/08/2015.
 */

/**
 * A Command message is a message that expects something to be done. It may or may not expect a response.
 */
trait Command

/**
 * An Event message is something that has just happened. You are notifying of the event that has just happened.
 * */

case class ConnectToSource(source: DataSource) extends Command
case class ConsumeFromSource(source: DataSource) extends Command
case class DisconnectFromSource(source: DataSource) extends Command

case class StartFileLoad(fileSourceName :String/*filePath: String*/) extends Command

case class StopFileLoad(fileSourceName :String /*filePath: String*/) extends Command

case class StartTwitterLoad(twitterSourceName :String) extends Command
case class StopTwitterLoad(twitterSourceName :String) extends Command