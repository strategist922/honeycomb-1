package km.honeycomb

import akka.actor._

class LeaderService extends Actor with ActorLogging {
  
  import LeaderService._
  
  def receive = {
    case Join(who) =>
      log.info("Join node: {}", who)
    case Leave(who) =>
      log.info("Leave node: {}", who)
    case x => log.warning("Unknown message: {}", x.toString)
  }
}

object LeaderService {
  case class Join(who: String)
  case class Leave(who: String)
  case class JoinAck(code: String)
  case object LeaveAck
}