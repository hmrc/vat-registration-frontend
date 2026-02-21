/*
 * Copyright 2026 HM Revenue & Customs
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
import models.bars._
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import support.AppAndStubs


class BarsConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val connector: BarsConnector = app.injector.instanceOf[BarsConnector]

  val stubbedBarsSuccessResponse: BarsVerificationResponse = BarsVerificationResponse(
    accountNumberIsWellFormatted = BarsResponse.Yes,
    sortCodeIsPresentOnEISCD = BarsResponse.Yes,
    sortCodeBankName = Some("Test Bank"),
    accountExists = BarsResponse.Yes,
    nameMatches = BarsResponse.Yes,
    sortCodeSupportsDirectDebit = BarsResponse.Yes,
    sortCodeSupportsDirectCredit = BarsResponse.Yes,
    nonStandardAccountDetailsRequiredForBacs = None,
    iban = None,
    accountName = None
  )

  def requestJson(bankAccountType: BankAccountType): JsValue = bankAccountType match {
    case BankAccountType.Personal =>
      Json.toJson(BarsPersonalRequest(BarsAccount(testSortCode, testAccountNumber), BarsSubject(testBankName)))
    case BankAccountType.Business =>
      Json.toJson(BarsBusinessRequest(BarsAccount(testSortCode, testAccountNumber), BarsBusiness(testBankName)))
  }

  "BarsConnector.verify" when {

    Seq(BankAccountType.Personal, BankAccountType.Business).foreach { bankAccountType =>

      s"account type is ${bankAccountType.asBars}" when {

        "BARS returns OK" must {
          "return the deserialised BarsVerificationResponse" in new Setup {
            given()
              .user.isAuthorised()
              .bars.verifySucceeds(bankAccountType)

            insertCurrentProfileIntoDb(currentProfile, sessionString)

            val result: BarsVerificationResponse = await(connector.verify(bankAccountType, requestJson(bankAccountType)))

            result mustBe stubbedBarsSuccessResponse
          }
        }

        "BARS returns a 400" must {
          "throw an UpstreamBarsException with status 400" in new Setup {
            given()
              .user.isAuthorised()
              .bars.verifyFails(bankAccountType, 400)

            insertCurrentProfileIntoDb(currentProfile, sessionString)

            val ex: UpstreamBarsException = intercept[UpstreamBarsException] {
              await(connector.verify(bankAccountType, requestJson(bankAccountType)))
            }

            ex.status mustBe 400
          }
        }

        "BARS returns a 500" must {
          "throw an UpstreamBarsException with status 500" in new Setup {
            given()
              .user.isAuthorised()
              .bars.isDown(bankAccountType)

            insertCurrentProfileIntoDb(currentProfile, sessionString)

            val ex: UpstreamBarsException = intercept[UpstreamBarsException] {
              await(connector.verify(bankAccountType, requestJson(bankAccountType)))
            }

            ex.status mustBe 500
          }
        }
      }
    }
  }
}