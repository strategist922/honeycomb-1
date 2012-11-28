package km.honeycomb

import akka.actor._
import akka.actor.Status.Failure
import akka.pattern.Patterns._
import akka.util.Timeout
import akka.util.duration._
import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._

class RESTService(port: Int) {
  
  Http(port).chunked(1048576).plan(new Routes()).run()
  
  class Routes extends async.Plan with ServerErrorResponse {
    
    import BucketService._
    
    val system = Honeycomb.system
    val bucketService = system.actorFor("controller/bucketService")
    
    def intent() = {
      case r @ GET(Path(Seg(key :: Nil))) =>
        ask(bucketService, Get(key), Timeout(1 second))
        .mapTo[Option[String]]
        .onComplete {
          case Right(Some(value)) => r.respond(ResponseString(value))
          case Right(None) => r.respond(BadRequest)
          case Left(e) => r.respond(BadRequest)
        }
        
      case r @ POST(Path(Seg(key :: Nil))) & QueryParams(params) =>
        params.get("value") match {
          case Some(Seq(value)) =>
            bucketService ! Put(key, value)
            r.respond(ResponseString("ok"))
          case _ =>
            r.respond(BadRequest)
        }
    }
  }
}
