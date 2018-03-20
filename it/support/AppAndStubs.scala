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


import java.util.Base64

import org.scalatest.concurrent.{IntegrationPatience, PatienceConfiguration}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Suite, TestSuite}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import support.SessionBuilder.getSessionCookie
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.it.Port

trait AppAndStubs extends StartAndStopWireMock with StubUtils with GuiceOneServerPerSuite with IntegrationPatience with PatienceConfiguration {
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

  private val ws: WSClient = app.injector.instanceOf(classOf[WSClient])

  def buildClient(path: String)(implicit headers:(String,String) = HeaderNames.COOKIE -> getSessionCookie()) = {
    ws.url(s"http://localhost:$port/register-for-vat$path").withFollowRedirects(false).withHeaders(headers,"Csrf-Token" -> "nocheck")
  }

  def buildInternalClient(path: String)(implicit headers:(String,String) = HeaderNames.COOKIE -> getSessionCookie()) = {
    ws.url(s"http://localhost:$port/internal$path").withFollowRedirects(false).withHeaders(headers,"Csrf-Token" -> "nocheck")
  }

  val encryptedRegIdList1  = Base64.getEncoder.encodeToString("99,98".getBytes("UTF-8"))

  def additionalConfig: Map[String, String] =     Map(
    "regIdWhitelist" -> s"OTgsOTk="
  )



  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(replaceWithWiremock(Seq(
      "address-lookup-frontend",
      "auth",
      "auth.company-auth",
      "vat-registration",
      "business-registration",
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
    )) ++ additionalConfig)
    .build()

  private def replaceWithWiremock(services: Seq[String]) =
    services.foldLeft(Map.empty[String, Any]) { (configMap, service) =>
      configMap + (
        s"microservice.services.$service.host" -> wiremockHost,
        s"microservice.services.$service.port" -> wiremockPort)
    } +
      (s"auditing.consumer.baseUri.host" -> wiremockHost, s"auditing.consumer.baseUri.port" -> wiremockPort) +
      ("play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck") +
      ("microservice.services.vat-registration-eligibility-frontend.www.host" -> s"http://$wiremockHost:$wiremockPort") +
      ("microservice.services.vat-registration-eligibility-frontend.uri" -> "/vat-eligibility-uri") +
      ("microservice.services.business-registration-dynamic-stub.uri" -> "/iv-uri") +
      ("microservice.services.business-registration.uri" -> "/business-registration") +
      ("microservice.services.iv.identity-verification-proxy.host" -> wiremockHost) +
      ("microservice.services.iv.identity-verification-proxy.port" -> wiremockPort)+
      ("microservice.services.iv.identity-verification-frontend.host" -> wiremockHost) +
      ("microservice.services.iv.identity-verification-frontend.port" -> wiremockPort) +
      ("microservice.services.address-lookup-frontend.new-address-callback.url" -> s"http://localhost:$port") +
      ("microservice.services.vat-registration-eligibility-frontend.host" -> s"$wiremockHost") +
      ("microservice.services.vat-registration-eligibility-frontend.port" -> s"$wiremockPort")
}




