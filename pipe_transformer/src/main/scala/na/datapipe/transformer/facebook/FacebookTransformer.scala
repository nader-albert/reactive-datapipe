package na.datapipe.transformer.facebook

import akka.actor.{ActorRef, Props}
import na.datapipe.transformer.DataTransformer

import scala.collection.immutable.HashSet

/**
 * @author nader albert
 * @since  21/07/2015.
 */

case class ElementTransformed(lineId :Int)

class FacebookTransformer(analyzersSystemHost: String,
                          analyzersSystemPort: String, requester: ActorRef) extends DataTransformer {

  val analyzer = context.actorSelection("akka.tcp://nlp@" + analyzersSystemPort + ":" + analyzersSystemPort +
    " /user/analyzer")

  val publisher = context.actorSelection("akka.tcp://publishers@" + "127.0.0.1" + ":" + "2556" + "/user/publisher")

  var posts = HashSet[String]()

  override def receive: Receive = ???
    /*case TransformLine (line, lineId) => {
      val processedPosts = processor.process(line)

      if (null == processedPosts) { // we have 123 line in the sample file that haven't got any posts
        requester ! ElementTransformed(lineId)
      } else {
        //TODO: send the processed posts to nlqp
        val postIterator = processedPosts.iterator

        if(! postIterator.hasNext) // we have 69 lines in the sample file, that have the post iterator empty
          requester ! ElementTransformed(lineId)

        while (postIterator.hasNext) {

          val mapper: ObjectMapper = new ObjectMapper

          //this is using java reflection which is not advised to use.. better doing it by hand
          val convertedPost: Post = mapper.convertValue(postIterator.next, classOf[Post])

          analyzer ! AnalyzePost(convertedPost, convertedPost.getPost_id, lineId)

          publisher ! PublishRawPost(convertedPost)

          posts += convertedPost.getPost_id
        }
      }
    }
    case PostAnalyzed(postId, lineId) => { //when all posts generated out of this line are analyzed, we can reply with a Analyzed message, marking the entire line as analyzed
      posts -= postId
      if (posts.isEmpty)
        requester ! ElementTransformed(lineId) // notify my parent supervisor that I am done
    } */
}

object FacebookTransformer {
  def props(analyzersSystemHost: String, analyzersSystemPort: String, requester: ActorRef) =
    Props(classOf[FacebookTransformer], analyzersSystemHost, analyzersSystemPort, requester)
}