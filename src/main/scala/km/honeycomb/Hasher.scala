package km.honeycomb

import akka.actor._
import java.security.MessageDigest

class Hasher extends Actor with ActorLogging {
  import Hasher._
  val sha = MessageDigest.getInstance("sha-1")
  
  def receive = {
    case Hash(x) => sender ! Code(sha.digest(x))
    case _ => Unit
  } 
}

object Hasher {
  case class Hash(x: Array[Byte])
  case class Code(c: Array[Byte])
}