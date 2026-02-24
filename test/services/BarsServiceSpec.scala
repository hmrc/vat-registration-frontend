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
import models.bars._
import models.bars.BarsError._
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
  private val service       = BarsService(mockConnector)

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

  "verifyBankDetails" should {

    "return ValidStatus" when {
      "the connector returns a successful BARS response" in {
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(successResponse))

        service.verifyBankDetails(BankAccountType.Personal, personalDetails).futureValue shouldBe ValidStatus
      }
    }

    "return IndeterminateStatus" when {
      "the connector returns a response where the account cannot be verified" in {
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponseWith(accountExists = BarsResponse.Indeterminate)))

        service.verifyBankDetails(BankAccountType.Personal, personalDetails).futureValue shouldBe IndeterminateStatus
      }
    }

    "return InvalidStatus" when {
      "the connector returns a response indicating a bad sort code or account" in {
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponseWith(sortCodeIsPresentOnEISCD = BarsResponse.No)))

        service.verifyBankDetails(BankAccountType.Personal, personalDetails).futureValue shouldBe InvalidStatus
      }

      "the connector throws an exception" in {
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.failed(new RuntimeException("failure")))

        service.verifyBankDetails(BankAccountType.Personal, personalDetails).futureValue shouldBe InvalidStatus
      }
    }
  }

  "checkVerificationResult" should {

    "return Right containing the response" when {
      "the response is successful" in {
        service.checkVerificationResult(successResponse) shouldBe Right(successResponse)
      }

      "the name is a partial match" in {
        val response = successResponse.copy(nameMatches = BarsResponse.Partial)
        service.checkVerificationResult(response) shouldBe Right(response)
      }

      "the account number is indeterminate" in {
        val response = successResponse.copy(accountNumberIsWellFormatted = BarsResponse.Indeterminate)
        service.checkVerificationResult(response) shouldBe Right(response)
      }
    }
  }

  "handleResponse" should {

    "return ValidStatus" when {
      "given Right()" in {
        service.handleResponse(Right(successResponse)) shouldBe ValidStatus
      }
    }

    "return IndeterminateStatus" when {
      "given Left(BankAccountUnverified)" in {
        service.handleResponse(Left(BankAccountUnverified)) shouldBe IndeterminateStatus
      }
    }

    "return InvalidStatus" when {
      "given Left(AccountDetailInvalidFormat)" in {
        service.handleResponse(Left(AccountDetailInvalidFormat)) shouldBe InvalidStatus
      }
      "given Left(SortCodeNotFound)" in {
        service.handleResponse(Left(SortCodeNotFound)) shouldBe InvalidStatus
      }
      "given Left(SortCodeNotSupported)" in {
        service.handleResponse(Left(SortCodeNotSupported)) shouldBe InvalidStatus
      }
      "given Left(AccountNotFound)" in {
        service.handleResponse(Left(AccountNotFound)) shouldBe InvalidStatus
      }
      "given Left(NameMismatch)" in {
        service.handleResponse(Left(NameMismatch)) shouldBe InvalidStatus
      }
      "given Left(DetailsVerificationFailed)" in {
        service.handleResponse(Left(DetailsVerificationFailed)) shouldBe InvalidStatus
      }
    }
  }

  "buildJsonRequestBody" should {

    "return a JSON body with 'account' and 'subject' keys" when {
      "given a Personal account type" in {
        val result = service.buildJsonRequestBody(BankAccountType.Personal, personalDetails)

        result shouldBe Json.toJson(
          BarsPersonalRequest(
            BarsAccount(personalDetails.sortCode, personalDetails.number),
            BarsSubject(personalDetails.name)
          )
        )
      }
    }

    "not include a 'business' key" when {
      "given a Personal account type" in {
        val result = service.buildJsonRequestBody(BankAccountType.Personal, personalDetails)
        (result \ "business").isDefined shouldBe false
      }
    }

    "return a JSON body with 'account' and 'business' keys" when {
      "given a Business account type" in {
        val result = service.buildJsonRequestBody(BankAccountType.Business, businessDetails)

        result shouldBe Json.toJson(
          BarsBusinessRequest(
            BarsAccount(businessDetails.sortCode, businessDetails.number),
            BarsBusiness(businessDetails.name)
          )
        )
      }
    }

    "not include a 'subject' key" when {
      "given a Business account type" in {
        val result = service.buildJsonRequestBody(BankAccountType.Business, businessDetails)
        (result \ "subject").isDefined shouldBe false
      }
    }
  }
}
