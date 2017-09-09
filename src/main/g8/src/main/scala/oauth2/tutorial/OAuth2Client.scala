package oauth2.tutorial

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._


object OAuth2Client extends App with TemplateSupport {

  implicit val system = ActorSystem("clientapp")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val host = "localhost"
  val serverPort = 7070
  val clientPort = 8080
  val serverUrl = s"http://\$host:\$serverPort"
  val webroot = "src/main/web/client"
  val Html = ContentTypes.`text/html(UTF-8)`

  val route =
    path("resource") {
      get {
        redirect(
          Uri(s"\$serverUrl/authorize").withQuery(Query(
            "response_type" -> "code",
            "client_id" -> "awsomeapp",
            "redirect_uri" -> s"http://\$host:\$clientPort/callback"
          )),
          StatusCodes.Found
        )
      }
    } ~
    path("callback") {
      post {
        parameterMap { params =>
          // TODO POST /token
          complete(HttpEntity(Html,
            render("callback", params)
          ))
        }
      }
    } ~
    pathSingleSlash {
      getFromFile(s"\$webroot/index.html")
    } ~
    getFromDirectory(webroot)


  val bindingFuture = Http().bindAndHandle(route, host, clientPort)
  println(s"OAuth 2.0 Tutorial - ClientApp running on \$host:\$clientPort ...")

  sys.addShutdownHook(() =>
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  )
}

