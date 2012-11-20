package km.honeycomb

import akka.actor._
import com.typesafe.config.ConfigFactory

object Honeycomb {
  
  import ZooKeeperClient._

  val system = ActorSystem(
    "honeycomb",
    ConfigFactory.load.getConfig("node")
  )
  
  val control = system.actorOf(Props[ControlService], "controlService")
  val zkClient = system.actorOf(Props[ZooKeeperClient], "zookeeperClient")
  
  def main(args: Array[String]) = {
    zkClient tell (WhoIsLeader, control)
  }

}