/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.registration.applicant

import controllers.registration.applicant.{routes => applicantRoutes}
import featureswitch.core.config.StubPersonalDetailsValidation
import itutil.ControllerISpec
import models.view.ApplicantDetails
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{await, _}

class PersonalDetailsValidationControllerISpec extends ControllerISpec {
  "GET /start-personal-details-validation-journey" should {
    "redirect to the personal details validation service" in new Setup {
      disable(StubPersonalDetailsValidation)

      given()
        .user.isAuthorised
        .audit.writesAudit()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney().url).get)

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some("http://localhost:9968/personal-details-validation/start?completionUrl=http://localhost:11111/register-for-vat/personal-details-validation-callback")

    }
  }

  "GET /personal-details-validation-callback" should {
    "retrieve the captured transactor details and redirect" in new Setup {
      disable(StubPersonalDetailsValidation)

      val testValidationId: String = "testValidationId"
      val applicantJson = Json.toJson(validFullApplicantDetails)(ApplicantDetails.apiWrites)

      given()
        .user.isAuthorised
        .audit.writesAudit()
        .vatScheme.has("applicant-details", applicantJson)
        .vatScheme.patched("applicant-details", applicantJson)
        .s4lContainer[ApplicantDetails].isUpdatedWith(validFullApplicantDetails)
        .s4lContainer[ApplicantDetails].cleared

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      stubGet(s"/personal-details-validation/$testValidationId", OK, Json.obj("personalDetails" -> Json.toJson(testTransactorDetails)).toString)

      val res: WSResponse = await(buildClient(applicantRoutes.PersonalDetailsValidationController.personalDetailsValidationCallback(testValidationId).url).get)

      res.status mustBe SEE_OTHER
    }
  }
}
