package km.honeycomb

import scala.collection.immutable._
import akka.actor._
import akka.actor.Status.Failure
import akka.pattern.Patterns._
import akka.util.Timeout
import akka.util.duration._

class BucketService extends Actor with ActorLogging with ActorHelper {
  
  import BucketService._
  import HashService._
  import MembershipService._
  
  var bucket: TreeMap[BigInt, String] = TreeMap.empty
  
  val hashService = context.actorFor("../hashService")
  val membershipService = context.actorFor("../membershipService")
  
  def receive = {
    case Put(key, value) =>
      val fut = for {
        c <- ask(hashService, Hash(key), Timeout(1 second)).mapTo[Code]
        n <- ask(membershipService, FindNode(c.c), Timeout(1 second)).mapTo[NodeForCode]
      } yield n
      fut onSuccess { case NodeForCode(code, path) =>
        val ref = context.actorFor(path + "/buckerService")
        ref ! Store(code, value)
      }
      
    case Get(key) =>
      //sender ! bucket.get(key)
      
    case Store(code: BigInt, value: String) =>
      bucket += code -> value
      
    case x => log.warning("Unknown message: {}", x.toString)
  }

}

object BucketService {
  case class Get(key: String)
  case class Put(key: String, value: String)
  case class Store(code: BigInt, value: String)
  case class Load(key: String)
}