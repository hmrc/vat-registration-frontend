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

package services

import connectors.BarsConnector
import models.BankAccountDetails
import models.api.{IndeterminateStatus, InvalidStatus, ValidStatus}
import models.bars.BarsErrors._
import models.bars.BarsResponse.Indeterminate
import models.bars._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BarsServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockConnector = mock[BarsConnector]
  private val service = BarsService(mockConnector)


  private val personalDetails = BankAccountDetails(
    sortCode = "123456",
    number = "12345678",
    name = "Jane Doe"
  )

  private val businessDetails = BankAccountDetails(
    sortCode = "654321",
    number = "87654321",
    name = "Acme Corp"
  )

  private def successResponse: BarsVerificationResponse = BarsVerificationResponse(
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

  private def barsResponseWith(
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


  "verifyBankDetails" when {

    "the connector returns a successful BARS response" should {
      "return ValidStatus" in {
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(successResponse))

        service.verifyBankDetails(BankAccountType.Personal, personalDetails).futureValue shouldBe ValidStatus
      }
    }

    "the connector returns a response where the account cannot be verified" should {
      "return IndeterminateStatus" in {
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponseWith(accountExists = BarsResponse.Indeterminate)))

        service.verifyBankDetails(BankAccountType.Personal, personalDetails).futureValue shouldBe IndeterminateStatus
      }
    }

    "the connector returns a response indicating a bad sort code or account" should {
      "return InvalidStatus" in {
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponseWith(sortCodeIsPresentOnEISCD = BarsResponse.No)))

        service.verifyBankDetails(BankAccountType.Personal, personalDetails).futureValue shouldBe InvalidStatus
      }
    }

    "the connector throws an exception" should {
      "return InvalidStatus" in {
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.failed(new RuntimeException("failure")))

        service.verifyBankDetails(BankAccountType.Personal, personalDetails).futureValue shouldBe InvalidStatus
      }
    }
  }

  "checkVerificationResult" when {

    "the response is successful" should {
      "return Right containing the response" in {
        service.checkVerificationResult(successResponse) shouldBe Right(successResponse)
      }
    }

    "the name matches partially" should {
      "return Right (this is successful verification)" in {
        val response = successResponse.copy(nameMatches = BarsResponse.Partial)
        service.checkVerificationResult(response) shouldBe Right(response)
      }
    }

    "the accountNumberIsWellFormatted value is indeterminate" should {
      "return right (this is succesful verification)" in {
        val response = successResponse.copy(accountNumberIsWellFormatted = BarsResponse.Indeterminate)
        service.checkVerificationResult(response) shouldBe Right(response)
      }
    }

    "the account number is not well formatted" should {
      "return Left(AccountDetailInvalidFormat)" in {
        val response = barsResponseWith(accountNumberIsWellFormatted = BarsResponse.No)
        service.checkVerificationResult(response) shouldBe Left(AccountDetailInvalidFormat)
      }
    }

    "the sort code is not present on EISCD" should {
      "return Left(SortCodeNotFound)" in {
        val response = barsResponseWith(sortCodeIsPresentOnEISCD = BarsResponse.No)
        service.checkVerificationResult(response) shouldBe Left(SortCodeNotFound)
      }
    }

    "the sort code does not support direct debit" should {
      "return Left(SortCodeNotSupported)" in {
        val response = barsResponseWith(sortCodeSupportsDirectDebit = BarsResponse.No)
        service.checkVerificationResult(response) shouldBe Left(SortCodeNotSupported)
      }
    }

    "the account does not exist" should {
      "return Left(AccountNotFound)" in {
        val response = barsResponseWith(accountExists = BarsResponse.No)
        service.checkVerificationResult(response) shouldBe Left(AccountNotFound)
      }
    }

    "the account existence is indeterminate" should {
      "return Left(BankAccountUnverified)" in {
        val response = barsResponseWith(accountExists = BarsResponse.Indeterminate)
        service.checkVerificationResult(response) shouldBe Left(BankAccountUnverified)
      }
    }

    "the name does not match" should {
      "return Left(NameMismatch)" in {
        val response = barsResponseWith(nameMatches = BarsResponse.No)
        service.checkVerificationResult(response) shouldBe Left(NameMismatch)
      }
    }

    "the name match is indeterminate and the account exists" should {
      "return Left(BankAccountUnverified)" in {
        val response = barsResponseWith(nameMatches = BarsResponse.Indeterminate, accountExists = BarsResponse.Yes)
        service.checkVerificationResult(response) shouldBe Left(BankAccountUnverified)
      }
    }
  }


  "handleResponse" when {

    "given Right(_)" should {
      "return ValidStatus" in {
        service.handleResponse(Right(successResponse)) shouldBe ValidStatus
      }
    }

    "given Left(BankAccountUnverified)" should {
      "return IndeterminateStatus" in {
        service.handleResponse(Left(BankAccountUnverified)) shouldBe IndeterminateStatus
      }
    }

    "given any other Left error" should {
      "return InvalidStatus for AccountDetailInvalidFormat" in {
        service.handleResponse(Left(AccountDetailInvalidFormat)) shouldBe InvalidStatus
      }
      "return InvalidStatus for SortCodeNotFound" in {
        service.handleResponse(Left(SortCodeNotFound)) shouldBe InvalidStatus
      }
      "return InvalidStatus for SortCodeNotSupported" in {
        service.handleResponse(Left(SortCodeNotSupported)) shouldBe InvalidStatus
      }
      "return InvalidStatus for AccountNotFound" in {
        service.handleResponse(Left(AccountNotFound)) shouldBe InvalidStatus
      }
      "return InvalidStatus for NameMismatch" in {
        service.handleResponse(Left(NameMismatch)) shouldBe InvalidStatus
      }
      "return InvalidStatus for SortCodeOnDenyList" in {
        service.handleResponse(Left(SortCodeOnDenyList)) shouldBe InvalidStatus
      }
      "return InvalidStatus for DetailsVerificationFailed" in {
        service.handleResponse(Left(DetailsVerificationFailed)) shouldBe InvalidStatus
      }
    }
  }

  "buildJsonRequestBody" when {

    "given a Personal account type" should {
      "produce a JSON body with 'account' and 'subject' keys" in {
        val result = service.buildJsonRequestBody(BankAccountType.Personal, personalDetails)

        result shouldBe Json.toJson(
          BarsPersonalRequest(
            BarsAccount(personalDetails.sortCode, personalDetails.number),
            BarsSubject(personalDetails.name)
          )
        )
      }

      "not include a 'business' key" in {
        val result = service.buildJsonRequestBody(BankAccountType.Personal, personalDetails)
        (result \ "business").isDefined shouldBe false
      }
    }

    "given a Business account type" should {
      "produce a JSON body with 'account' and 'business' keys" in {
        val result = service.buildJsonRequestBody(BankAccountType.Business, businessDetails)

        result shouldBe Json.toJson(
          BarsBusinessRequest(
            BarsAccount(businessDetails.sortCode, businessDetails.number),
            BarsBusiness(businessDetails.name)
          )
        )
      }

      "not include a 'subject' key" in {
        val result = service.buildJsonRequestBody(BankAccountType.Business, businessDetails)
        (result \ "subject").isDefined shouldBe false
      }
    }
  }
}