package km.honeycomb

import akka.actor._

trait ActorHelper extends Actor {
  val config = context.system.settings.config
  val myhost = config.getString("akka.remote.netty.hostname")
  val myport = config.getInt("akka.remote.netty.port")
  
  val deadLetterActorRef = context.system.deadLetters
  
  val myFullPath = toFullPath(self)
  val localControllerFullPath = toFullPath(context.system.actorFor("user/controller"))
  
  val mypath = toPath(self)
  val localControllerPath = toPath(context.system.actorFor("user/controller"))
  
  def toPath(actor: ActorRef): String = {
    actor.path.toStringWithAddress(actor.path.address)
  }
  
  def toFullPath(actor: ActorRef): String = {
    val addr = actor.path.address
    val addrString = addr.toString
    val pathString = actor.path.toStringWithAddress(addr)
    val suffix = pathString diff addrString
    addrString + "@" + myhost + ":" + myport + suffix
  }
}
