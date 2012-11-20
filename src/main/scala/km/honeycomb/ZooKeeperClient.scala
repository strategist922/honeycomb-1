package km.honeycomb

import akka.actor._

/** Not yet implemented. Actually, it's dummy! */
class ZooKeeperClient extends Actor with ActorLogging {
  
  import ZooKeeperClient._
  
  val isLeader =
    if (context.system.settings.config.hasPath("leader"))
      context.system.settings.config.getBoolean("leader")
    else
      false
  
  def receive = {
    case WhoIsLeader =>
      if (isLeader) sender ! self.path.root.address
      else sender ! "null"
    case LeaderIs(who) =>
    case x => log.warning("Unknown message: {}", x.toString)
  }
}

object ZooKeeperClient {
  case object WhoIsLeader
  case class LeaderIs(who: String)
}