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

import itutil.ControllerISpec
import models.view.ApplicantDetails
import models.{TradingDetails, TradingNameView}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class TradingNameControllerISpec extends ControllerISpec {
  val companyName = "testCompanyName"

  "show Trading Name page" should {
    "return OK" in new Setup {
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
    "return SEE_OTHER" in new Setup {
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