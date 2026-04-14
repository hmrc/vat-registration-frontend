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

import models.bars.BarsError._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsNumber, Json, JsString, JsSuccess}

class BarsVerificationResponseSpec extends AnyWordSpec with Matchers {

  private def responseWith(
      sortCodeIsPresentOnEISCD: BarsResponse = BarsResponse.Yes,
      accountExists: BarsResponse = BarsResponse.Yes,
      nameMatches: BarsResponse = BarsResponse.Yes
  ): BarsVerificationResponse = BarsVerificationResponse(
    accountNumberIsWellFormatted = BarsResponse.Yes,
    sortCodeIsPresentOnEISCD = sortCodeIsPresentOnEISCD,
    sortCodeBankName = None,
    accountExists = accountExists,
    nameMatches = nameMatches,
    sortCodeSupportsDirectDebit = BarsResponse.Yes,
    sortCodeSupportsDirectCredit = BarsResponse.Yes,
    nonStandardAccountDetailsRequiredForBacs = None,
    iban = None,
    accountName = None
  )

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

  "isSuccessful" should {

    "return true" when {
      "all fields pass" in {
        responseWith().isSuccessful shouldBe true
      }
      "nameMatches is Partial" in {
        responseWith(nameMatches = BarsResponse.Partial).isSuccessful shouldBe true
      }
    }

    "return false" when {
      "sortCodeIsPresentOnEISCD is No" in {
        responseWith(sortCodeIsPresentOnEISCD = BarsResponse.No).isSuccessful shouldBe false
      }
      "accountExists is No" in {
        responseWith(accountExists = BarsResponse.No).isSuccessful shouldBe false
      }
      "accountExists is Indeterminate" in {
        responseWith(accountExists = BarsResponse.Indeterminate).isSuccessful shouldBe false
      }
      "accountExists is Inapplicable" in {
        responseWith(accountExists = BarsResponse.Inapplicable).isSuccessful shouldBe false
      }
      "nameMatches is No" in {
        responseWith(nameMatches = BarsResponse.No).isSuccessful shouldBe false
      }
      "nameMatches is Indeterminate" in {
        responseWith(nameMatches = BarsResponse.Indeterminate).isSuccessful shouldBe false
      }
      "nameMatches is Inapplicable" in {
        responseWith(nameMatches = BarsResponse.Inapplicable).isSuccessful shouldBe false
      }
    }
  }

  "check" should {

    "return an empty Seq" when {
      "all fields pass" in {
        responseWith().check shouldBe empty
      }
    }

    "return Seq(SortCodeNotFound)" when {
      "sortCodeIsPresentOnEISCD is No" in {
        val response = responseWith(sortCodeIsPresentOnEISCD = BarsResponse.No)
        response.check shouldBe Seq(SortCodeNotFound)
      }
    }

    "return Seq(AccountNotFound)" when {
      "accountExists is No" in {
        val response = responseWith(accountExists = BarsResponse.No)
        response.check shouldBe Seq(AccountNotFound)
      }
      "accountExists is Inapplicable" in {
        val response = responseWith(accountExists = BarsResponse.Inapplicable)
        response.check shouldBe Seq(AccountNotFound)
      }
    }

    "return Seq(BankAccountUnverified)" when {
      "accountExists is Indeterminate" in {
        val response = responseWith(accountExists = BarsResponse.Indeterminate)
        response.check shouldBe Seq(BankAccountUnverified)
      }
      "nameMatches is Indeterminate and accountExists is Yes" in {
        val response = responseWith(nameMatches = BarsResponse.Indeterminate, accountExists = BarsResponse.Yes)
        response.check shouldBe Seq(BankAccountUnverified)
      }
      "nameMatches is Indeterminate and accountExists is Indeterminate" in {
        val response = responseWith(nameMatches = BarsResponse.Indeterminate, accountExists = BarsResponse.Indeterminate)
        response.check shouldBe Seq(BankAccountUnverified)
      }
    }

    "return Seq(NameMismatch)" when {
      "nameMatches is No" in {
        val response = responseWith(nameMatches = BarsResponse.No)
        response.check shouldBe Seq(NameMismatch)
      }
      "nameMatches is Inapplicable" in {
        val response = responseWith(nameMatches = BarsResponse.Inapplicable)
        response.check shouldBe Seq(NameMismatch)
      }
    }

    "return Seq(ThirdPartyError)" when {
      "sortCodeIsPresentOnEISCD is Error" in {
        responseWith(sortCodeIsPresentOnEISCD = BarsResponse.Error).check shouldBe Seq(ThirdPartyError)
      }
      "accountExists is Error" in {
        responseWith(accountExists = BarsResponse.Error).check shouldBe Seq(ThirdPartyError)
      }
      "nameMatches is Error" in {
        responseWith(nameMatches = BarsResponse.Error).check shouldBe Seq(ThirdPartyError)
      }
    }
  }
}
