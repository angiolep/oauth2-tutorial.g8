package oauth2.tutorial

import org.fusesource.scalate.TemplateEngine
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import java.net.URLDecoder.decode
import java.net.URLEncoder.encode

import akka.http.scaladsl.server.directives.Credentials

object OAuth2Server extends App with TemplateSupport {

  implicit val system = ActorSystem("authserver")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val Html = ContentTypes.`text/html(UTF-8)`
  val Utf8 = "UTF-8"
  val webroot = "src/main/web/server"
  val realm = "OAuth2 Tutorial - Identity Challenge"

  def authenticator(credentials: Credentials): Option[String] = {
    credentials match {
      case p @ Credentials.Provided(id) if (p.verify("changeme")) => Some(id)
      case _ => None
    }
  }

  val route =
    path("authorize") {
      get {
        authenticateBasic(realm, authenticator) { owner =>
          parameterMap { params =>
            complete(HttpEntity(Html,
              render("authorize", params)
            ))
          }
        }
      }
    } ~
    path("grant") {
      post {
        authenticateBasic(realm, authenticator) { owner =>
          formFieldMap { fields =>
            redirect(
              Uri(decode(fields("redirect_uri"), Utf8))
                .withQuery(Query(
                  // TODO how to calculate the authorization code?
                  "code" -> encode("TODO", Utf8)
                )),
              StatusCodes.Found
            )
          }
        }
      }
    } ~
    pathSingleSlash {
      getFromFile(s"\$webroot/index.html")
    } ~
    getFromDirectory(webroot)


  val bindingFuture = Http().bindAndHandle(route, "localhost", 7070)
  println("OAuth 2.0 Tutorial - AuthServer up and running ...")

  sys.addShutdownHook(() =>
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  )
}
