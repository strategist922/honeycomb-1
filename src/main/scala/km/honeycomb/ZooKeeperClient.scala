package km.honeycomb

import akka.actor._

/** Not yet implemented. Actually, it's dummy! */
class ZooKeeperClient extends Actor with ActorHelper with ActorLogging {

  import ZooKeeperClient._

  def receive = {
    case WhoIsLeader =>
      // akka://honeycomb@127.0.0.1:2550/user/controller
      val leaderHost = config.getString("leader.host")
      val leaderPort = config.getInt("leader.port")
      val leaderPath = "akka://honeycomb@" + leaderHost + ":" +
        leaderPort + "/user/controller"
      sender ! LeaderIs(leaderPath)
    case x => log.warning("Unknown message: {}", x.toString)
  }
}

object ZooKeeperClient {
  case object WhoIsLeader
  case class LeaderIs(who: String)
}