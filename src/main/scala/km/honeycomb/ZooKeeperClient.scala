package km.honeycomb

import akka.actor._

/** Not yet implemented. Actually, it's dummy! */
class ZooKeeperClient extends Actor with ActorLogging {
  
  import ZooKeeperClient._
  
  def receive = {
    case WhoIsLeader =>
      sender ! LeaderIs("akka://honeycomb@127.0.0.1:2552/user/controller")
    case LeaderIs(who) =>
    case x => log.warning("Unknown message: {}", x.toString)
  }
}

object ZooKeeperClient {
  case object WhoIsLeader
  case class LeaderIs(who: String)
}