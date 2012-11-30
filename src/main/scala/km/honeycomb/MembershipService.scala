package km.honeycomb

import scala.collection.immutable._
import akka.actor._
import akka.actor.Status.Failure
import akka.pattern.Patterns._
import akka.util.Timeout
import akka.util.duration._

class MembershipService extends Actor with ActorHelper with ActorLogging {

  import MembershipService._
  import HashService._
  import BucketService._

  val hashService = context.actorFor("../hashService")

  // code -> path (remote controller)
  var members: SortedMap[BigInt, String] = SortedMap.empty

  var myCode: Option[BigInt] = None

  var leaderMembershipService: Option[ActorRef] = None

  def receive = {
    case Code(code) =>
      myCode = Some(code)
      leaderMembershipService.get ! Join(code, localControllerFullPath)

    case SetLeader(leaderPath, myself) =>
      leaderMembershipService =
        Some(context.actorFor(leaderPath + "/membershipService"))
      if (deadLetterActorRef == leaderMembershipService.get) context.stop(self)

      hashService ! Hash(localControllerFullPath)

    // Only leader receives Join and Leave message
    case Join(code, actorFullPath) =>
      log.info("Join node: {} :: {}", actorFullPath, code.toString)
      log.info("localControllerFullPath: {}", localControllerFullPath)
      members foreach {
        case (code, path) if path != localControllerFullPath =>
          // TODO - cache remote actor reference for performance!
          val ref = context.actorFor(path + "/membershipService")
          log.info("Announce {}'s Join to {}", actorFullPath, path)
          ref ! JoinAnnounce(code, actorFullPath)
        case _ => Unit
      }

      // Re-balance
      if (members.size > 0) {
        val fromPath = nodePathByCircularFloor(code)
        val fromRef = context.actorFor(fromPath + "/bucketService")
        fromRef ! Migration(code, actorFullPath)
      }

      addMember(code, actorFullPath)

    // Only leader receives Join and Leave message
    case Leave(who, code) =>
      log.info("Leave node: {}", who)
      delMember(code)

    case JoinAnnounce(code, fullPath) => addMember(code, fullPath)

    case FindNode(c: BigInt) =>
      // extended floor function
      val path = nodePathByCircularFloor(c)
      sender ! NodeForCode(c, path)

    case x => log.warning("Unknown message: {}", x.toString)
  }

  private def nodePathByCircularFloor(c: BigInt): String = {
    val revKey = members.keySet.toList.reverse
    revKey.find(code => code < c) match {
      case Some(theCode) => members(theCode)
      case None => members(members.maxBy(_._1)._1)
    }
  }

  private def addMember(code: BigInt, path: String) = members += code -> path

  private def delMember(code: BigInt) = members -= code

  private def actorPath(ref: ActorRef) = ref.path.toStringWithAddress(ref.path.address)
}

object MembershipService {
  case class SetLeader(leaderPath: String, myself: Boolean = false)
  case class Join(code: BigInt, actorPath: String)
  case class JoinAck()
  case class JoinAnnounce(code: BigInt, fullPath: String)
  case class Leave(who: String, code: BigInt)
  case class FindNode(code: BigInt)
  case class NodeForCode(code: BigInt, path: String)
}