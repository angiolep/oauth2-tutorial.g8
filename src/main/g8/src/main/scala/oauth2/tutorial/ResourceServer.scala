package oauth2.tutorial

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import java.net.URLDecoder.decode
import java.net.URLEncoder.encode

import akka.http.scaladsl.server.directives.Credentials

object ResourceServer extends App with TemplateSupport with SSLSupport {

  implicit val system = ActorSystem("resourceServer")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val Html = ContentTypes.`text/html(UTF-8)`
  val Utf8 = "UTF-8"
  val webroot = "src/main/web/server"
  val realm = "OAuth2 Tutorial - Identity Challenge"

  def authenticator(credentials: Credentials): Option[String] = {
    credentials match {
      case p @ Credentials.Provided(id) if (id == "owner" && p.verify("changeit")) => Some(id)
      case _ => None
    }
  }

  val route =
    getFromDirectory(webroot)


  val bindingFuture = Http().bindAndHandle(route, "localhost", 7070, connectionContext)
  println("OAuth 2.0 Tutorial - AuthServer up and running ...")

  sys.addShutdownHook(() =>
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  )
}
