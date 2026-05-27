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
import models.bars.BankAccountType.{Business, Personal}
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
  private val service       = BarsService(mockConnector)

  private val personalDetails = BankAccountDetails(
    sortCode = "123456",
    number = "12345678",
    name = "Jane Doe",
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
        when(mockConnector.verify(any(), any())(any())).thenReturn(Future.successful(successResponse))

        val result = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        result.bankDetailsVerificationStatus shouldBe ValidStatus
        result.barsVerificationResponse shouldBe Some(successResponse)
      }
    }

    "return IndeterminateStatus and the response" when {
      "accountExists is Indeterminate" in {
        val barsResponse = barsResponseWith(accountExists = BarsResponse.Indeterminate)
        when(mockConnector.verify(any(), any())(any())).thenReturn(Future.successful(barsResponse))

        val result = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        result.bankDetailsVerificationStatus shouldBe IndeterminateStatus
        result.barsVerificationResponse shouldBe Some(barsResponse)
      }

      "nameMatches is Indeterminate" in {
        val barsResponse = barsResponseWith(nameMatches = BarsResponse.Indeterminate)
        when(mockConnector.verify(any(), any())(any())).thenReturn(Future.successful(barsResponse))

        val result = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        result.bankDetailsVerificationStatus shouldBe IndeterminateStatus
        result.barsVerificationResponse shouldBe Some(barsResponse)
      }
    }

    "return InvalidStatus" when {
      "sortCodeIsPresentOnEISCD is No" in {
        val barsResponse = barsResponseWith(sortCodeIsPresentOnEISCD = BarsResponse.No)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val result = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        result.bankDetailsVerificationStatus shouldBe InvalidStatus
        result.barsVerificationResponse shouldBe Some(barsResponse)
      }

      "accountExists is No" in {
        val barsResponse = barsResponseWith(accountExists = BarsResponse.No)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val result = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        result.bankDetailsVerificationStatus shouldBe InvalidStatus
        result.barsVerificationResponse shouldBe Some(barsResponse)
      }

      "nameMatches is No" in {
        val barsResponse = barsResponseWith(nameMatches = BarsResponse.No)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val result = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        result.bankDetailsVerificationStatus shouldBe InvalidStatus
        result.barsVerificationResponse shouldBe Some(barsResponse)
      }

      "BARS returns an error on the sort code check" in {
        val barsResponse = barsResponseWith(sortCodeIsPresentOnEISCD = BarsResponse.Error)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val result = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        result.bankDetailsVerificationStatus shouldBe InvalidStatus
        result.barsVerificationResponse shouldBe Some(barsResponse)
      }

      "BARS returns an error on the account exists check" in {
        val barsResponse = barsResponseWith(accountExists = BarsResponse.Error)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val result = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        result.bankDetailsVerificationStatus shouldBe InvalidStatus
        result.barsVerificationResponse shouldBe Some(barsResponse)
      }

      "BARS returns an error on the name match check" in {
        val barsResponse = barsResponseWith(nameMatches = BarsResponse.Error)
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.successful(barsResponse))

        val result = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        result.bankDetailsVerificationStatus shouldBe InvalidStatus
        result.barsVerificationResponse shouldBe Some(barsResponse)
      }

      "the connector throws an exception" in {
        when(mockConnector.verify(any(), any())(any()))
          .thenReturn(Future.failed(new RuntimeException("failure")))

        val result = service.verifyBankDetails(personalDetails, BankAccountType.Personal).futureValue

        result.bankDetailsVerificationStatus shouldBe InvalidStatus
        result.barsVerificationResponse shouldBe None
      }
    }
  }

  "buildJsonRequestBody" should {
    val personalDetails = BankAccountDetails(
      sortCode = "786786",
      number = "12345678",
      name = "Jane Doe",
      rollNumber = Some("AB/463")
    )

    val businessDetails = BankAccountDetails(
      sortCode = "786786",
      number = "87654321",
      name = "Aspect Ratio Software Ltd",
      rollNumber = Some("AB/463")
    )

    "return a Json body for a Personal account" when {
      "no rollNumber is provided" in {
        val result = service.buildJsonRequestBody(personalDetails.copy(rollNumber = None), Personal)
        val expectedResult = Json.parse("""
            |{
            |  "account": {
            |    "sortCode": "786786",
            |    "accountNumber": "12345678"
            |  },
            |  "subject": {
            |    "name": "Jane Doe"
            |  }
            |}
            |""".stripMargin)

        result shouldBe expectedResult
      }

      "a rollNumber is provided" in {
        val result = service.buildJsonRequestBody(personalDetails.copy(rollNumber = Some("AB/463")), Personal)
        val expectedResult = Json.parse("""
            |{
            |  "account": {
            |    "sortCode": "786786",
            |    "accountNumber": "12345678",
            |    "rollNumber": "AB/463"
            |  },
            |  "subject": {
            |    "name": "Jane Doe"
            |  }
            |}
            |""".stripMargin)

        result shouldBe expectedResult
      }
    }

    "return a Json body for a Business account" when {
      "no rollNumber is provided" in {
        val result = service.buildJsonRequestBody(businessDetails.copy(rollNumber = None), Business)
        val expectedResult = Json.parse("""
            |{
            |  "account": {
            |    "sortCode": "786786",
            |    "accountNumber": "87654321"
            |  },
            |  "business": {
            |    "companyName": "Aspect Ratio Software Ltd"
            |  }
            |}
            |""".stripMargin)

        result shouldBe expectedResult
      }

      "a rollNumber is provided" in {
        val result = service.buildJsonRequestBody(businessDetails.copy(rollNumber = Some("AB/463")), Business)
        val expectedResult = Json.parse("""
            |{
            |  "account": {
            |    "sortCode": "786786",
            |    "accountNumber": "87654321",
            |    "rollNumber": "AB/463"
            |  },
            |  "business": {
            |    "companyName": "Aspect Ratio Software Ltd"
            |  }
            |}
            |""".stripMargin)

        result shouldBe expectedResult
      }
    }
  }

}
