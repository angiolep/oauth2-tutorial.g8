package oauth2.tutorial

import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import akka.http.scaladsl.ConnectionContext


trait SSLSupport {

  val passwd = "changeit".toCharArray

  val ks = {
    val ks = KeyStore.getInstance("JKS")
    val in = this.getClass.getResourceAsStream("/tutorial.com.jks")
    try ks.load(in, passwd)
    finally in.close()
    ks
  }

  val kmf = KeyManagerFactory.getInstance("SunX509")
  kmf.init(ks, passwd)

  val tmf = TrustManagerFactory.getInstance("SunX509")
  tmf.init(ks)

  val sslContext = SSLContext.getInstance("TLS")
  sslContext.init(kmf.getKeyManagers, tmf.getTrustManagers, new SecureRandom)

  var connectionContext = ConnectionContext.https(sslContext)
}
