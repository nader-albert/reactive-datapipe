package na.datapipe.model

/**
 * @author nader albert
 * @since  2/05/16.
 */

trait Command {
  val pill: Pill
  val sequence: Int
}

case class Load (pill :TextPill, sequence :Int) extends Command
case class Transform (override val pill :TextPill, sequence: Int) extends Command
case class Process (override val pill :Pill, sequence: Int) extends Command
case class Swallow (pill :Pill, sequence: Int) extends Command

object Commands extends Enumeration {
  val LoadCommand = 1
  val TransformCommand = 2
  val ProcessCommand = 3
  val SwallowCommand = 4

  def ? (command: Command) = command match {
    case ld: Load => LoadCommand
    case tm: Transform => TransformCommand
    case pr: Process => ProcessCommand
    case sw :Swallow => SwallowCommand
  }

  val LOAD = (pill: TextPill, sequence: Int) => Load(pill,sequence)
  val TRANSFORM = (pill: TextPill, sequence: Int) => Transform(pill, sequence)
  val PROCESS = (pill: Pill) => Process(pill, 0)
  val SWALLOW = (pill: Pill) => Swallow(pill, 0) // TODO: change this hard-coding
}





