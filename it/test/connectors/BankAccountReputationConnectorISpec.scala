/*
 * Copyright 2024 HM Revenue & Customs
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

import itFixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import support.AppAndStubs
import uk.gov.hmrc.http.InternalServerException
import play.api.mvc.Request

class BankAccountReputationConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val connector = app.injector.instanceOf[BankAccountReputationConnector]

  val stubbedBarsSuccessResponse = Json.obj(
   "accountNumberIsWellFormatted" -> "yes",
   "nonStandardAccountDetailsRequiredForBacs" -> "no"
  )

  "validateBankDetails" when {
    "BARS returns OK" must {
      "return the JSON response" in new Setup {
        given()
          .user.isAuthorised()
          .bankAccountReputation.passes

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(connector.validateBankDetails(testUkBankDetails))

        res mustBe stubbedBarsSuccessResponse
      }
    }
    "BARS returns another status" must {
      "throw an internal server exception" in new Setup {
        given()
          .user.isAuthorised()
          .bankAccountReputation.isDown

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        intercept[InternalServerException] {
          await(connector.validateBankDetails(testUkBankDetails))
        }
      }
    }
  }

}
