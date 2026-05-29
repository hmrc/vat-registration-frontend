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

package models.bars

import models.api.{IndeterminateStatus, InvalidStatus, ValidStatus}
import models.bars.BarsError._
import models.bars.BarsResponse._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsNumber, JsString, JsSuccess, Json}

class BarsVerificationResponseSpec extends AnyWordSpec with Matchers {

  "BarsResponse reads" should {

    "deserialise to Yes" when {
      "given 'yes'" in {
        JsString("yes").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.Yes)
      }
      "given 'YES' (case-insensitive)" in {
        JsString("YES").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.Yes)
      }
    }

    "deserialise to No" when {
      "given 'no'" in {
        JsString("no").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.No)
      }
    }

    "deserialise to Partial" when {
      "given 'partial'" in {
        JsString("partial").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.Partial)
      }
    }

    "deserialise to Indeterminate" when {
      "given 'indeterminate'" in {
        JsString("indeterminate").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.Indeterminate)
      }
    }

    "deserialise to Inapplicable" when {
      "given 'inapplicable'" in {
        JsString("inapplicable").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.Inapplicable)
      }
    }

    "deserialise to Error" when {
      "given 'error'" in {
        JsString("error").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.Error)
      }
    }

    "return a JsError" when {
      "given an unrecognised string" in {
        JsString("unknown").validate[BarsResponse].isError shouldBe true
      }
      "given a non-string JSON value" in {
        JsNumber(1).validate[BarsResponse].isError shouldBe true
      }
    }
  }

  "BarsVerificationResponse" should {

    "deserialise successfully" when {
      "given a full response with all optional fields present" in {
        val json = Json.parse(
          """{
            |  "accountNumberIsWellFormatted": "yes",
            |  "sortCodeIsPresentOnEISCD": "yes",
            |  "sortCodeBankName": "Test Bank",
            |  "accountExists": "yes",
            |  "nameMatches": "yes",
            |  "sortCodeSupportsDirectDebit": "yes",
            |  "sortCodeSupportsDirectCredit": "yes",
            |  "nonStandardAccountDetailsRequiredForBacs": "no",
            |  "iban": "GB29NWBK60161331926819",
            |  "accountName": "Jane Doe"
            |}""".stripMargin
        )
        json.validate[BarsVerificationResponse].isSuccess shouldBe true
      }

      "given a minimal response with all optional fields absent" in {
        val json = Json.parse(
          """{
            |  "accountNumberIsWellFormatted": "yes",
            |  "sortCodeIsPresentOnEISCD": "yes",
            |  "accountExists": "yes",
            |  "nameMatches": "yes",
            |  "sortCodeSupportsDirectDebit": "yes",
            |  "sortCodeSupportsDirectCredit": "yes"
            |}""".stripMargin
        )
        json.validate[BarsVerificationResponse].isSuccess shouldBe true
      }
    }

    "return a JsError" when {
      "a required field is missing" in {
        val json = Json.parse("""{"accountNumberIsWellFormatted":"yes"}""")
        json.validate[BarsVerificationResponse].isError shouldBe true
      }
    }
  }

  private val successfulResponse = BarsVerificationResponse(
    accountNumberIsWellFormatted = BarsResponse.Yes,
    sortCodeIsPresentOnEISCD = BarsResponse.Yes,
    sortCodeBankName = None,
    accountExists = BarsResponse.Yes,
    nameMatches = BarsResponse.Yes,
    sortCodeSupportsDirectDebit = BarsResponse.Yes,
    sortCodeSupportsDirectCredit = BarsResponse.Yes,
    nonStandardAccountDetailsRequiredForBacs = None,
    iban = None,
    accountName = None
  )

  def barsErrorAndReason(error: BarsError, reasonSnippet: String): BarsErrorAndReason =
    BarsErrorAndReason(error, s"${error.toString} failure: $reasonSnippet")

  "handleVerificationResponse" should {
    "return a ValidStatus and an empty BarsErrorAndReason list" when {
      "the response is successful" in {
        successfulResponse.handleVerificationResponse shouldBe (ValidStatus, Seq.empty[BarsErrorAndReason])
      }

      "nameMatches is Partial but the response is otherwise successful" in {
        val successfulResponseWithPartialNameMatch = successfulResponse.copy(nameMatches = Partial)

        successfulResponseWithPartialNameMatch.handleVerificationResponse shouldBe (ValidStatus, Seq.empty[BarsErrorAndReason])
      }
    }

    "return an IndeterminateStatus and a list of the relevant BarsErrorAndReasons" when {
      "the result is BankAccountUnverified with reason 'accountExists = Indeterminate'" in {
        val bankAccountVerificationFailedResponse = successfulResponse.copy(accountExists = Indeterminate)
        val errorAndReason                        = barsErrorAndReason(BankAccountUnverified, "accountExists = Indeterminate")

        bankAccountVerificationFailedResponse.handleVerificationResponse shouldBe (IndeterminateStatus, Seq(errorAndReason))
      }

      "the result is BankAccountUnverified with reason 'nameMatches = Indeterminate' IF 'accountExists == yes'" in {
        val bankAccountVerificationFailedResponse = successfulResponse.copy(nameMatches = Indeterminate, accountExists = Yes)
        val errorAndReason                        = barsErrorAndReason(BankAccountUnverified, "nameMatches = Indeterminate")

        bankAccountVerificationFailedResponse.handleVerificationResponse shouldBe (IndeterminateStatus, Seq(errorAndReason))
      }
    }

    "return an InvalidStatus and a list of the relevant BarsErrorAndReasons" when {
      "the result is NameMismatch" when {
        "'nameMatches = Indeterminate' AND accountExists is not 'yes'" in {
          val bankAccountVerificationFailedResponse = successfulResponse.copy(nameMatches = Indeterminate, accountExists = Indeterminate)
          val errorAndReason1                       = barsErrorAndReason(BankAccountUnverified, "accountExists = Indeterminate")
          val errorAndReason2                       = barsErrorAndReason(NameMismatch, "nameMatches = Indeterminate")

          bankAccountVerificationFailedResponse.handleVerificationResponse shouldBe (InvalidStatus, Seq(errorAndReason1, errorAndReason2))
        }

        "'nameMatches = Inapplicable'" in {
          val bankAccountVerificationFailedResponse = successfulResponse.copy(nameMatches = Inapplicable)
          val errorAndReason                        = barsErrorAndReason(NameMismatch, "nameMatches = Inapplicable")

          bankAccountVerificationFailedResponse.handleVerificationResponse shouldBe (InvalidStatus, Seq(errorAndReason))
        }

        "'nameMatches = No'" in {
          val bankAccountVerificationFailedResponse = successfulResponse.copy(nameMatches = No)
          val errorAndReason                        = barsErrorAndReason(NameMismatch, "nameMatches = No")

          bankAccountVerificationFailedResponse.handleVerificationResponse shouldBe (InvalidStatus, Seq(errorAndReason))
        }
      }

      "the result is ThirdPartyError" when {
        "'nameMatches = error'" in {
          val bankAccountVerificationFailedResponse = successfulResponse.copy(nameMatches = Error)
          val errorAndReason                        = barsErrorAndReason(ThirdPartyError, "nameMatches = Error")

          bankAccountVerificationFailedResponse.handleVerificationResponse shouldBe (InvalidStatus, Seq(errorAndReason))
        }

        "'accountExists = error'" in {
          val bankAccountVerificationFailedResponse = successfulResponse.copy(accountExists = Error)
          val errorAndReason                        = barsErrorAndReason(ThirdPartyError, "accountExists = Error")

          bankAccountVerificationFailedResponse.handleVerificationResponse shouldBe (InvalidStatus, Seq(errorAndReason))
        }

        "'sortCodeIsPresentOnEISCD = error'" in {
          val bankAccountVerificationFailedResponse = successfulResponse.copy(sortCodeIsPresentOnEISCD = Error)
          val errorAndReason                        = barsErrorAndReason(ThirdPartyError, "sortCodeIsPresentOnEISCD = Error")

          bankAccountVerificationFailedResponse.handleVerificationResponse shouldBe (InvalidStatus, Seq(errorAndReason))
        }
      }

      "the result is AccountNotFound" when {
        "'accountExists = No'" in {
          val bankAccountVerificationFailedResponse = successfulResponse.copy(accountExists = No)
          val errorAndReason                        = barsErrorAndReason(AccountNotFound, "accountExists = No")

          bankAccountVerificationFailedResponse.handleVerificationResponse shouldBe (InvalidStatus, Seq(errorAndReason))
        }

        "'accountExists = Inapplicable'" in {
          val bankAccountVerificationFailedResponse = successfulResponse.copy(accountExists = Inapplicable)
          val errorAndReason                        = barsErrorAndReason(AccountNotFound, "accountExists = Inapplicable")

          bankAccountVerificationFailedResponse.handleVerificationResponse shouldBe (InvalidStatus, Seq(errorAndReason))
        }
      }

      "the result is SortCodeNotFound" when {
        "'sortCodeIsPresentOnEISCD = No'" in {
          val bankAccountVerificationFailedResponse = successfulResponse.copy(sortCodeIsPresentOnEISCD = No)
          val errorAndReason                        = barsErrorAndReason(SortCodeNotFound, "sortCodeIsPresentOnEISCD = No")

          bankAccountVerificationFailedResponse.handleVerificationResponse shouldBe (InvalidStatus, Seq(errorAndReason))
        }
      }
    }
  }

}
