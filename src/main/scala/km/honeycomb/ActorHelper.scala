package km.honeycomb

import akka.actor._

trait ActorHelper extends Actor {
  val config = context.system.settings.config
  val myhost = config.getString("akka.remote.netty.hostname")
  val myport = config.getInt("akka.remote.netty.port")
  
  val mypath = toFullPath(self)
  val localControllerPath = toFullPath(context.system.actorFor("user/controller"))
  
  def toFullPath(actor: ActorRef): String = {
    val addr = actor.path.address
    val addrString = addr.toString
    val pathString = actor.path.toStringWithAddress(addr)
    val suffix = pathString diff addrString
    addrString + "@" + myhost + ":" + myport + suffix
  }
}
