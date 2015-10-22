package na.datapipe.source

import scala.concurrent.{ExecutionContext, Future}

import ExecutionContext.Implicits.global

/**
 * @author nader albert
 * @since  19/10/2015.
 */
trait Materializer {
  this :TextPipe =>

  def materialize :Future[B] = Future { this.input + " after execution"}

}
