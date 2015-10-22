package na.datapipe.source.engine

import scala.io.BufferedSource

/**
 * @author nader albert
 * @since  5/08/2015.
 */
trait DataSource {
  val name :String
  val path :String
  //def open :Any
  def close
  //def getOpenedSource
}

case class FileSource(name :String, path :String, connection :Option[BufferedSource]) extends DataSource {
  //override val name = "file" //TODO: try to get the source file path here, would be useful
  //override def open: BufferedSource = Source.fromURL(getClass.getClassLoader.getResource(filePath))
  override def close = connection foreach(_ close)

}

case class TwitterSource(name :String, connection :Option[com.twitter.hbc.core.Client]) extends DataSource {
  override val path = name
  override def close = connection foreach (_ stop)
}
