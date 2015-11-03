package na.datapipe.spark

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import na.datapipe.model.Tweet
import na.datapipe.sink.model.{Channel, Swallow}
import na.datapipe.sink.producers.spray.model.{HttpHeaders, HttpPill}

import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import spray.http.{HttpResponse, StatusCodes}
import spray.json._

import scala.collection.immutable.{IndexedSeq, Iterable}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * @author nader albert
 * @since  17/09/2015.
 */
object SparkEngine {

  //Note that the below code will be executed only once, with the instantiation of thi singleton object

  val driverPort = 7777
  val driverHost = "127.0.0.1"
  val conf = new SparkConf(false) // skip loading external settings
    .setMaster("local[*]") // run locally with enough threads
    .setAppName("Spark Streaming with Scala and Akka") // name in Spark web UI
    .set("spark.logConf", "false")
    .set("spark.driver.port", s"$driverPort")
    .set("spark.driver.host", s"$driverHost")
    .set("spark.akka.logLifecycleEvents", "false")

  val ssc = new StreamingContext(conf, Seconds(30)) // a new RDD will be generated every 30 seconds...

  val actorName = "spark-pipe"

  val actorStream: ReceiverInputDStream[Tweet] = ssc.actorStream[Tweet](Props[SparkPipe], actorName)

  var sinks : Set[ActorRef] = Set.empty

  def addSink(sink :ActorRef) =
    sinks = sinks + sink

  def removeSink(sink :ActorRef) =
    sinks = sinks - sink

  def stop = actorStream stop

  def run = {

    // Configuration for a Spark application.
    // Used to set various Spark parameters as key-value pairs.


    // describe the computation on the input stream as a series of higher-level transformations

    //implicit def arrayToString (numbers: Array[Long]) : String = numbers.sum + ""

    //map(s => s.substring (s.indexOf("Sydney"), s.indexOf("Sydney") + 7 ) " " length)

    implicit val brandWriter = JsonWriter.func2Writer(Brand.writer)

    implicit val horseStatsWriter = JsonWriter.func2Writer(HorseStatistics.writer)

    import scala.concurrent.duration._
    val timeout = 20 seconds
    //val system = ActorSystem("SparkVirtualSystem")
    //val url = "akka.tcp://sinks@" + "127.0.0.1" + ":" + "3000" + "/user/sink"
    //val sink: ActorRef = Await.result(system.actorSelection(url).resolveOne(timeout), timeout)

    /*
  publisher ! PublishToFireBase(HTTPMessage(PUT,
    Uri("https://customer-mind.firebaseio.com/melbournecup/horses/mentions.json"),
    Some(
      "{ " + "\" cities \" : {" + "\"Sydney\":{ \"duration\": \"past_10_seconds\", \"number_of_mentions\" : \" \"} "

        + "," + "\"Melbourne\" : { \"duration\": \"past_10_seconds\", \"number_of_mentions\" : \" \" } "

        + "," + "\"Perth\" : { \"duration\": \"past_10_seconds\", \"number_of_mentions\" : \" \"} "

        + "," + "\"Brisbane\" : { \"duration\": \"past_10_seconds\", \"number_of_mentions\" : \" \" } "

        + "," + "\"Adelaide\" : { \"duration\": \"past_10_seconds\", \"number_of_mentions\" : \" \" } "

        + "," + "\"Hobart\" : { \"duration\": \"past_10_seconds\", \"number_of_mentions\" : \" \"} "

        +"}" + "}"
  )))*/

    actorStream
      .filter(tweet =>
      tweet.text.content.contains("Sydney")
        || tweet.text.content.contains("Brisbane")
        || tweet.text.content.contains("Hobart")
        || tweet.text.content.contains("Melbourne")
        || tweet.text.content.contains("Adelaide")
        || tweet.text.content.contains("Cairns")
        || tweet.text.content.contains("Perth")
        || tweet.text.content.contains("Alice Springs")
        || tweet.text.content.contains("New Castle")
        || tweet.text.content.contains("the"))
      .map(tweet =>
      if (tweet.text.content.contains("Sydney"))
        ("SYDNEY", 1)
      else if (tweet.text.content.contains("Brisbane"))
        ("BISBANE", 1)
      else if (tweet.text.content.contains("Hobart"))
        ("HOBART", 1)
      else if (tweet.text.content.contains("Melbourne"))
        ("MELBOURNE", 1)
      else if (tweet.text.content.contains("Adelaide"))
        ("ADELAIDE", 1)
      else if (tweet.text.content.contains("Cairns"))
        ("CAIRNS", 1)
      else if (tweet.text.content.contains("Perth"))
        ("PERTH", 1)
      else if (tweet.text.content.contains("Alice Springs"))
        ("ALICE SPRINGS", 1)
      else if (tweet.text.content.contains("New Castle"))
        ("NEW CASTLE", 1)
      else if (tweet.text.content.contains("the"))
        ("THE", 1)
      else
        ("ERROR", 1))
      .reduceByKey(_ + _) //accumulate the numbers of each class separately
      .foreachRDD {
      _.collect.foreach { horseStats =>
        implicit val actorAskTimeout = new Timeout(10 seconds)
        import scala.concurrent.ExecutionContext.Implicits.global

        //Assuming that we will send to only one Sink for now
        sinks.headOption.map {sink =>
          (sink ? Swallow(HttpPill("", HttpHeaders("find", "https://customer-mind.firebaseio.com/melbournecup/stats/live/"
            + horseStats._1 + ".json"), 1), Channel("http/firebase")))
            .collect {
            case response: HttpResponse if response.status == StatusCodes.OK =>
              if (response.entity.data.asString == "null") "0"
              else {
                println(response.entity.data.asString)
                response.entity.data.asString
              }
          } onComplete {
            case Success(previousCount) =>
              println("number is " + previousCount)

              sink ! Swallow(
                HttpPill(
                  (previousCount.toInt + horseStats._2).toString,
                  HttpHeaders("update", "https://customer-mind.firebaseio.com/melbournecup/stats/live/" + horseStats._1 + ".json"), 1),
                Channel("http/firebase"))

            case Failure(exception) => println("EXCEPTION OCCURRED WHILE TRYING TO GET VALUE FROM FIREBASE !" + exception)
          }
        }
      }
    }

    //.count() //should count the number of posts in each RDD... we should be receiving 1 new RDD every 10 seconds.
    //.reduce(_ + _) // the numbers of posts per RDD, will be accumulated here to give us the total number of posts across the entire period of time, during which the stream is on
    //val url = "akka.tcp://sparkDriver@127.0.0.1:7777/user/Supervisor0/processor"
    //val processor: ActorSelection = context.actorSelection("akka.tcp://sparkDriver@127.0.0.1:7777/user/Supervisor0/processor")
    //val processor: ActorRef = Await.result(system.actorSelection(url).resolveOne(timeout), timeout)

    ssc start

    ssc awaitTermination
  }
}

