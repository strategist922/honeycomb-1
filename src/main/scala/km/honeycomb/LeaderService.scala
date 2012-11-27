package km.honeycomb

import akka.actor._
import akka.actor.Status.Failure
import akka.pattern.Patterns._
import akka.util.Timeout
import akka.util.duration._

class MembershipService extends Actor with ActorLogging {

  import MembershipService._
  import HashService._

  val hashService = context.actorFor("../hashService")

  var members: Map[String, BigInt] = Map.empty

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
      if (myself) myCode = Some(0)
      leaderMembershipService =
        if (myself) Some(self)
        else Some(context.actorFor(path))
        if (!myself) leaderMembershipService.get ! 

    case Join(who, isLeader) =>
      log.info("Join node: {}", who)
      val theSender = sender
      val theMembers = members
      ask(hashService, RandomHash, Timeout(1 seconds)).mapTo[Code] onComplete {
        case Right(Code(x)) =>
          theSender ! JoinAck(x)
          val path = actorPath(theSender)
          addMember(path, x)
          broadcastJoinNews(theMembers, path, x)
        case Left(e) =>
          theSender ! Failure(e)
      }

    case Leave(who) =>
      log.info("Leave node: {}", who)
      val theSender = sender
      delMember(actorPath(theSender))

    case JoinAnnounce(who, code) =>
      val ref = context.actorFor(who)
      ref ! IntroduceMyself(actorPath(self), myCode.get)

    case x => log.warning("Unknown message: {}", x.toString)
  }

  private def broadcastJoinNews(theMembers: Map[String, BigInt],
                                who: String,
                                code: BigInt) = members map {
    case (path, code) =>
      val ref = context.actorFor(path)
      ref ! JoinAnnounce(who, code)
  }

  private def introduceMyself() = members

  private def addMember(path: String, code: BigInt) = members += path -> code

  private def delMember(path: String) = members -= path

  private def actorPath(ref: ActorRef) = ref.path.toStringWithAddress(ref.path.address)
}

object MembershipService {
  case class SetLeader(path: String, myself: Boolean = false)
  case class Join(who: String, isLeader: Boolean = false)
  case class Leave(who: String)
  case class JoinAck(code: BigInt)
  case object LeaveAck
  case class JoinAnnounce(who: String, code: BigInt)
  case class IntroduceMyself(who: String, code: BigInt)
  case class MyCode(code: BigInt)
}