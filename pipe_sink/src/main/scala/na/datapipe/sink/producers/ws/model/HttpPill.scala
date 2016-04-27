package na.datapipe.sink.producers.ws.model

import na.datapipe.model.{Identifiable, TextPill, Pill}
import spray.http.{HttpMethods, HttpEntity, Uri}

/**
 * @author nader albert
 * @since  23/10/2015.
 */
case class HttpPill(body :String, httpHeaders: HttpHeaders, id: Int)
  extends Pill with Identifiable {

  type Content = String

  override var header:Option[Map[String, String]] = Some(httpHeaders.headers)

  def headers(names: Set[String]): Map[String, String] = httpHeaders.headers filterKeys names

  def httpMethod = httpHeaders.headers.find (_._1 == "method") flatMap {
      case (_, "find") => Some(HttpMethods.GET)
      case (_, "update") => Some(HttpMethods.PUT)
      case (_, "add") => Some(HttpMethods.POST)
      case (_, "remove") => Some(HttpMethods.DELETE)
      case (_, "check") => Some(HttpMethods.HEAD)
  }

  def httpUrl = httpHeaders.headers find (_._1 == "uri") flatMap { uri => Some(Uri(uri._2.toString)) }

  def httpBody = Some(HttpEntity(body.toString))
}

case class HttpHeaders(headers: Map[String, String])

object HttpHeaders {
  type verb = String

  //type FIND = "find"
  val ADD = "add"

  def apply(method: verb, uri: String):HttpHeaders = HttpHeaders(Map() updated("method", "find") updated("uri", uri))
}