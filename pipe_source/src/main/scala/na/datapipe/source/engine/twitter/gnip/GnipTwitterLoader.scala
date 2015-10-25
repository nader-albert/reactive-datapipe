package na.datapipe.source.engine.twitter.gnip

import na.datapipe.source.engine.StreamLoader
import na.datapipe.source.model.DataSource

/**
 * @author nader albert
 * @since  25/10/2015.
 */
class GnipTwitterLoader extends StreamLoader {

  /**
   * describes the mechanism of connecting to that specific source
   * */
  override protected def connect(source: DataSource): Option[DataSource] = ???

  /**
   * describes the mechanism of consuming from that specific source
   *  */
  override protected def consume(source: DataSource): Unit = ???
}
