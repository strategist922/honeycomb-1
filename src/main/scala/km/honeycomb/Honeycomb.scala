package km.honeycomb

import akka.actor._
import com.typesafe.config._

object Honeycomb {

  //import ZooKeeperClient._

  val system = ActorSystem(
    "honeycomb",
    ConfigFactory.load.getConfig("node")
  )
  
  system.actorOf(Props[ControlService], "controller")

  def main(args: Array[String]): Unit = {
    //zkClient tell (WhoIsLeader, control)
    
    val rest = new RESTService(8080)
    rest.start()
    
  }

}

