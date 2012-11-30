package km.honeycomb

import scala.collection.immutable._
import akka.actor._
import akka.dispatch.Future
import akka.actor.Status.Failure
import akka.pattern.Patterns._
import akka.util.Timeout
import akka.util.duration._

class BucketService extends Actor with ActorHelper with ActorLogging {

  import BucketService._
  import HashService._
  import MembershipService._

  var bucket: TreeMap[BigInt, String] = TreeMap.empty

  val hashService = context.actorFor("../hashService")
  val membershipService = context.actorFor("../membershipService")

  override def preStart() = log.info("{} started", myFullPath)
  
  def receive = {
    case Put(key, value) =>
      val theSender = sender
      val fut = findNodeFuture(key)
      fut onSuccess {
        case NodeForCode(code, path) =>
          log.info("Put({}, {}) :: Store({}, {})", key, value, code, value)
          val ref = context.actorFor(path + "/bucketService")
          ref ! Store(code, value)
      }

    case Get(key) =>
      val theSender = sender
      val fut = for {
        n <- findNodeFuture(key)
        v <- ask(
          context.actorFor(n.path + "/bucketService"),
          Load(n.code),
          Timeout(1 second)
        ).mapTo[Option[String]]
      } yield v
      fut onSuccess { case x => theSender ! x }

    case Store(code, value) => bucket += code -> value

    case Load(code) => sender ! bucket.get(code)
    
    case Migration(code, fullPath) =>
      val actor = context.actorFor(fullPath + "/bucketService")
      bucket.filter(_._1 > code).foreach(x => actor ! Store(x._1, x._2))

    case x => log.warning("Unknown message: {}", x.toString)
  }

  private def findNodeFuture(key: String): Future[NodeForCode] =
    for {
      c <- ask(hashService, Hash(key), Timeout(1 second)).mapTo[Code]
      n <- ask(membershipService, FindNode(c.c), Timeout(1 second)).mapTo[NodeForCode]
    } yield n

}

object BucketService {
  case class Get(key: String)
  case class Put(key: String, value: String)
  case class Store(code: BigInt, value: String)
  case class Load(code: BigInt)
  case class Migration(code: BigInt, fullPath: String)
}