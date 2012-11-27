package km.honeycomb

import scala.collection.immutable._
import akka.actor._
import akka.actor.Status.Failure
import akka.pattern.Patterns._
import akka.util.Timeout
import akka.util.duration._

class MembershipService extends Actor with ActorLogging {

  import MembershipService._
  import HashService._

  val hashService = context.actorFor("../hashService")

  var members: SortedMap[BigInt, String] = SortedMap.empty

  var myCode: Option[BigInt] = None

  var leaderMembershipService: Option[ActorRef] = None

  val config = context.system.settings.config

  val myname = self.path.name
  val myaddr = self.path.address.toString
  val myhost = config.getString("akka.remote.netty.hostname")
  val myport = config.getInt("akka.remote.netty.port")
  val mypath = myaddr + "@" + myhost + ":" + myport + "/user/" + myname

  def receive = {
    case SetLeader(path, myself) =>
      log.info("My path: {}", mypath)
      log.info("Leader's path: {}", path)
      leaderMembershipService = Some(context.actorFor(path + "/membershipService"))
      ask(hashService, Hash(mypath), Timeout(1 second))
        .mapTo[Code]
        .onSuccess { case Code(code) =>
          myCode = Some(code)
          leaderMembershipService.get ! Join(mypath, code)
        }

    case Join(who, code) =>
      log.info("Join node: {} :: {}", who, code.toString)
      addMember(code, who)
      
    case Leave(who) =>
      log.info("Leave node: {}", who)
      
    case FindNode(c: BigInt) =>
      // extended floor function
      val revKey = members.keySet.toList.reverse
      val path = revKey.find(code => code < c) match {
        case Some(theCode) => members(theCode)
        case None => members(members.maxBy(_._1)._1)
      }
      sender ! NodeForCode(c, path)
      

    case x => log.warning("Unknown message: {}", x.toString)
  }

  private def addMember(code: BigInt, path: String) = members += code -> path

  private def delMember(code: BigInt) = members -= code

  private def actorPath(ref: ActorRef) = ref.path.toStringWithAddress(ref.path.address)
}

object MembershipService {
  case class SetLeader(path: String, myself: Boolean = false)
  case class Join(who: String, code: BigInt)
  case class Leave(who: String)
  case class FindNode(code: BigInt)
  case class NodeForCode(code: BigInt, path: String)
}