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

import featureswitch.core.config.ShortOrgName
import itutil.ControllerISpec
import models.api._
import models.{ApplicantDetails, TradingDetails}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class TradingNameResolverControllerISpec extends ControllerISpec {

  "Trading name page resolver" should {
    List(Individual, NETP).foreach { partyType =>
      s"return SEE_OTHER and redirects to ${controllers.registration.business.routes.MandatoryTradingNameController.show.url} for ${partyType.toString}" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[TradingDetails].isEmpty
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = partyType)))
          .vatScheme.has("applicant-details", Json.toJson(validFullApplicantDetails)(ApplicantDetails.writes))
          .vatScheme.doesNotHave("trading-details")

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response = buildClient("/resolve-party-type").get()
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.MandatoryTradingNameController.show.url)
        }
      }
    }

    List(Partnership, ScotPartnership).foreach { partyType =>
      s"return SEE_OTHER and redirects to ${controllers.registration.business.routes.PartnershipNameController.show.url} for ${partyType.toString}" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[TradingDetails].isEmpty
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = partyType)))
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatScheme.has("applicant-details", Json.toJson(validFullApplicantDetails)(ApplicantDetails.writes))
          .vatScheme.doesNotHave("trading-details")

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response = buildClient("/resolve-party-type").get()
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.PartnershipNameController.show.url)
        }
      }
    }

    s"return SEE_OTHER and redirects to ${controllers.registration.business.routes.ShortOrgNameController.show.url} for a ${UkCompany.toString} with a company name longer than 105" in new Setup {
      val longCompanyName: String = "1" * 106

      enable(ShortOrgName)

      given()
        .user.isAuthorised()
        .s4lContainer[TradingDetails].isEmpty
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = UkCompany)))
        .vatScheme.has("applicant-details", Json.toJson(validFullApplicantDetails.copy(entity =
        Some(testApplicantIncorpDetails.copy(companyName = Some(longCompanyName)))
      ))(ApplicantDetails.writes))
        .vatScheme.doesNotHave("trading-details")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/resolve-party-type").get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.ShortOrgNameController.show.url)
      }
    }

    List(UkCompany, RegSociety, CharitableOrg).foreach { partyType =>
      s"return SEE_OTHER and redirects to ${controllers.registration.business.routes.TradingNameController.show.url} for ${partyType.toString}" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[TradingDetails].isEmpty
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = partyType)))
          .vatScheme.has("applicant-details", Json.toJson(validFullApplicantDetails)(ApplicantDetails.writes))
          .vatScheme.doesNotHave("trading-details")

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response = buildClient("/resolve-party-type").get()
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.TradingNameController.show.url)
        }
      }
    }

    List(Trust, UnincorpAssoc, NonUkNonEstablished).foreach { partyType =>
      s"return SEE_OTHER and redirects to ${controllers.registration.business.routes.BusinessNameController.show.url} for ${partyType.toString}" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[TradingDetails].isEmpty
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = partyType)))
          .vatScheme.has("applicant-details", Json.toJson(validFullApplicantDetails.copy(entity = Some(testMinorEntity)))(ApplicantDetails.writes))
          .vatScheme.doesNotHave("trading-details")

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response = buildClient("/resolve-party-type").get()
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.BusinessNameController.show.url)
        }
      }
    }
  }
}
