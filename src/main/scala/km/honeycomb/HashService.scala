package km.honeycomb

import akka.actor._
import java.security.MessageDigest

class HashService extends Actor with ActorLogging {
  import HashService._
  val sha = MessageDigest.getInstance("sha-1")
  
  def receive = {
    case Hash(x) => sender ! Code(sha.digest(x))
    case x => log.warning("Unknown message: {}", x.toString)
  } 
}

object HashService {
  case class Hash(x: Array[Byte])
  case class Code(c: Array[Byte])
}