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

import common.enums.VatRegStatus
import itutil.WiremockHelper
import org.scalatest.concurrent.{IntegrationPatience, PatienceConfiguration}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Suite, TestSuite}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.ws.WSClient
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

trait AppAndStubs extends StubUtils with GuiceOneServerPerSuite with IntegrationPatience with PatienceConfiguration {
  me: Suite with TestSuite =>

  trait Setup {
    def customAwait[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)

    val repo = app.injector.instanceOf[SessionRepository]
    val defaultTimeout: FiniteDuration = 2 seconds

    customAwait(repo.ensureIndexes)(defaultTimeout)
    customAwait(repo.drop)(defaultTimeout)

    def insertCurrentProfileIntoDb(currentProfile: models.CurrentProfile, sessionId: String): Boolean = {
      customAwait(repo.count)(defaultTimeout)
      val currentProfileMapping: Map[String, JsValue] = Map("CurrentProfile" -> Json.toJson(currentProfile))
      val res = customAwait(repo.upsert(CacheMap(sessionId, currentProfileMapping)))(defaultTimeout)
      res
    }

    def insertIntoDb[T](sessionId: String, mapping: Map[String, T])(implicit writes: Writes[T]): Boolean = {
      customAwait(repo.count)(defaultTimeout)
      val mappingAsJson = mapping.map { case (id, value) => id -> Json.toJson(value) }
      customAwait(repo.upsert(CacheMap(sessionId, mappingAsJson)))(defaultTimeout)
    }
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val portNum: Int = port
  implicit val requestHolder: RequestHolder = new RequestHolder(FakeRequest().withFormUrlEncodedBody())

  val sessionId: String = "session-ac4ed3e7-dbc3-4150-9574-40771c4285c1"
  val currentProfile: models.CurrentProfile = models.CurrentProfile("1", VatRegStatus.draft)
  val currentProfileIncorp: models.CurrentProfile = models.CurrentProfile("1", VatRegStatus.draft)

  def request: FakeRequest[AnyContentAsFormUrlEncoded] = requestHolder.request

  abstract override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(
      timeout = Span(4, Seconds),
      interval = Span(50, Millis))

  private val ws: WSClient = app.injector.instanceOf(classOf[WSClient])

  def buildClient(path: String)(implicit headers: (String, String) = HeaderNames.COOKIE -> SessionCookieBaker.getSessionCookie()) = {
    val removeRegisterWithPath = path.replace("""/register-for-vat""", "")
    ws.url(s"http://localhost:$port/register-for-vat$removeRegisterWithPath").withFollowRedirects(false).withHttpHeaders(headers, "Csrf-Token" -> "nocheck")
  }

  def buildInternalClient(path: String)(implicit headers: (String, String) = HeaderNames.COOKIE -> SessionCookieBaker.getSessionCookie()) = {
    ws.url(s"http://localhost:$port/internal$path").withFollowRedirects(false).withHttpHeaders(headers, "Csrf-Token" -> "nocheck")
  }

  val encryptedRegIdList1 = Base64.getEncoder.encodeToString("99,98".getBytes("UTF-8"))

  def additionalConfig: Map[String, String] = Map(
    "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test?rm.monitorRefreshMS=1000&rm.failover=default"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(replaceWithWiremock(Seq(
      "address-lookup-frontend",
      "auth",
      "auth.company-auth",
      "vat-registration",
      "cachable.short-lived-cache",
      "cachable.session-cache",
      "bank-account-reputation",
      "industry-classification-lookup-frontend",
      "industry-classification-lookup-frontend-internal",
      "vat-registration-eligibility-frontend",
      "vat-registration-frontend.internal",
      "vat-registration",
      "email-verification",
      "incorporated-entity-identification-frontend",
      "personal-details-validation",
      "iv.identity-verification-proxy",
      "iv.identity-verification-frontend",
      "sole-trader-identification-frontend",
      "partnership-identification-frontend",
      "minor-entity-identification-frontend",
      "upscan-initiate"
    )) ++ additionalConfig)
    .configure("application.router" -> "testOnlyDoNotUseInAppConf.Routes")
    .build()

  private def replaceWithWiremock(services: Seq[String]) =
    services.foldLeft(Map.empty[String, Any]) { (configMap, service) =>
      configMap + (
        s"microservice.services.$service.host" -> WiremockHelper.wiremockHost,
        s"microservice.services.$service.port" -> WiremockHelper.wiremockPort)
    } +
      (s"auditing.consumer.baseUri.host" -> WiremockHelper.wiremockHost, s"auditing.consumer.baseUri.port" -> WiremockHelper.wiremockPort) +
      ("play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck") +
      ("microservice.services.address-lookup-frontend.new-address-callback.url" -> s"http://${WiremockHelper.wiremockHost}:${WiremockHelper.wiremockPort}") +
      ("microservice.services.vat-registration-eligibility-frontend.uri" -> s"http://${WiremockHelper.wiremockHost}:${WiremockHelper.wiremockPort}/uriELFE") +
      ("microservice.services.vat-registration-eligibility-frontend.question" -> s"/foo") +
      ("microservice.services.vat-registration-frontend.www.url" -> s"http://${WiremockHelper.wiremockHost}:${WiremockHelper.wiremockPort}") +
      ("microservice.services.incorporated-entity-identification-frontend.url" -> s"http://${WiremockHelper.wiremockHost}:${WiremockHelper.wiremockPort}") +
      ("microservice.services.email-verification.url" -> s"http://${WiremockHelper.wiremockHost}:${WiremockHelper.wiremockPort}")

}




