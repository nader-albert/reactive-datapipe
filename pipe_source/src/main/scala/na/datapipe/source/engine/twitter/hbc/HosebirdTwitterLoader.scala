package na.datapipe.source.engine.twitter.hbc

import java.util
import java.util.concurrent.LinkedBlockingQueue

import akka.actor.Props
import au.datapipe.model.LocationDto
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.endpoint.{Location, StatusesFilterEndpoint}
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.twitter.hbc.core.{Client, Constants, HttpHosts}
import com.twitter.hbc.httpclient.auth.OAuth1
import com.typesafe.config.Config
import na.datapipe.model.Pill
import na.datapipe.source.engine.twitter.TweetLoader
import na.datapipe.source.engine.{StreamLoader, _}
import na.datapipe.source.model._
import spray.json.{JsString, JsValue, JsonReader}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.languageFeature.implicitConversions._
import scala.util.Random._
import scala.util.{Failure, Success}

/**
 * @author nader albert
 * @since  3/08/2015.
 */

class HosebirdTwitterLoader(twitterConfig :Config /*, transformersHost: String, transformersPort: String*/) extends StreamLoader {

  private var client: Client = null

  private val CLIENT_NAME = twitterConfig getConfig "client" getString "name"

  private val authenticationConfig = twitterConfig getConfig "authentication"

  private val filterConfig = twitterConfig getConfig "filters"

  private val twitterAuthentication = new OAuth1(
    authenticationConfig getString "oauth.consumer.key",
    authenticationConfig getString "oauth.consumer.secret",
    authenticationConfig getString "oauth.token.key",
    authenticationConfig getString "oauth.token.secret")

  private val host = new HttpHosts(Constants.STREAM_HOST)
  private val endpoint = new StatusesFilterEndpoint
  private val msgQueue = new LinkedBlockingQueue[String](100000) //TODO: we have to implement a smarter back-pressure mechanism here !

  /**
   * describes the mechanism of connecting to that specific source
   *  */
  def connect(source: DataSource) = {
    val twitterSource = source match {
      case _ :TwitterSource => Some(TwitterSource(source.name, None))
      case _ => None
    }

    twitterSource map {
      setFilters

      client = new ClientBuilder name CLIENT_NAME hosts host authentication
        twitterAuthentication endpoint endpoint processor new StringDelimitedProcessor(msgQueue) build

      connected = true

      client connect

      _ copy(connection = Some(client))
    }
  }

  /**
   * describes the mechanism of consuming from that specific source
   *  */
  def consume(source: DataSource) = {
    val tweetLoader = context.actorOf(TweetLoader props(/*transformersHost, transformersPort*/), "tweet-loader" + nextInt)

    Future {
      while (connected) {
        val seq = nextInt(10000)
        tweetLoader ! Load(Pill(msgQueue.take, Some(Map.empty[String, Any].updated("source", "twitter")),seq),seq)
      }
    } onComplete {
        case Success(numberOfLines) =>
          println("load completed successfully! " + numberOfLines)
          context.parent ! LoadComplete

        case Failure(exception :Throwable) =>
          exception match {
            case interrupt: LoadInterruptedException => println("interrupt signal received ! ")
              self ! LoadingInterrupted(interrupt)

            case t :RuntimeException => println("runtime exception "); self ! LoadingFailed(t)
          }
    }
  }

  private def setFilters {
    import na.datapipe.source.engine.twitter.hbc.TwitterFilter._
    import na.datapipe.source.engine.twitter.hbc.HosebirdTwitterLoader._

    val filter :TwitterFilter = JsString(filterConfig.getString("match"))

    if (filter.tracks.isEmpty || filter.tracks.head.trim.isEmpty) {
      throw new RuntimeException("Tracks filter field is required")
    }
    endpoint trackTerms filter.tracks

    endpoint locations filter.locations
  }
}

case class TwitterFilter(tracks :List[String], locations :List[LocationDto])

object HosebirdTwitterLoader {
  def props(twitterConfig :Config/*, transformersConfig :Config*/) = {
  //  val transformersHost = transformersConfig.getString("transformers_host")
 //   val transformersPort = transformersConfig.getString("transformers_port")

    Props(classOf[HosebirdTwitterLoader], twitterConfig/*, transformersHost, transformersPort*/)
  }

  import scala.languageFeature.implicitConversions._

  implicit def toJavaListOfString(scalaList :List[String]): java.util.List[String] = {
    val javaList = new util.ArrayList[String]()
    scalaList foreach(javaList add _)
    javaList
  }

  implicit def toJavaListOfLocation(scalaList :List[LocationDto]): java.util.List[Location] = {
    val javaList = new util.ArrayList[Location]()
    scalaList.map(locationDto => new Location(new Location.Coordinate(locationDto.getSwLongitude, locationDto.getSwLatitude),
      new Location.Coordinate(locationDto.getNeLongitude, locationDto.getNeLatitude))).foreach(loc => javaList.add(loc))

    javaList
  }
}

object TwitterFilter {
  import scala.languageFeature.implicitConversions

  implicit def toFilter(jsonText: JsString): TwitterFilter = JsonReader.func2Reader(TwitterFilter.reader).read(jsonText)

  def reader(json: JsValue): TwitterFilter = {
    new TwitterFilter(List("the"), Nil) //TODO: parse it correctly 
    //new TwitterFilter(json.asInstanceOf[JsString].tracks,json.asInstanceOf[JsString].locations)
  }
}
