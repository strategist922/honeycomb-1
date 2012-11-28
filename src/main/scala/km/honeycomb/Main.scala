package km.honeycomb

import akka.actor._
import com.typesafe.play.mini._
import play.api.mvc._
import play.api.mvc.Results._
import com.typesafe.config.ConfigFactory

object Honeycomb {

  import ZooKeeperClient._

  val system = ActorSystem(
    "honeycomb",
    ConfigFactory.load.getConfig("node")
  )

  val control = system.actorOf(Props[ControlService], "controller")

  def main(args: Array[String]) = {
    //zkClient tell (WhoIsLeader, control)
  }

}