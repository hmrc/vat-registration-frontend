/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package support


import org.scalatest.concurrent.{IntegrationPatience, PatienceConfiguration}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Suite, TestSuite}
import org.scalatestplus.play.OneServerPerSuite
import play.api.http.HeaderNames
import play.api.libs.ws.WS
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.{FakeApplication, FakeRequest}
import support.SessionBuilder.getSessionCookie
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.it.Port

trait AppAndStubs extends StartAndStopWireMock with StubUtils with OneServerPerSuite with IntegrationPatience with PatienceConfiguration {
  me: Suite with TestSuite =>

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val portNum: Int = port
  implicit val requestHolder: RequestHolder = new RequestHolder(FakeRequest().withFormUrlEncodedBody())

  def request: FakeRequest[AnyContentAsFormUrlEncoded] = requestHolder.request

  abstract override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(
      timeout = Span(4, Seconds),
      interval = Span(50, Millis))

  override lazy val port: Int = Port.randomAvailable

  def buildClient(path: String)(implicit headers:(String,String) = HeaderNames.COOKIE -> getSessionCookie()) = {
    WS.url(s"http://localhost:$port/register-for-vat$path").withFollowRedirects(false).withHeaders(headers,"Csrf-Token" -> "nocheck")
  }

  override implicit lazy val app: FakeApplication = FakeApplication(
    //override app config here, chaning hosts and ports to point app at Wiremock
    additionalConfiguration = replaceWithWiremock(Seq(
      "address-lookup-frontend",
      "auth",
      "auth.company-auth",
      "vat-registration",
      "company-registration",
      "company-registration-frontend",
      "incorporation-frontend-stub",
      "incorporation-information",
      "cachable.short-lived-cache",
      "cachable.session-cache",
      "business-registration-dynamic-stub",
      "bank-account-reputation",
      "identity-verification-proxy",
      "identity-verification-frontend"
    ))
  )

  private def replaceWithWiremock(services: Seq[String]) =
    services.foldLeft(Map.empty[String, Any]) { (configMap, service) =>
      configMap + (
        s"microservice.services.$service.host" -> wiremockHost,
        s"microservice.services.$service.port" -> wiremockPort)
    } +
      (s"auditing.consumer.baseUri.host" -> wiremockHost, s"auditing.consumer.baseUri.port" -> wiremockPort) +
      ("play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck") +
      ("microservice.services.vat-registration-eligibility-frontend.www.host" -> "") +
      ("microservice.services.vat-registration-eligibility-frontend.uri" -> "/vat-eligibility-uri") +
      ("microservice.services.business-registration-dynamic-stub.uri" -> "/iv-uri") +
      ("microservice.services.iv.identity-verification-proxy.host" -> wiremockHost) +
      ("microservice.services.iv.identity-verification-proxy.port" -> wiremockPort)+
      ("microservice.services.iv.identity-verification-frontend.host" -> wiremockHost) +
      ("microservice.services.iv.identity-verification-frontend.port" -> wiremockPort)
}




