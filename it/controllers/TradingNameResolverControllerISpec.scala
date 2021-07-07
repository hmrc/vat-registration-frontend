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

package controllers

import common.enums.VatRegStatus
import itutil.ControllerISpec
import models.api.{Individual, UkCompany, VatScheme}
import models.{ApplicantDetails, TradingDetails}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class TradingNameResolverControllerISpec extends ControllerISpec {
  "Party type resolver" should {
    "return SEE_OTHER and redirects to /former-name" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TradingDetails].isEmpty
        .s4lContainer[ApplicantDetails].isUpdatedWith(validFullApplicantDetails)
        .vatScheme.contains(
          VatScheme(id = currentProfile.registrationId,
            status = VatRegStatus.draft,
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Individual))
          )
        )
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("applicant-details", Json.toJson(validFullApplicantDetails)(ApplicantDetails.apiFormat))
        .vatScheme.doesNotHave("trading-details")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val  response = buildClient("/resolve-party-type").get()
      whenReady(response) {res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.applicant.routes.SoleTraderNameController.show().url)
      }
    }

    "return SEE_OTHER and redirects to /trading-name-not-incorporated" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TradingDetails].isEmpty
        .s4lContainer[ApplicantDetails].isUpdatedWith(validFullApplicantDetails)
        .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = UkCompany))
        )
      )
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("applicant-details", Json.toJson(validFullApplicantDetails)(ApplicantDetails.apiFormat))
        .vatScheme.doesNotHave("trading-details")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val  response = buildClient("/resolve-party-type").get()
      whenReady(response) {res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.TradingNameController.show().url)
      }
    }
  }
}
