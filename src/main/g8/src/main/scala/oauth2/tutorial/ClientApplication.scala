package oauth2.tutorial

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer


object ClientApplication extends App with TemplateSupport with SSLSupport {

  implicit val system = ActorSystem("clientApplication")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // TODO the clientId and clientSecret shall be assigned by the AuthorizationServer during a preliminary "registration phase"
  val clientId = "client"
  val clientSecret = "secret"

  val clientHost = "localhost"
  val clientPort = 8080
  val clientUrl = s"https://\$clientHost:\$clientPort"

  val authServerHost = "localhost"
  val authServerPort = 7070
  val authServerUrl = s"https://\$authServerHost:\$authServerPort"
  val authorizationEndpoint = "authorization"
  val tokenEndpoint = "token"

  val resourceServerHost = "localhost"
  val resourceServerPort = 6060
  val resourceServerUrl = s"https://\$resourceServerHost:\$resourceServerPort"

  val webroot = "src/main/web/client"
  val Html = ContentTypes.`text/html(UTF-8)`

  val route =
    get {
      path("resource") {
        // TODO if !token
        redirect(
          redirectionType = StatusCodes.Found, // 302
          uri = Uri(s"\$authServerUrl/\$authorizationEndpoint")
            .withQuery(Query(
              "response_type" -> "code",
              "client_id" -> clientId,
              "redirect_uri" -> s"\$clientUrl/callback"
            ))
        )
      } ~
      path("callback") {
        parameterMap { callbackParams =>
          val tokenRequest = Http().singleRequest(HttpRequest(
            method = POST,
            uri = Uri(s"\$authServerUrl/\$tokenEndpoint"),
            headers = Authorization(BasicHttpCredentials(clientId, clientSecret)) :: Nil,
            entity = FormData(Map(
              "grant_type" -> "authorization_code",
              "code" -> callbackParams("code"),
              "redirect_uri" -> callbackParams("redirect_uri")
            )).toEntity
          ))
          onSuccess(tokenRequest) { tokenReply =>
            // TODO get the token out of the reply
            complete(HttpEntity(Html, render("token", Map(
              "token" -> "token"
            ))))
          }
        }
      } ~
      pathSingleSlash {
        getFromFile(s"\$webroot/index.html")
      }
    } ~
    getFromDirectory(webroot)



  val bindingFuture = Http().bindAndHandle(route, clientHost, clientPort, connectionContext)
  println(s"OAuth 2.0 Tutorial - ClientApp running on \$clientHost:\$clientPort ...")

  sys.addShutdownHook(() =>
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  )
}

