package km.honeycomb

import akka.actor._
import com.typesafe.config._

object Honeycomb {

  //import ZooKeeperClient._

  val system = ActorSystem(
    "honeycomb",
    ConfigFactory.load.getConfig("node")
  )

  def main(args: Array[String]): Unit = {
    //zkClient tell (WhoIsLeader, control)
    system.actorOf(Props[ControlService], "controller")
    val rest = new RESTService(8080)
    rest.start()
    
  }

}

