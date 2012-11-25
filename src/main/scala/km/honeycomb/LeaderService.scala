package km.honeycomb

import akka.actor._

class MembershipService extends Actor with ActorLogging {
  
  import MembershipService._
  import HashService._
  
  val hashService = context.actorFor("../hashService")
  
  def receive = {
    case Join(who) =>
      log.info("Join node: {}", who)
      hashService ! RandomHash
    case Leave(who) =>
      log.info("Leave node: {}", who)
    case Code(x) =>
      log.info("Code: {}", x)
    case x => log.warning("Unknown message: {}", x.toString)
  }
}

object MembershipService {
  case class Join(who: String)
  case class Leave(who: String)
  case class JoinAck(code: String)
  case object LeaveAck
}