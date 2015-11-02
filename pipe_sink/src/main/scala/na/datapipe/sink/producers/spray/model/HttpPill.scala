package na.datapipe.sink.producers.spray.model

import na.datapipe.model.Pill
import spray.http.{HttpMethods, HttpEntity, Uri, HttpMethod}

/**
 * @author nader albert
 * @since  23/10/2015.
 */
case class HttpPill(content :String, headers: Map[String, Any]) {

  //override type headers = Map[String, Any]
  //override type body = HttpEntity

  def headers(names: Set[String]): Map[String, Any] = headers filterKeys names

  def method = headers find (_._1 == "method") flatMap {
      case (_, "get") => Some(HttpMethods.GET)
      case (_, "update") => Some(HttpMethods.PUT)
      case (_, "add") => Some(HttpMethods.POST)
      case (_, "remove") => Some(HttpMethods.DELETE)
      case (_, "check") => Some(HttpMethods.HEAD)
  }

  def url = headers find (_._1 == "url") flatMap { uri => Some(Uri(uri._2.toString)) }

  def body = Some(HttpEntity(content.toString))
}
