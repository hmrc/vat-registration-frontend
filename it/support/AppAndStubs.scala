package support


import org.scalatest.{BeforeAndAfterEach, Suite, TestSuite}
import org.scalatestplus.play.OneServerPerSuite
import play.api.test.FakeApplication
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.it.Port

trait AppAndStubs extends StartAndStopWireMock with OneServerPerSuite {
  me: Suite with TestSuite =>

  implicit val hc = HeaderCarrier()
  implicit val portNum = port

  override lazy val port: Int = Port.randomAvailable

  override implicit lazy val app: FakeApplication = FakeApplication(
    //override app config here, chaning hosts and ports to point app at Wiremock
    additionalConfiguration = Map(
      "microservice.services.auth.host" -> wiremockHost,
      "microservice.services.auth.port" -> wiremockPort
    )
  )

}

