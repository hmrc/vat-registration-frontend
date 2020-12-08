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

package controllers.registration.business

import helpers.RequestsFinder
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.view.ApplicantDetails
import models.{TradingDetails, TradingNameView}
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import repositories.SessionRepository
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap
import play.api.test.Helpers._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class TradingNameControllerISpec extends IntegrationSpecBase with AppAndStubs with ScalaFutures with RequestsFinder with ITRegistrationFixtures {
  val companyName = "testCompanyName"

  class Setup {
    def customAwait[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)

    val repo = app.injector.instanceOf[SessionRepository]
    val defaultTimeout: FiniteDuration = 5 seconds

    customAwait(repo.ensureIndexes)(defaultTimeout)
    customAwait(repo.drop)(defaultTimeout)

    def insertCurrentProfileIntoDb(currentProfile: models.CurrentProfile, sessionId: String): Boolean = {
      val preawait = customAwait(repo.count)(defaultTimeout)
      val currentProfileMapping: Map[String, JsValue] = Map("CurrentProfile" -> Json.toJson(currentProfile))
      val res = customAwait(repo.upsert(CacheMap(sessionId, currentProfileMapping)))(defaultTimeout)
      customAwait(repo.count)(defaultTimeout) mustBe preawait + 1
      res
    }
  }

  "show Trading Name page" should {
    "return 200" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TradingDetails].isEmpty
        .s4lContainer[ApplicantDetails].isUpdatedWith(validFullApplicantDetails)
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("applicant-details", Json.toJson(validFullApplicantDetails)(ApplicantDetails.apiFormat))
        .vatScheme.doesNotHave("trading-details")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/trading-name").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  "submit Trading Name page" should {
    "return 303" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TradingDetails].contains(tradingDetails)
        .vatScheme.doesNotHave("trading-details")
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("applicant-details", Json.toJson(validFullApplicantDetails))
        .vatScheme.isUpdatedWith(tradingDetails.copy(tradingNameView = Some(TradingNameView(true, Some("Test Trading Name")))))
        .s4lContainer[TradingDetails].cleared

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/trading-name").post(Map("value" -> Seq("true"), "tradingName" -> Seq("Test Trading Name")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.EuGoodsController.show().url)
      }
    }
  }
}