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

package controllers

import java.time.LocalDate

import featureswitch.core.config.{FeatureSwitching, StubPersonalDetailsValidation}
import itutil.IntegrationSpecBase
import org.scalatest.concurrent.IntegrationPatience
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{await, _}
import support.AppAndStubs

class PersonalDetailsValidationControllerISpec extends IntegrationSpecBase with AppAndStubs with FeatureSwitching with IntegrationPatience {
  "GET /start-personal-details-validation-journey" should {
    "redirect to the personal details validation service" in new StandardTestHelpers {
      disable(StubPersonalDetailsValidation)

      given()
        .user.isAuthorised
        .audit.writesAudit()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(controllers.routes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney().url).get)

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some("http://localhost:9968/personal-details-validation/start?completionUrl=http://localhost:11111/register-for-vat/personal-details-validation-callback")

    }
  }

  "GET /personal-details-validation-callback" should {
    "retrieve the captured transactor details and play them back" in new StandardTestHelpers {
      disable(StubPersonalDetailsValidation)

      val testValidationId: String = "testValidationId"

      given()
        .user.isAuthorised
        .audit.writesAudit()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val testTransactorDetails: JsObject = Json.obj(
        "personalDetails" -> Json.obj(
          "firstName" -> "testFirstName",
          "lastName" -> "testLastName",
          "nino" -> "AA123456A",
          "dateOfBirth" -> LocalDate.of(1990, 1, 1)
        )
      )

      stubGet(s"/personal-details-validation/$testValidationId", OK, testTransactorDetails.toString)

      val res: WSResponse = await(buildClient(controllers.routes.PersonalDetailsValidationController.personalDetailsValidationCallback(testValidationId).url).get)

      res.status mustBe OK
    }
  }
}
