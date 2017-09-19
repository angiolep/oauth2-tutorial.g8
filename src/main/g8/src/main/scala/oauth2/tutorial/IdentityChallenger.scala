package oauth2.tutorial

import akka.http.scaladsl.server.directives.Credentials

trait IdentityChallenger {

  val realm = "OAuth2 Tutorial - Identity Challenge"

  val ownerId = "owner"
  val ownerPasswd = "password"

  // TODO the clientId and clientSecret shall be assigned by the AuthorizationServer during a preliminary "registration phase"
  val clientId = "client"
  val clientSecret = "secret"

  def ownerAuthenticator(credentials: Credentials): Option[String] = {
    credentials match {
      case c @ Credentials.Provided(userid) if (userid == ownerId && c.verify(ownerPasswd)) => Some(userid)
      case _ => None
    }
  }

  def clientAuthenticator(credentials: Credentials): Option[String] = {
    credentials match {
      case c @ Credentials.Provided(userid) if (userid == clientId && c.verify(clientSecret)) => Some(userid)
      case _ => None
    }
  }
}
