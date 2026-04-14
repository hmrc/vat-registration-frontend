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
    name = "Jane Doe",
    rollNumber = None
  )

  private val businessDetails = BankAccountDetails(
    sortCode = "654321",
    number = "87654321",
    name = "Aspect Ratio",
    rollNumber = None
  )

  private def barsResponseWith(
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

  private val successResponse: BarsVerificationResponse = barsResponseWith()

  "verifyBankDetails" should {

    "return ValidStatus and the response" when {
      "the connector returns a successful BARS response" in {
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(successResponse))

        val (status, response) = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        status shouldBe ValidStatus
        response shouldBe Some(successResponse)
      }
    }

    "return IndeterminateStatus and the response" when {
      "accountExists is Indeterminate" in {
        val barsResponse = barsResponseWith(accountExists = BarsResponse.Indeterminate)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val (status, response) = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        status shouldBe IndeterminateStatus
        response shouldBe Some(barsResponse)
      }

      "nameMatches is Indeterminate" in {
        val barsResponse = barsResponseWith(nameMatches = BarsResponse.Indeterminate)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val (status, response) = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        status shouldBe IndeterminateStatus
        response shouldBe Some(barsResponse)
      }
    }

    "return InvalidStatus" when {
      "sortCodeIsPresentOnEISCD is No" in {
        val barsResponse = barsResponseWith(sortCodeIsPresentOnEISCD = BarsResponse.No)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val (status, response) = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        status shouldBe InvalidStatus
        response shouldBe Some(barsResponse)
      }

      "accountExists is No" in {
        val barsResponse = barsResponseWith(accountExists = BarsResponse.No)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val (status, response) = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        status shouldBe InvalidStatus
        response shouldBe Some(barsResponse)
      }

      "nameMatches is No" in {
        val barsResponse = barsResponseWith(nameMatches = BarsResponse.No)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val (status, response) = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        status shouldBe InvalidStatus
        response shouldBe Some(barsResponse)
      }

      "BARS returns an error on the sort code check" in {
        val barsResponse = barsResponseWith(sortCodeIsPresentOnEISCD = BarsResponse.Error)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val (status, response) = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        status shouldBe InvalidStatus
        response shouldBe Some(barsResponse)
      }

      "BARS returns an error on the account exists check" in {
        val barsResponse = barsResponseWith(accountExists = BarsResponse.Error)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val (status, response) = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        status shouldBe InvalidStatus
        response shouldBe Some(barsResponse)
      }

      "BARS returns an error on the name match check" in {
        val barsResponse = barsResponseWith(nameMatches = BarsResponse.Error)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val (status, response) = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        status shouldBe InvalidStatus
        response shouldBe Some(barsResponse)
      }

      "the connector throws an exception" in {
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.failed(new RuntimeException("failure")))

        val (status, response) = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        status shouldBe InvalidStatus
        response shouldBe None
      }
    }
  }

  "checkVerificationResult" should {

    "return Right containing the response" when {
      "the response is successful" in {
        service.checkVerificationResult(successResponse) shouldBe Right(successResponse)
      }

      "the name is a partial match" in {
        val response = barsResponseWith(nameMatches = BarsResponse.Partial)
        service.checkVerificationResult(response) shouldBe Right(response)
      }
    }

    "return Left(BankAccountUnverified)" when {
      "accountExists is Indeterminate" in {
        val response = barsResponseWith(accountExists = BarsResponse.Indeterminate)
        service.checkVerificationResult(response) shouldBe Left(BankAccountUnverified)
      }

      "nameMatches is Indeterminate and accountExists is Yes" in {
        val response = barsResponseWith(nameMatches = BarsResponse.Indeterminate)
        service.checkVerificationResult(response) shouldBe Left(BankAccountUnverified)
      }
    }

    "return Left(ThirdPartyError)" when {
      "sortCodeIsPresentOnEISCD is Error" in {
        val response = barsResponseWith(sortCodeIsPresentOnEISCD = BarsResponse.Error)
        service.checkVerificationResult(response) shouldBe Left(ThirdPartyError)
      }

      "accountExists is Error" in {
        val response = barsResponseWith(accountExists = BarsResponse.Error)
        service.checkVerificationResult(response) shouldBe Left(ThirdPartyError)
      }

      "nameMatches is Error" in {
        val response = barsResponseWith(nameMatches = BarsResponse.Error)
        service.checkVerificationResult(response) shouldBe Left(ThirdPartyError)
      }
    }

    "return Left(SortCodeNotFound)" when {
      "sortCodeIsPresentOnEISCD is No" in {
        val response = barsResponseWith(sortCodeIsPresentOnEISCD = BarsResponse.No)
        service.checkVerificationResult(response) shouldBe Left(SortCodeNotFound)
      }
    }

    "return Left(AccountNotFound)" when {
      "accountExists is No" in {
        val response = barsResponseWith(accountExists = BarsResponse.No)
        service.checkVerificationResult(response) shouldBe Left(AccountNotFound)
      }
    }

    "return Left(NameMismatch)" when {
      "nameMatches is No" in {
        val response = barsResponseWith(nameMatches = BarsResponse.No)
        service.checkVerificationResult(response) shouldBe Left(NameMismatch)
      }
    }
  }

  "buildJsonRequestBody" should {
    val personalDetailsWithRollNumber = BankAccountDetails(
      sortCode = "786786",
      number = "12345678",
      name = "Jane Doe",
      rollNumber = Some("AB/463")
    )

    val businessDetailsWithRollNumber = BankAccountDetails(
      sortCode = "786786",
      number = "87654321",
      name = "Aspect Ratio Software Ltd",
      rollNumber = Some("AB/463")
    )

    "return a JSON body with 'account' and 'subject' keys" when {
      "given a Personal account type without a roll number" in {
        val result = service.buildJsonRequestBody(personalDetails, BankAccountType.Personal)

        result shouldBe Json.toJson(
          BarsPersonalRequest(
            BarsAccount(personalDetails.sortCode, personalDetails.number, None),
            BarsSubject(personalDetails.name)
          )
        )
      }

      "given a Personal account type with a roll number" in {
        val result = service.buildJsonRequestBody(personalDetailsWithRollNumber, BankAccountType.Personal)

        result shouldBe Json.toJson(
          BarsPersonalRequest(
            BarsAccount(personalDetailsWithRollNumber.sortCode, personalDetailsWithRollNumber.number, Some("AB/463")),
            BarsSubject(personalDetailsWithRollNumber.name)
          )
        )
      }
    }

    "include the roll number in the account object" when {
      "given a Personal account type with a roll number" in {
        val result = service.buildJsonRequestBody(personalDetailsWithRollNumber, BankAccountType.Personal)
        (result \ "account" \ "rollNumber").asOpt[String] shouldBe Some("AB/463")
      }

      "given a Business account type with a roll number" in {
        val result = service.buildJsonRequestBody(businessDetailsWithRollNumber, BankAccountType.Business)
        (result \ "account" \ "rollNumber").asOpt[String] shouldBe Some("AB/463")
      }
    }

    "not include the roll number in the account object" when {
      "given a Personal account type without a roll number" in {
        val result = service.buildJsonRequestBody(personalDetails, BankAccountType.Personal)
        (result \ "account" \ "rollNumber").isDefined shouldBe false
      }

      "given a Business account type without a roll number" in {
        val result = service.buildJsonRequestBody(businessDetails, BankAccountType.Business)
        (result \ "account" \ "rollNumber").isDefined shouldBe false
      }
    }

    "not include a 'business' key" when {
      "given a Personal account type" in {
        val result = service.buildJsonRequestBody(personalDetails, BankAccountType.Personal)
        (result \ "business").isDefined shouldBe false
      }
    }

    "return a JSON body with 'account' and 'business' keys" when {
      "given a Business account type without a roll number" in {
        val result = service.buildJsonRequestBody(businessDetails, BankAccountType.Business)

        result shouldBe Json.toJson(
          BarsBusinessRequest(
            BarsAccount(businessDetails.sortCode, businessDetails.number, None),
            BarsBusiness(businessDetails.name)
          )
        )
      }

      "given a Business account type with a roll number" in {
        val result = service.buildJsonRequestBody(businessDetailsWithRollNumber, BankAccountType.Business)

        result shouldBe Json.toJson(
          BarsBusinessRequest(
            BarsAccount(businessDetailsWithRollNumber.sortCode, businessDetailsWithRollNumber.number, Some("AB/463")),
            BarsBusiness(businessDetailsWithRollNumber.name)
          )
        )
      }
    }

    "not include a 'subject' key" when {
      "given a Business account type" in {
        val result = service.buildJsonRequestBody(businessDetails, BankAccountType.Business)
        (result \ "subject").isDefined shouldBe false
      }
    }
  }
}
