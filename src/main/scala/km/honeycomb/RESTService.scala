package km.honeycomb

import com.typesafe.play.mini._
import play.api.mvc._
import play.api.mvc.Results._

object Global extends com.typesafe.play.mini.Setup(km.honeycomb.RESTService)

object RESTService extends Application {
  def route = {
    case GET(Path("/coco")) & QueryString(qs) => Action {
      val o = QueryString(qs, "foo").getOrElse("noh")
      Ok(<h1>It works!, query String { o }</h1>).as("text/html")
    }
    case GET(Path("/flowers")) => Action {
      Ok(<h1>It works for flowers!</h1>).as("text/html")
    }
  }
}