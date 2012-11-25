package km.honeycomb

import scala.util.Random
import scala.math._
import akka.actor._
import java.security.MessageDigest

class HashService extends Actor with ActorLogging {

  import HashService._

  val enc = MessageDigest.getInstance("md5")
  val minValue: BigInt = 0
  val maxValue: BigInt = BigInt(2).pow(enc.getDigestLength() * 8)
  val random = new Random()

  def receive = {
    case Hash(x) =>
      val ret = hashFunction(x)
      sender ! Code(ret)
    case RandomHash =>
      val ret = hashFunction()
      sender ! Code(ret)
    case x => log.warning("Unknown message: {}", x.toString)
  }

  private def hashFunction() = {
    var x = new Array[Byte](random.nextInt())
    random.nextBytes(x)
    val ret = BigInt(enc.digest(x))
    enc.reset
    ret
  }
  private def hashFunction(x: Array[Byte]) = {
    val ret = BigInt(enc.digest(x))
    enc.reset
    ret
  }
}

object HashService {
  case class Hash(x: Array[Byte])
  case class Code(c: BigInt)
  case object RandomHash
}