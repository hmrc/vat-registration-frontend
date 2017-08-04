package support

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{configureFor, reset}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import uk.gov.hmrc.play.it.Port.randomAvailable

trait StartAndStopWireMock extends BeforeAndAfterEach with BeforeAndAfterAll {
  self: Suite =>

  protected val wiremockPort = randomAvailable
  protected val wiremockHost = "localhost"
  protected val wiremockBaseUrl: String = s"http://$wiremockHost:$wiremockPort"
  val wireMockServer = new WireMockServer(wireMockConfig().port(wiremockPort))

  override def beforeAll() = {
    wireMockServer.stop()
    wireMockServer.start()
    configureFor(wiremockHost, wiremockPort)
  }

  override def beforeEach() = {
    reset()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
  }
}
