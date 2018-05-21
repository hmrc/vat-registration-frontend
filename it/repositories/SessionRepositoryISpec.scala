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

package repositories

import java.time.LocalDate
import java.util.UUID

import common.enums.VatRegStatus
import connectors.KeystoreConnector
import itutil.{IntegrationSpecBase, WiremockHelper}
import models.CurrentProfile
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json, OWrites}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.mongo.MongoSpecSupport

import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositoryISpec extends IntegrationSpecBase with MongoSpecSupport {
  val mockHost = WiremockHelper.wiremockHost
  val mockPort = WiremockHelper.wiremockPort
  val mockUrl = s"http://$mockHost:$mockPort"

  val additionalConfiguration = Map(
    "microservice.services.vat-registration.host" -> s"$mockHost",
    "microservice.services.vat-registration.port" -> s"$mockPort",
    "microservice.services.cachable.session-cache.host" -> s"$mockHost",
    "microservice.services.cachable.session-cache.port" -> s"$mockPort",
    "application.router" -> "testOnlyDoNotUseInAppConf.Routes",
    "microservice.services.incorporation-information.uri" -> "/test-incorporation-information",
    "microservice.services.incorporation-information.host" -> s"$mockHost",
    "microservice.services.incorporation-information.port" -> s"$mockPort",
    "mongodb.uri" -> s"$mongoUri"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(additionalConfiguration)
    .build

  val sId = UUID.randomUUID().toString
  implicit val hc = HeaderCarrier(sessionId = Some(SessionId(sId)))

  def currentProfile(regId: String) = CurrentProfile(
    companyName = "TestCompanyName",
    registrationId = regId,
    transactionId = "40-123456",
    vatRegistrationStatus = VatRegStatus.draft,
    incorporationDate = Some(LocalDate.of(2017, 11, 27)),
    ivPassed = Some(true)
  )

  class Setup {
    val repository = new ReactiveMongoRepository(app.configuration, mongo)

    val connector = app.injector.instanceOf[KeystoreConnector]
    await(repository.drop)
    await(repository.ensureIndexes)

    implicit val jsObjWts: OWrites[JsObject] = OWrites(identity)

    def count = await(repository.count)
  }

  "SessionRepository" should {
    "cache" when {
      "given a new currentProfile" in new Setup(){
        count shouldBe 0
        await(connector.cache("CurrentProfile", currentProfile("regId")))
        count shouldBe 1
      }
      "given an existing currentProfile" in new Setup(){
        await(connector.cache("CurrentProfile", currentProfile("regId")))
        count shouldBe 1
        await(connector.cache("CurrentProfile", currentProfile("newregId")))
        count shouldBe 1
      }
    }
    "fetch" when {
      "given a currentProfile exists" in new Setup(){
        val currentProfileData: CurrentProfile = currentProfile("regId2").copy(incorpRejected = Some(true))
        val key: String = "CurrentProfile"

        await(connector.cache(key, currentProfileData))

        val res: Option[CacheMap] = await(connector.fetch)
        res.isDefined shouldBe true
        res.get.data shouldBe Map(key -> Json.toJson(currentProfileData))
      }
    }
    "addRejectionFlag" when {
      "given a currentProfile exists" in new Setup(){
        val currentProfileData: CurrentProfile = currentProfile("regId2")
        val key: String = "CurrentProfile"

        val expectedResult = CacheMap(sId, Map("CurrentProfile" ->
          Json.toJson(currentProfileData.copy(incorpRejected = Some(true))))
        )

        await(connector.cache(key, currentProfileData))

        await(connector.addRejectionFlag("40-123456")) shouldBe Some("regId2")

        await(connector.fetch(hc)).get shouldBe expectedResult
      }
    }
    "fetchAndGet" when {
      "given a currentProfile and key" in new Setup(){
        val currentProfileData: CurrentProfile = currentProfile("regId3")
        val key: String = "CurrentProfile"

        await(connector.cache(key, currentProfileData))

        val res: Option[CurrentProfile] = await(connector.fetchAndGet(key)(hc, CurrentProfile.format))
        res.isDefined shouldBe true
        res.get shouldBe currentProfileData
      }
      "given no current profile" in new Setup(){
        val key: String = "CurrentProfile"

        val res: Option[CurrentProfile] = await(connector.fetchAndGet(key)(hc, CurrentProfile.format))
        res.isDefined shouldBe false
      }
    }
    "remove" when {
      "there is a current profile to remove" in new Setup() {
        await(connector.cache("CurrentProfile", currentProfile("regId")))
        count shouldBe 1

        val res: Boolean = await(connector.remove)
        res shouldBe true
        count shouldBe 0
      }
      "there is no current profile to remove" in new Setup() {
        val res: Boolean = await(connector.remove)

        res shouldBe false
        count shouldBe 0
      }
      "there are two current profiles" in new Setup(){
        val hc1 = hc.copy(sessionId = Some(SessionId("id1")))

        await(connector.cache("CurrentProfile", currentProfile("regId"))(hc1, CurrentProfile.format))
        await(connector.cache("CurrentProfile", currentProfile("regId"))(hc.copy(sessionId = Some(SessionId("id2"))), CurrentProfile.format))
        count shouldBe 2

        val res: Boolean = await(connector.remove(hc1))
        res shouldBe true
        count shouldBe 1
      }
    }
  }
}
