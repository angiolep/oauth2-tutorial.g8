package oauth2.tutorial

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import java.net.URLDecoder.decode
import java.net.URLEncoder.encode

object AuthorizationServer extends App
  with TemplateSupport with SSLSupport with IdentityChallenger {

  implicit val system = ActorSystem("authorizationServer")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val Html = ContentTypes.`text/html(UTF-8)`
  val Json = ContentTypes.`application/json`
  val Utf8 = "UTF-8"
  val webroot = "src/main/web/server"

  val route =
    path("authorization") {
      get {
        authenticateBasic(realm, ownerAuthenticator) { owner =>
          parameterMap { params =>
            val responseType = params("response_type")
            val clientId = params("client_id")
            val redirectUri = params("redirect_uri")
            //val scope = params("scope")
            //val state = params("state")
            // TODO validate request ...
            complete(HttpEntity(Html,
              render("grant_form", params + ("user_id" -> owner))
            ))
          }
        }
      }
    } ~
    path("grant") {
      post {
        authenticateBasic(realm, ownerAuthenticator) { owner =>
          formFieldMap { fields =>
            val redirectUri = fields("redirect_uri")
            // TODO validate request and calculate code
            val code = encode("code", Utf8)
            redirect(
              redirectionType = StatusCodes.Found, // 302
              uri = Uri(decode(redirectUri, Utf8))
                .withQuery(Query(
                  "code" -> code,
                  "redirect_uri" -> redirectUri
                ))
            )
          }
        }
      }
    } ~
    path("token") {
      post {
        authenticateBasic(realm, clientAuthenticator) { client =>
          formFieldMap { fields =>
            val grantType = fields("grant_type")
            val code = fields("code")
            val redirectUri = fields("redirect_uri")
            // TODO validate request and calculate token
            val token = encode("token", Utf8)
            complete(HttpEntity(Json, s"""{
             |  "access_token":"\$token"
             |}""".stripMargin
            ))
          }
        }
      }
    }
    pathSingleSlash {
      getFromFile(s"\$webroot/index.html")
    } ~
    getFromDirectory(webroot)


  val bindingFuture = Http().bindAndHandle(route, "localhost", 7070, connectionContext)
  println("OAuth 2.0 Tutorial - AuthServer up and running ...")


  sys.addShutdownHook(() =>
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  )
}
