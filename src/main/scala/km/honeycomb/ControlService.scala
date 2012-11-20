package km.honeycomb

import akka.actor._

class ControlService extends Actor with ActorLogging {
  def receive = {
    case x => log.warning("Unknown message: {}", x.toString)
  }
}