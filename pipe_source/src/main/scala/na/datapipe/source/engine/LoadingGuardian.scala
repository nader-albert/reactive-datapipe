package na.datapipe.source.engine

import java.lang.System.currentTimeMillis
import akka.actor.SupervisorStrategy.{Stop, Restart}
import akka.actor._
import com.typesafe.config.{ConfigException, Config}
import na.datapipe.model.{StopLoad, SourcesChannels, StartLoad}
import na.datapipe.source.engine.file.TextFileConnector
import na.datapipe.source.engine.hbc.HosebirdConnector
import na.datapipe.source.model._
import na.datapipe.transformer.model.TransformerRegistration

import scala.collection.immutable.HashMap
import scala.util.Random

/**
 * @author nader albert
 * @since  20/07/2015.
 */

class LoadingGuardian(loadingConfig :Config) extends Actor with ActorLogging {
  var t1 = 0.0
  var t2 = 0.0

  val source_channels = loadingConfig.getConfig("source-channels")
  var openDataSources = new HashMap[DataSource, ActorRef]

  override def supervisorStrategy = OneForOneStrategy(){
    case runtimeException :LoadRuntimeException =>
      log error "runtime exception received.. attempting to restart the associated file loader"

      context.children.foreach(_! ConnectToSource) //TODO: check if that's really correct or not
      Restart

    case interruptException :LoadInterruptedException =>
      log error "interrupt received...  attempting to stop the associated file loader .. !" + interruptException.getStackTrace
      Stop
  }

  override def receive: Receive = {

    case StartLoad(source) if source == SourcesChannels.FACEBOOK_FILE =>
      try {
        val sourceConfig = source_channels getConfig source.name

        val faceBookFileLoader = context.actorOf(TextFileConnector.props, "facebook_file_loader" + Random.nextInt(1000))

        log info "start load command for facebook file received !"

        t1 = currentTimeMillis

        faceBookFileLoader ! ConnectToSource(FileSource("facebook", sourceConfig getString "path", None))
      } catch {
          case _:ConfigException.Missing => log error "missing configuration !" //TODO: reply back with a failure message to the caller
      }

    case StartLoad(source) if source == SourcesChannels.TWITTER_API =>
      try {
        val sourceConfig = source_channels getConfig source.name

        val twitterLoader = context.actorOf(HosebirdConnector.props(sourceConfig),
          "twitter-loader" + Random.nextInt(100))

        println("start twitter load command received !")

        twitterLoader ! ConnectToSource(TwitterSource(source.name , None))

        log info "twitter loader connected successfully to source !"
      } catch {
        case _:ConfigException.Missing => //TODO: reply back with a failure message to the caller
      }

    /*case StartFileLoad(fileSourceName) =>
      try {
        val fileConfig = sourcesConfig getConfig "files" getConfig fileSourceName

        val fileLoader = context.actorOf(FileLoader.props(transformersConfig),
          "file-loader" + Random.nextInt(1000))

        println("starting the loading of the input file ........")

        t1 = currentTimeMillis

        fileLoader ! ConnectToSource(FileSource(fileSourceName, fileConfig.getString("path"), None))
      } catch {
          case _:ConfigException.Missing => println("missing configuration !")//TODO: reply back with a failure message to the caller
      }

    case StartTwitterLoad(twitterSourceName) =>
      try {
        val twitterConfig = sourcesConfig getConfig "twitters" getConfig twitterSourceName

        val twitterLoader = context.actorOf(HosebirdConnector.props(twitterConfig),
          "twitter-loader" + Random.nextInt(100))

        println("start twitter load command received !")

        twitterLoader ! ConnectToSource(TwitterSource(twitterSourceName, None))

        println("twitter loader connected successfully to source !")
      } catch {
          case _:ConfigException.Missing => //TODO: reply back with a failure message to the caller
      }*/

    case ConnectedToSource(source :DataSource) =>
      log info "starting data consumption from source:" + source.path

      openDataSources = openDataSources updated(source, sender)

      sender ! ConsumeFromSource(source)

    case FailedToConnect(source :DataSource) =>
      log error "Failed To connect to source with name : " + source.name + " and whose path is: " + source.path
      // TODO: reply to the sender of the StartFileLoad or StartTwitterLoad message

    case StopLoad(source) =>
      openDataSources find (_._1.path == source) map (openSource => {
        openSource._2 ! DisconnectFromSource(openSource._1) ; openDataSources = openDataSources - openSource._1 } )

    case StopLoad(source) =>
      openDataSources find (_._1.path == source) map (openSource => {
        openSource._2 ! DisconnectFromSource(openSource._1) ; openDataSources = openDataSources - openSource._1 } )

    case LoadComplete =>
      t2 = currentTimeMillis

      println("finished loading the file !" + "and the time taken was " + (t2 - t1) + " milliseconds")

      sender ! PoisonPill

    case TransformerRegistration => println("transformation registration received ! ")
      // spread the news, disseminate the update of having a new transformer coming into play.. basically all existing
      // transformers in the cluster will send this message to register themselves with a newly inserted loader.
      context.children.foreach( _ forward TransformerJoined) //keep the sender as is, don't change it... as it will be watched by the fine grained ElementLoader !
  }
}

object LoadingGuardian {
  def props(loadingConfig :Config) = Props(classOf[LoadingGuardian], loadingConfig)
}
