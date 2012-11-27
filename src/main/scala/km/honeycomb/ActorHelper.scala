package km.honeycomb

import akka.actor._

trait ActorHelper extends Actor {
  val config = context.system.settings.config
  val myname = self.path.name
  val myaddr = self.path.address.toString
  val myhost = config.getString("akka.remote.netty.hostname")
  val myport = config.getInt("akka.remote.netty.port")
  val mypath = myaddr + "@" + myhost + ":" + myport + "/user/" + myname
}