case class HorseStatistics(name: String, count: Int)

/*case class HorseStatistics(number_of_tweets: Horse, stats: Statistics)
case class Horse(name :String = "dummy", rank: Int = 10) //don't really know what rank could be !
case class Statistics(history :HistoricalStatistics, live :LiveStatistics)
case class HistoricalStatistics(dailyStats :List[DailyStats], weeklyStats :List[WeeklyStats])
case class LiveStatistics(minutesStats :List[TimeStats])
case class DailyStats(date :String, numbers :Int) //17:10:2015, 55
case class WeeklyStats(startDate :String, endDate: String, numbers :Int) //17:10:2015, 24:10:2015, 155
case class TimeStats(time :String, numbers :Int) //17:10, 12*/

object HorseStatistics {
  def writer(horseStats: HorseStatistics): JsValue = {

    //JsObject( (horseStats.name, JsString(horseStats.count.toString)) )

    JsNumber(horseStats.count)
    // JsObject(
    //   ("stats",
    //JsObject( ("snapshot@"+stats.formattedDate,

    //JsObject( ("number_of_tweets", JsString(stats.number_of_tweets.toString)) ) //)) //17/10/2015 : 12:00 PM
    //)
    //)

    /*JsObject(stats.formattedDate,JsObject(
            ("number", JsString(stats.number_of_tweets.toString)),
            ("time_stamp", JsString(stats.formattedDate.toString)),
            ("number_of_tweets", JsString("0")),
            ("top_words", JsString("0")))) */

    /*val liveMinutes: Iterable[JsObject] = for {
      minuteData <- horseStats.stats.live.minutesStats
    } yield JsObject((minuteData.time , JsString(minuteData.numbers.toString)))

    val indexedSequence: IndexedSeq[JsObject] = liveMinutes.flatMap(List(_))(collection.breakOut)

    val liveStats: List[JsObject] = indexedSequence.toList

    JsObject(
      ("HorseStatistics", JsObject(
        ("horse", JsObject ("horse", JsObject(("name", JsString("nader")), ("reputation", JsString("10"))))),

        ("stats",
          JsObject("history",
                  JsObject("dailyStats",
                      JsObject(
                          ("17:10:2015", JsObject(("name", JsString("")), ("count", JsString("22")))),
                          ("18:10:2015", JsObject(("name", JsString("")), ("count", JsString("26")))),
                          ("19:10:2015", JsObject(("name", JsString("")), ("count", JsString("30"))))))
                  ,

                  ("weeklyStats",
                      JsObject(
                        ("17:10:2015", JsObject(("name", JsString("")), ("count", JsString("22")))),
                        ("18:10:2015", JsObject(("name", JsString("")), ("count", JsString("26")))),
                        ("19:10:2015", JsObject(("name", JsString("")), ("count", JsString("30"))))))
                )

                )
              ,
              ("live",
                    JsObject(("minutesStats",
                      JsArray(
                    /*JsObject(("2:13", JsObject(("name", JsString("")), ("count", JsString("22"))))),
                    JsObject(("2:14", JsObject(("name", JsString("")), ("count", JsString("26")))))*/
                    liveStats)))
              )
            )
          )
      ))
    )*/
  }

    /**
     * Array based formatting
     */
  /* JsObject(
      ("HorseStatistics",
        JsArray(
          JsObject(("horse", JsObject(("name", JsString("nader")), ("reputation", JsString("10"))))),
          JsObject(
            ("stats",
              JsArray(
                JsObject(
                  ("history",
                    JsArray(
                      JsObject(
                        ("dailyStats",
                          JsArray(
                            JsObject(("17:10:2015", JsObject(("name", JsString("")), ("count", JsString("22"))))),

                            JsObject(("18:10:2015", JsObject(("name", JsString("")), ("count", JsString("26"))))),

                            JsObject(("19:10:2015", JsObject(("name", JsString("")), ("count", JsString("30")))))
                          )
                          )
                      ),

                      JsObject(
                        ("weeklyStats",
                          JsArray(
                            JsObject(("17:10:2015", JsObject(("name", JsString("")), ("count", JsString("22"))))),

                            JsObject(("18:10:2015", JsObject(("name", JsString("")), ("count", JsString("26"))))),

                            JsObject(("19:10:2015", JsObject(("name", JsString("")), ("count", JsString("30")))))
                          )
                          )
                      ))
                    )),

                JsObject(("live",
                  JsObject(("minutesStats",
                    JsArray(
                      /*JsObject(("2:13", JsObject(("name", JsString("")), ("count", JsString("22"))))),
                      JsObject(("2:14", JsObject(("name", JsString("")), ("count", JsString("26")))))*/
                      liveStats
                    )))
                  )
                )
              )
              )
          )
        )
        )
    )
  }*/

  /*
  * {
  "HorseStatistics" :
    {
      "Horse" : {
        "name": "ahmed" ,
        "reputation" : "gamed"
      }
    ,
    "Stats" : {
        "History": [
          {"dailyStats" :
            [
              { "17/10/2015" : { "date" : "17/10/2015" , "numbers" : "4" } },
              { "18/10/2015" : { "date" : "18/10/2015" , "numbers" : "5" } }
            ]
          },
          {"monthlyStat" :
            [
              { "october" : { "date" : "october" , "numbers" : "4" } },
              { "november" : { "date" : "november" , "numbers" : "5" } }
            ]
          }
        ],

        "Live" : [
          { "7:10" : { "name" : "7:10" , "numbers" : "4" } },
          { "7:11" : { "name" : "7:11" , "numbers" : "5" } },
          { "7:12" : { "name" : "7:12" , "numbers" : "6" } }
        ]
    }
  }
  }*/
}

