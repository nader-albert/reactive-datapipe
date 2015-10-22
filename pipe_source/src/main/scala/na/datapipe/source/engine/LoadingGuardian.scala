package na.datapipe.source.engine

import java.lang.System.currentTimeMillis
import akka.actor.SupervisorStrategy.{Stop, Restart}
import akka.actor._
import com.typesafe.config.Config
import na.datapipe.source.engine.twitter.TwitterLoader

import scala.collection.immutable.HashMap
import scala.util.Random

/**
 * @author nader albert
 * @since  20/07/2015.
 */

class LoadingGuardian(loadingConfig :Config) extends Actor{
  var t1 = 0.0
  var t2 = 0.0

  val sourcesConfig = loadingConfig.getConfig("sources")
  val transformersConfig = loadingConfig.getConfig("transformers")

  var openDataSources = new HashMap[DataSource, ActorRef]

  override def supervisorStrategy = OneForOneStrategy(){
    case runtimeException :LoadRuntimeException => {
      println("runtime exception received.. attempting to restart the associated file loader")

      context.children.foreach(_! ConnectToSource) //TODO: check if that's really correct or not
      Restart
    }

    case interruptException :LoadInterruptedException => {
      println("interrupt received...  attempting to stop the associated file loader .. !" + interruptException.getStackTrace)
      Stop
    }
  }

  override def receive: Receive = {
    case StartFileLoad(fileSourceName) =>

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

        val twitterLoader = context.actorOf(TwitterLoader.props(twitterConfig, transformersConfig),
          "twitter-loader" + Random.nextInt(100))

        println("start twitter load command received !")

        twitterLoader ! ConnectToSource(TwitterSource(twitterSourceName, None))

        println("twitter loader connected successfully to source !")
      } catch {
          case _:ConfigException.Missing => //TODO: reply back with a failure message to the caller
      }

    case ConnectedToSource(source :DataSource) =>
      println("starting data consumption from source:" + source.path) //TODO: turn this to logging

      openDataSources = openDataSources updated(source, sender)

      sender ! ConsumeFromSource(source)

    case FailedToConnect(source :DataSource) =>
      println("Failed To connect to :" )
      // TODO: reply to the sender of the StartFileLoad or StartTwitterLoad message

    case StopFileLoad(source) =>
      openDataSources find (_._1.path == source) map (openSource => {
        openSource._2 ! DisconnectFromSource(openSource._1) ; openDataSources = openDataSources - openSource._1 } )

    case StopTwitterLoad(source) =>
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
