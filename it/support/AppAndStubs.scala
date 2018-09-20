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


import java.time.LocalDate
import java.util.Base64

import common.enums.VatRegStatus
import org.scalatest.concurrent.{IntegrationPatience, PatienceConfiguration}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Suite, TestSuite}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import repositories.ReactiveMongoRepository
import support.SessionBuilder.getSessionCookie
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.it.Port

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Await, Future}

trait AppAndStubs extends StartAndStopWireMock with StubUtils with GuiceOneServerPerSuite with IntegrationPatience with PatienceConfiguration with MongoSpecSupport {
  me: Suite with TestSuite =>

  trait StandardTestHelpers {
    import scala.concurrent.duration._

    def customAwait[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)
    val repo = new ReactiveMongoRepository(app.configuration, mongo)
    val defaultTimeout: FiniteDuration = 5 seconds

    customAwait(repo.ensureIndexes)(defaultTimeout)
    customAwait(repo.drop)(defaultTimeout)

    def insertCurrentProfileIntoDb(currentProfile: models.CurrentProfile, sessionId : String): Boolean = {
      val preawait = customAwait(repo.count)(defaultTimeout)
      val currentProfileMapping: Map[String, JsValue] = Map("CurrentProfile" -> Json.toJson(currentProfile))
      val res = customAwait(repo.upsert(CacheMap(sessionId, currentProfileMapping)))(defaultTimeout)
      res
    }
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val portNum: Int = port
  implicit val requestHolder: RequestHolder = new RequestHolder(FakeRequest().withFormUrlEncodedBody())

  val sessionId: String = "session-ac4ed3e7-dbc3-4150-9574-40771c4285c1"
  val currentProfile = models.CurrentProfile("testingCompanyName", "1", "000-431-TEST", VatRegStatus.draft, None, Some(true))
  val currentProfileIncorp = models.CurrentProfile("testingCompanyName", "1", "000-431-TEST", VatRegStatus.draft, Some(LocalDate.of(2016, 8, 5)), Some(true))

  def request: FakeRequest[AnyContentAsFormUrlEncoded] = requestHolder.request

  abstract override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(
      timeout = Span(4, Seconds),
      interval = Span(50, Millis))

  override lazy val port: Int = Port.randomAvailable

  private val ws: WSClient = app.injector.instanceOf(classOf[WSClient])

  def buildClient(path: String)(implicit headers:(String,String) =  HeaderNames.COOKIE -> getSessionCookie()) = {
    val removeRegisterWithPath = path.replace("""/register-for-vat""","")
    ws.url(s"http://localhost:$port/register-for-vat$removeRegisterWithPath").withFollowRedirects(false).withHeaders(headers,"Csrf-Token" -> "nocheck")
  }

  def buildInternalClient(path: String)(implicit headers:(String,String) = HeaderNames.COOKIE -> getSessionCookie()) = {
    ws.url(s"http://localhost:$port/internal$path").withFollowRedirects(false).withHeaders(headers,"Csrf-Token" -> "nocheck")
  }

  val encryptedRegIdList1  = Base64.getEncoder.encodeToString("99,98".getBytes("UTF-8"))

  def additionalConfig: Map[String, String] =     Map(
    "regIdWhitelist" -> s"OTgsOTk=",
    "mongodb.uri" -> s"$mongoUri"
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
      "identity-verification-frontend",
      "industry-classification-lookup-frontend",
      "industry-classification-lookup-frontend-internal"
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
      ("microservice.services.business-registration-dynamic-stub.uri" -> "/iv-uri") +
      ("microservice.services.business-registration.uri" -> "/business-registration") +
      ("microservice.services.iv.identity-verification-proxy.host" -> wiremockHost) +
      ("microservice.services.iv.identity-verification-proxy.port" -> wiremockPort) +
      ("microservice.services.iv.identity-verification-frontend.host" -> wiremockHost) +
      ("microservice.services.iv.identity-verification-frontend.port" -> wiremockPort) +
      ("microservice.services.address-lookup-frontend.new-address-callback.url" -> s"http://localhost:$port") +
      ("microservice.services.vat-registration-eligibility-frontend.uri"  -> s"http://$wiremockHost:$wiremockPort/uriELFE") +
      ("microservice.services.vat-registration-eligibility-frontend.question" -> s"/foo") +
      ("microservice.services.vat-registration-eligibility-frontend.host"  -> wiremockHost) +
      ("microservice.services.vat-registration-eligibility-frontend.port" -> wiremockPort)
}




