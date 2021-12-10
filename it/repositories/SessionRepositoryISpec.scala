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

import common.enums.VatRegStatus
import itutil.IntegrationSpecBase
import models.CurrentProfile._
import play.api.libs.json.{JsObject, Json, OWrites}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import java.util.UUID
import play.api.test.Helpers._
import services.SessionService
import support.AppAndStubs

import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositoryISpec extends IntegrationSpecBase with AppAndStubs {

  val sId = UUID.randomUUID().toString

  override implicit val hc = HeaderCarrier(sessionId = Some(SessionId(sId)))

  class Setup {
    val repository = app.injector.instanceOf[SessionRepository]

    val connector = app.injector.instanceOf[SessionService]
    await(repository.drop)
    await(repository.ensureIndexes)

    implicit val jsObjWts: OWrites[JsObject] = OWrites(identity)

    def count = await(repository.count)
  }

  "SessionRepository" should {
    "cache" when {
      "given a new currentProfile" in new Setup() {
        count mustBe 0
        await(connector.cache("CurrentProfile", currentProfile))
        count mustBe 1
      }
      "given an existing currentProfile" in new Setup() {
        await(connector.cache("CurrentProfile", currentProfile))
        count mustBe 1
        await(connector.cache("CurrentProfile", models.CurrentProfile("newregId",  VatRegStatus.draft)))
        count mustBe 1
      }
    }
    "fetch" when {
      "given a currentProfile exists" in new Setup() {
        val currentProfileData: models.CurrentProfile = models.CurrentProfile("regId2",  VatRegStatus.draft)
        val key: String = "CurrentProfile"

        await(connector.cache(key, currentProfileData))

        val res: Option[CacheMap] = await(connector.fetch)
        res.isDefined mustBe true
        res.get.data mustBe Map(key -> Json.toJson(currentProfileData))
      }
    }
    "fetchAndGet" when {
      "given a currentProfile and key" in new Setup() {
        val currentProfileData: models.CurrentProfile = models.CurrentProfile("regId3",  VatRegStatus.draft)
        val key: String = "CurrentProfile"

        await(connector.cache(key, currentProfileData))

        val res: Option[models.CurrentProfile] = await(connector.fetchAndGet(key)(hc, models.CurrentProfile.format))
        res.isDefined mustBe true
        res.get mustBe currentProfileData
      }
      "given no current profile" in new Setup() {
        val key: String = "CurrentProfile"

        val res: Option[models.CurrentProfile] = await(connector.fetchAndGet(key)(hc, models.CurrentProfile.format))
        res.isDefined mustBe false
      }
    }
    "remove" when {
      "there is a current profile to remove" in new Setup() {
        await(connector.cache("CurrentProfile", currentProfile))
        count mustBe 1

        val res: Boolean = await(connector.remove)
        res mustBe true
        count mustBe 0
      }
      "there is no current profile to remove" in new Setup() {
        val res: Boolean = await(connector.remove)

        res mustBe false
        count mustBe 0
      }
      "there are two current profiles" in new Setup() {
        val hc1 = hc.copy(sessionId = Some(SessionId("id1")))

        await(connector.cache("CurrentProfile", currentProfile)(hc1, models.CurrentProfile.format))
        await(connector.cache("CurrentProfile", currentProfile)(hc.copy(sessionId = Some(SessionId("id2"))), models.CurrentProfile.format))
        count mustBe 2

        val res: Boolean = await(connector.remove(hc1))
        res mustBe true
        count mustBe 1
      }
    }
  }
}
