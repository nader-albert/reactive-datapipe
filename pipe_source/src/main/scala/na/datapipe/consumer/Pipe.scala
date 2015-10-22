package na.datapipe.consumer

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

import ExecutionContext.Implicits.global

/**
 * @author nader albert
 * @since  19/10/2015.
 */
trait Pipe {
  type A
  type B

  val input :A
  val output :B

  def filter :Pipe
  def transform[B](implicit evidence: A=>B) :Pipe
  def translate[B](implicit evidence: A=>B) :Pipe

  def materialize :Future[B]
  //def andThen
}

abstract class TextPipe(val input :String) extends Pipe {
  override type A = String
  override type B = String

  val output = ""
}

/*case class TextPipe(input :String) extends Pipe with Filter with Transformer with Translator with Materializer {
  override type A = String
}*/

object TextPipe {
  def apply(input: String) = new TextPipe(input) with Filter with Transformer with Translator with Materializer
}

object Pipe {
  def apply[A](content: A) = TextPipe
  //def apply[A,B](content: A, transform: A=>B): Pipe = Pipe(transform(content))
}

object TestApp extends App {
  /*val pipe1 = Pipe("Nader", transform) flow

  val pipe2 = Pipe("Noha", (ss :String) => ss + "loves Egypt!") flow

  pipe1 onComplete {
    case Success(value) => println(value content)
  }

  pipe2 onComplete {
    case Success(value) => println(value content)
  }

  println(pipe2)

  //val zz = transform.andThen(_ -> Pipe(3))

  //def transform2 (text:String) = text + "loves Australia!"

  def transform = (text:String) => text + "loves Australia!" */

  val textPipe1 = TextPipe("nader").filter.transform.translate.materialize

  val textPipe2 = TextPipe("noha").filter.transform.translate.materialize

  textPipe1 onComplete {
    case Success(result) => println(result)
  }

  textPipe2 onComplete {
    case Success(result) => println(result)
  }

}
