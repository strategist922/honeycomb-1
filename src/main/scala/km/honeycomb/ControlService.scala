package km.honeycomb

import akka.actor._
import akka.actor.Status.Failure
import akka.pattern.Patterns._
import akka.util.Timeout
import akka.util.duration._

class ControlService extends Actor with ActorLogging {

  import ControlService._
  import ZooKeeperClient._
  import MembershipService._
  import BucketService._
  import HashService._

  var leader: Option[ActorRef] = None
  var leaderURI: Option[String] = None

  val zkClient = context.actorOf(Props[ZooKeeperClient], "zookeeperClient")
  val hashService = context.actorOf(Props[HashService], "hashService")
  val membershipService = context.actorOf(
    Props[MembershipService],
    "membershipService"
  )
  val bucketService = context.actorOf(Props[BucketService], "bucketService")

  val config = context.system.settings.config
  val myname = self.path.name
  val myaddr = self.path.address.toString
  val myhost = config.getString("akka.remote.netty.hostname")
  val myport = config.getInt("akka.remote.netty.port")
  val mypath = myaddr + "@" + myhost + ":" + myport + "/user/" + myname

  override def preStart() = {
    log.info("{} started", mypath)
    zkClient ! WhoIsLeader
  }

  def receive = {
    case LeaderIs(who) =>
      // save some information about leader
      leaderURI = Some(who)
      leader = Some(context.actorFor(who))

      // notify my membershipService who is leader
      membershipService ! SetLeader(who, self == leader.get)
      
    case Get(key) =>
      val theSender = sender
      ask(bucketService, Load(key), Timeout(1 second))
      .mapTo[Option[String]]
      .onSuccess { case x => theSender ! x }
      
    case Put(key, value) =>
      for {
        c <- ask(hashService, Hash(key), Timeout(1 second)).mapTo[Code]
        
      }
      bucketService ! Store(key, value)

    case x => log.warning("Unknown message: {}", x.toString)
  }

  private def leaderFor(name: String) = context.actorFor(leaderURI.get + name)

  private def isLeader(ref: ActorRef) = ref == leader.get

}

object ControlService {
  case class Get(key: String)
  case class Put(key: String, value: String)
}
