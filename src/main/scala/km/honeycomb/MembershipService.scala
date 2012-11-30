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

  val hashService = context.actorFor("../hashService")

  // code -> path (remote controller)
  var members: SortedMap[BigInt, String] = SortedMap.empty

  var myCode: Option[BigInt] = None

  var leaderMembershipService: Option[ActorRef] = None

  def receive = {
    case SetLeader(leaderPath, myself) =>
      log.info("My path: {}", mypath)
      log.info("My local controller: {}", localControllerPath)
      log.info("Leader's path: {}", leaderPath)
      leaderMembershipService = Some(context.actorFor(leaderPath + "/membershipService"))

      val fut = for {
        c <- ask(hashService, Hash(localControllerPath), Timeout(1 second)).mapTo[Code]
        a <- ask(
          leaderMembershipService.get,
          Join(c.c, localControllerPath),
          Timeout(1 second)
        ).mapTo[JoinAck]
      } yield c
      
      fut onSuccess { case Code(code) =>
        myCode = Some(code)
      }
      // get hash code for local controller path
      ask(hashService, Hash(localControllerPath), Timeout(1 second))
        .mapTo[Code]
        .onSuccess {
          case Code(code) =>
            myCode = Some(code) // set hash code
            // send (local controller path, code) to leader membership service
            leaderMembershipService.get ! Join(code, localControllerPath)
        }

    // Only leader receives Join and Leave message
    case Join(code, actorPath) =>
      log.info("Join node: {} :: {}", actorPath, code.toString)
      members map {
        case (code, path) if path != localControllerPath =>
          // TODO - cache remote actor reference for performance!
          val ref = context.actorFor(path + "/membershipService")
          log.info("Announce {}'s Join to {}", actorPath, path)
          ref ! (code, actorPath)
      }
      addMember(code, actorPath)

      // Re-balance
      val fromPath = nodePathByCircularFloor(code)
      val fromRef = context.actorFor(fromPath + "/membershipService")
      fromRef ! "mig!"

    // Only leader receives Join and Leave message
    case Leave(who, code) =>
      log.info("Leave node: {}", who)
      delMember(code)

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
  case class Leave(who: String, code: BigInt)
  case class FindNode(code: BigInt)
  case class NodeForCode(code: BigInt, path: String)
}