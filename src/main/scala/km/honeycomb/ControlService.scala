package km.honeycomb

import akka.actor._
import akka.actor.SupervisorStrategy._
import akka.actor.Status.Failure
import akka.pattern.Patterns._
import akka.util.Timeout
import akka.util.duration._

class ControlService extends Actor with ActorHelper with ActorLogging {

  import ControlService._
  import ZooKeeperClient._
  import MembershipService._
  import BucketService._
  import HashService._

  var leader: Option[ActorRef] = None
  var leaderPath: Option[String] = None

  val zkClient = context.actorOf(Props[ZooKeeperClient], "zookeeperClient")
  val hashService = context.actorOf(Props[HashService], "hashService")
  val membershipService = context.actorOf(
    Props[MembershipService],
    "membershipService"
  )
  val bucketService = context.actorOf(Props[BucketService], "bucketService")

  //  val config = context.system.settings.config
  //  val myname = self.path.name
  //  val myaddr = self.path.address.toString
  //  val myhost = config.getString("akka.remote.netty.hostname")
  //  val myport = config.getInt("akka.remote.netty.port")
  //  val mypath = myaddr + "@" + myhost + ":" + myport + "/user/" + myname

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ActorInitializationException => Restart
      case _: ActorKilledException => Restart
    }

  override def preStart() = {
    log.info("{} started", mypath)
    zkClient ! WhoIsLeader
  }

  def receive = {
    case LeaderIs(who) =>
      // save some information about leader
      leaderPath = Some(who)
      val leaderRef = context.actorFor(who)
      if (leaderRef != deadLetterActorRef) {
        leader = Some(leaderRef)
        membershipService ! SetLeader(leaderPath.get, self == leader.get)
      } else {
        log.warning("Failed to find leader")
        sys.exit(1)
        //context.system.shutdown()
      }
    case Terminated(actorRef) =>
      log.info("{} is terminated", toFullPath(actorRef))
      sys.exit(1)

    case x => log.warning("Unknown message: {}", x.toString)
  }

  private def leaderFor(name: String) = context.actorFor(leaderPath.get + name)

  private def isLeader(ref: ActorRef) = ref == leader.get

}

object ControlService {
  case class Get(key: String)
  case class Put(key: String, value: String)
  class LeaderNotFoundException(msg: String) extends RuntimeException(msg)
}
