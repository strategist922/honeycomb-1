package km.honeycomb

import akka.actor._
import com.typesafe.config._

object Honeycomb {

  import ZooKeeperClient._

  val system = ActorSystem(
    "honeycomb",
    ConfigFactory.load.getConfig("node")
  )

  val control = system.actorOf(Props[ControlService], "controller")

  def main(args: Array[String]) = {
    //zkClient tell (WhoIsLeader, control)
    new RESTService(8080)
  }

}

