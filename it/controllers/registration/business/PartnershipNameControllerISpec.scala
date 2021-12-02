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
import models.ApplicantDetails
import models.api.EligibilitySubmissionData
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PartnershipNameControllerISpec extends ControllerISpec {
  val partnershipName = "testPartnershipName"

  "show Partnership Name page" should {
    "return OK" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[ApplicantDetails].isEmpty
        .vatScheme.has("applicant-details", Json.toJson(validFullApplicantDetails)(ApplicantDetails.writes))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/partnership-official-name").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  "submit Partnership Name page" should {
    "return SEE_OTHER" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.has("applicant-details", Json.toJson(validFullApplicantDetails)(ApplicantDetails.writes))
        .vatScheme.isUpdatedWith(
        validFullApplicantDetails.copy(
          entity = Some(testPartnership.copy(
            companyName = Some(partnershipName)
          ))
        ))
        .s4lContainer[ApplicantDetails].isEmpty
        .s4lContainer[ApplicantDetails].clearedByKey
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionDataPartner))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/partnership-official-name").post(Map("partnershipName" -> Seq(partnershipName)))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.MandatoryTradingNameController.show.url)
      }
    }
  }
}