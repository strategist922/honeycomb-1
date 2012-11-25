package km.honeycomb

import akka.actor._

class ControlService extends Actor with ActorLogging {

  import ZooKeeperClient._
  import MembershipService._

  var leader: Option[ActorRef] = None
  var leaderURI: Option[String] = None

  val zkClient = context.actorOf(Props[ZooKeeperClient], "zookeeperClient")
  context.actorOf(Props[HashService], "hashService")
  context.actorOf(Props[MembershipService], "membershipService")
  var membershipService: Option[ActorRef] = None

  val config = context.system.settings.config

  val myname = self.path.name
  val myaddr = self.path.address.toString
  val myhost = config.getString("akka.remote.netty.hostname")
  val myport = config.getInt("akka.remote.netty.port")

  private def addr = myaddr + "@" + myhost + ":" + myport + "/user/" + myname

  override def preStart() = {
    log.info("{} started", addr)
    zkClient ! WhoIsLeader
  }

  def receive = {
    case LeaderIs(who) =>
      leaderURI = Some(who)
      leader = Some(context.actorFor(who))
      membershipService = Some(leaderFor("/membershipService"))
      membershipService.get ! Join(addr)
    case m @ Join(who) =>
      val ref = leaderFor("/leaderService")
      log.info("{}", ref.toString())
    //leaderService forward m
    case x => log.warning("Unknown message: {}", x.toString)
  }

  private def leaderFor(name: String) = context.actorFor(leaderURI.get + name)

}