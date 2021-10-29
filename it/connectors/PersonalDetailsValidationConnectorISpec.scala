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

package connectors

import featureswitch.core.config.{FeatureSwitching, StubPersonalDetailsValidation}
import itutil.IntegrationSpecBase
import models.PersonalDetails
import play.api.libs.json.Json
import play.api.test.Helpers._
import support.AppAndStubs

import java.time.LocalDate

class PersonalDetailsValidationConnectorISpec extends IntegrationSpecBase with AppAndStubs with FeatureSwitching {
  "retrieveValidationResult" when {
    "the stub personal details validation feature switch is disabled" should {
      "return the captured details from personal details validation" in {
        disable(StubPersonalDetailsValidation)
        val testValidationId: String = "testValidationId"

        val testFirstName = "testFirstName"
        val testLastName = "testLastName"
        val testNino = "AA123456A"
        val testRole = "testRole"
        val testDateOfBirth = LocalDate.of(1990, 1, 1)

        val testJsonBody = Json.obj(
          "personalDetails" -> Json.obj(
            "firstName" -> testFirstName,
            "lastName" -> testLastName,
            "nino" -> testNino,
            "dateOfBirth" -> testDateOfBirth,
            "role" -> testRole
          )
        )

        stubGet(s"/personal-details-validation/$testValidationId", OK, testJsonBody.toString)

        val connector = app.injector.instanceOf[PersonalDetailsValidationConnector]

        val res = connector.retrieveValidationResult(testValidationId)

        val expectedData = PersonalDetails(
          testFirstName,
          testLastName,
          Some(testNino),
          None,
          identifiersMatch = true,
          testDateOfBirth
        )

        await(res) mustBe expectedData
      }
    }
    "the stub personal details validation feature switch is enabled" should {
      "return the test data from the stub API" in {
        enable(StubPersonalDetailsValidation)
        val testValidationId: String = "testValidationId"

        val testFirstName = "testFirstName"
        val testLastName = "testLastName"
        val testNino = "AA123456A"
        val testRole = "testRole"
        val testDateOfBirth = LocalDate.of(1990, 1, 1)

        val testJsonBody = Json.obj(
          "personalDetails" -> Json.obj(
            "firstName" -> testFirstName,
            "lastName" -> testLastName,
            "nino" -> testNino,
            "dateOfBirth" -> testDateOfBirth,
            "role" -> testRole
          )
        )

        stubGet(s"/register-for-vat/test-only/personal-details-validation/$testValidationId", OK, testJsonBody.toString)

        val connector = app.injector.instanceOf[PersonalDetailsValidationConnector]

        val res = connector.retrieveValidationResult(testValidationId)

        val expectedData = PersonalDetails(
          testFirstName,
          testLastName,
          Some(testNino),
          None,
          identifiersMatch = true,
          testDateOfBirth
        )

        await(res) mustBe expectedData
      }
    }
  }

}
