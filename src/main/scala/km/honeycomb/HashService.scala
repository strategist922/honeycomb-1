package km.honeycomb

import scala.math._
import akka.actor._
import java.security.MessageDigest

class HashService extends Actor with ActorLogging {

  import HashService._

  val enc = MessageDigest.getInstance("md5")
  val minValue: BigInt = 0
  val maxValue: BigInt = BigInt(2).pow(enc.getDigestLength() * 8)

  def receive = {
    case Hash(k) =>
      val ret = hashFunction(k)
      sender ! Code(ret)
    case x => log.warning("Unknown message: {}", x.toString)
  }

  private def hashFunction(x: String): BigInt = {
    val c = enc.digest(x.getBytes("UTF-8"))
    val ret = BigInt(1, c)
    enc.reset
    ret
  }
}

object HashService {
  case class Hash(k: String)
  case class Code(c: BigInt)
}