case class Brand(tweets: Map[String, String], number_of_tweets_per_hashTag: Map[String, Int], number_of_tweets: Int,
                 top_words: Map[String, Int])

object Brand {
  def writer (brand: Brand): JsValue =
    JsObject(
      //Map.empty[String, JsValue]
        //updated ("tweets", {
        ("tweets", {
          //var array = JsArray.empty

          //brand.tweets.fold(("",""))((acc, currentValue) => acc.+:( JsObject(("tweet", JsString(_._1)), ("lang", JsString(_._2)))))

          //brand.tweets.keys.fold()

          //val ff: Map[String, String] = brand.tweets.flatMap{x => List(("0"))}

          val tweets: Iterable[JsObject] = for {
            tweet <- brand.tweets
          } yield JsObject((tweet._1 , JsString(tweet._2)))

          /*brand.tweets.foreach {
            tweet => {
              array = JsArray(JsObject(("tweet", JsString(tweet._1)), ("lang", JsString(tweet._2))))

              array.elements.+:( JsObject(("tweet", JsString(tweet._1)), ("lang", JsString(tweet._2))))
              //array
            }
          }
        array* }*/
          var pp: List[JsObject] = List[JsObject]()

          tweets.foreach {
            tweet => { pp = pp.::(tweet) }
          }

          val indexedSequence: IndexedSeq[JsObject] = tweets.flatMap(List(_))(collection.breakOut)

          indexedSequence.toList

          JsArray(pp)
        }),
      ("number_of_tweets_per_hashTag", JsString("0")),
      ("number_of_tweets", JsString("0")),
      ("top_words", JsString("0"))
    )
}

