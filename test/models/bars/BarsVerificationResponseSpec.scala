package models.bars

import models.bars.BarsError._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsNumber, Json, JsString, JsSuccess}

class BarsVerificationResponseSpec extends AnyWordSpec with Matchers {

  private def responseWith(
      accountNumberIsWellFormatted: BarsResponse = BarsResponse.Yes,
      sortCodeIsPresentOnEISCD: BarsResponse = BarsResponse.Yes,
      accountExists: BarsResponse = BarsResponse.Yes,
      nameMatches: BarsResponse = BarsResponse.Yes,
      sortCodeSupportsDirectDebit: BarsResponse = BarsResponse.Yes
  ): BarsVerificationResponse = BarsVerificationResponse(
    accountNumberIsWellFormatted = accountNumberIsWellFormatted,
    sortCodeIsPresentOnEISCD = sortCodeIsPresentOnEISCD,
    sortCodeBankName = None,
    accountExists = accountExists,
    nameMatches = nameMatches,
    sortCodeSupportsDirectDebit = sortCodeSupportsDirectDebit,
    sortCodeSupportsDirectCredit = BarsResponse.Yes,
    nonStandardAccountDetailsRequiredForBacs = None,
    iban = None,
    accountName = None
  )

  // ---------------------------------------------------------------------------
  // BarsResponse reads
  // ---------------------------------------------------------------------------

  "BarsResponse reads" when {

    "given a valid lowercase string" should {
      "deserialise 'yes' to Yes" in {
        JsString("yes").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.Yes)
      }
      "deserialise 'no' to No" in {
        JsString("no").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.No)
      }
      "deserialise 'partial' to Partial" in {
        JsString("partial").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.Partial)
      }
      "deserialise 'indeterminate' to Indeterminate" in {
        JsString("indeterminate").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.Indeterminate)
      }
      "deserialise 'inapplicable' to Inapplicable" in {
        JsString("inapplicable").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.Inapplicable)
      }
      "deserialise 'error' to Error" in {
        JsString("error").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.Error)
      }
    }

    "given an uppercase string" should {
      "be case-insensitive and deserialise 'YES' to Yes" in {
        JsString("YES").validate[BarsResponse] shouldBe JsSuccess(BarsResponse.Yes)
      }
    }

    "given an unrecognised string" should {
      "return a JsError" in {
        JsString("unknown").validate[BarsResponse].isError shouldBe true
      }
    }

    "given a non-string JSON value" should {
      "return a JsError" in {
        JsNumber(1).validate[BarsResponse].isError shouldBe true
      }
    }
  }

  "BarsVerificationResponse" when {

    "reading from JSON" should {
      "deserialise a full response with all optional fields present" in {
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

      "deserialise a minimal response with all optional fields absent" in {
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

      "return a JsError when a required field is missing" in {
        val json = Json.parse("""{"accountNumberIsWellFormatted":"yes"}""")
        json.validate[BarsVerificationResponse].isError shouldBe true
      }
    }

    "round-tripping through JSON" should {
      "serialise and deserialise back to the same response" in {
        val response = responseWith()
        Json.toJson(response).validate[BarsVerificationResponse].asOpt shouldBe Some(response)
      }
    }
  }

  "isSuccessful" when {

    "all fields pass" should {
      "return true" in {
        responseWith().isSuccessful shouldBe true
      }
    }

    "accountNumberIsWellFormatted is Indeterminate" should {
      "return true — indeterminate is accepted" in {
        responseWith(accountNumberIsWellFormatted = BarsResponse.Indeterminate).isSuccessful shouldBe true
      }
    }

    "nameMatches is Partial" should {
      "return true — partial match is accepted" in {
        responseWith(nameMatches = BarsResponse.Partial).isSuccessful shouldBe true
      }
    }

    "accountNumberIsWellFormatted is No" should {
      "return false" in {
        responseWith(accountNumberIsWellFormatted = BarsResponse.No).isSuccessful shouldBe false
      }
    }

    "sortCodeIsPresentOnEISCD is No" should {
      "return false" in {
        responseWith(sortCodeIsPresentOnEISCD = BarsResponse.No).isSuccessful shouldBe false
      }
    }

    "accountExists is No" should {
      "return false" in {
        responseWith(accountExists = BarsResponse.No).isSuccessful shouldBe false
      }
    }

    "accountExists is Indeterminate" should {
      "return false" in {
        responseWith(accountExists = BarsResponse.Indeterminate).isSuccessful shouldBe false
      }
    }

    "nameMatches is No" should {
      "return false" in {
        responseWith(nameMatches = BarsResponse.No).isSuccessful shouldBe false
      }
    }

    "nameMatches is Indeterminate" should {
      "return false" in {
        responseWith(nameMatches = BarsResponse.Indeterminate).isSuccessful shouldBe false
      }
    }

    "sortCodeSupportsDirectDebit is No" should {
      "return false" in {
        responseWith(sortCodeSupportsDirectDebit = BarsResponse.No).isSuccessful shouldBe false
      }
    }
  }

  "check" when {

    "all fields pass" should {
      "return Right containing the response" in {
        val response = responseWith()
        response.check shouldBe Right(response)
      }
    }

    "accountExists and nameMatches are both No" should {
      "return Left(DetailsVerificationFailed) — short-circuits at the first check" in {
        val response = responseWith(accountExists = BarsResponse.No, nameMatches = BarsResponse.No)
        response.check shouldBe Left(DetailsVerificationFailed)
      }
    }

    "accountNumberIsWellFormatted is No" should {
      "return Left(AccountDetailInvalidFormat)" in {
        val response = responseWith(accountNumberIsWellFormatted = BarsResponse.No)
        response.check shouldBe Left(AccountDetailInvalidFormat)
      }
    }

    "sortCodeIsPresentOnEISCD is No" should {
      "return Left(SortCodeNotFound)" in {
        val response = responseWith(sortCodeIsPresentOnEISCD = BarsResponse.No)
        response.check shouldBe Left(SortCodeNotFound)
      }
    }

    "sortCodeSupportsDirectDebit is No" should {
      "return Left(SortCodeNotSupported)" in {
        val response = responseWith(sortCodeSupportsDirectDebit = BarsResponse.No)
        response.check shouldBe Left(SortCodeNotSupported)
      }
    }

    "accountExists is No" should {
      "return Left(AccountNotFound)" in {
        val response = responseWith(accountExists = BarsResponse.No)
        response.check shouldBe Left(AccountNotFound)
      }
    }

    "accountExists is Indeterminate" should {
      "return Left(BankAccountUnverified)" in {
        val response = responseWith(accountExists = BarsResponse.Indeterminate)
        response.check shouldBe Left(BankAccountUnverified)
      }
    }

    "nameMatches is No" should {
      "return Left(NameMismatch)" in {
        val response = responseWith(nameMatches = BarsResponse.No)
        response.check shouldBe Left(NameMismatch)
      }
    }

    "nameMatches is Indeterminate and accountExists is Yes" should {
      "return Left(BankAccountUnverified)" in {
        val response = responseWith(nameMatches = BarsResponse.Indeterminate, accountExists = BarsResponse.Yes)
        response.check shouldBe Left(BankAccountUnverified)
      }
    }

    "nameMatches is Indeterminate and accountExists is not Yes" should {
      "return Left(BankAccountUnverified)" in {
        val response = responseWith(nameMatches = BarsResponse.Indeterminate, accountExists = BarsResponse.Indeterminate)
        response.check shouldBe Left(BankAccountUnverified)
      }
    }
  }
}
