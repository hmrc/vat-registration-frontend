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
import org.mockito.ArgumentMatchers.{any => anyArg, eq => eqArg}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class BarsServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with ScalaFutures {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  private val mockConnector: BarsConnector = mock[BarsConnector]
  private val service = BarsService(mockConnector)

  private val bankDetails: BankAccountDetails =
    BankAccountDetails(
      name     = "Test Name",
      number   = "12345678",
      sortCode = "112233",
      status   = None
    )

  private def personalJson(details: BankAccountDetails): JsValue =
    Json.toJson(
      BarsPersonalRequest(
        BarsAccount(details.sortCode, details.number),
        BarsSubject(details.name)
      )
    )

  private def businessJson(details: BankAccountDetails): JsValue =
    Json.toJson(
      BarsBusinessRequest(
        BarsAccount(details.sortCode, details.number),
        BarsBusiness(details.name)
      )
    )

  private def makeResponse(
                            accountNumberIsWellFormatted: BarsResponse = BarsResponse.Yes,
                            sortCodeIsPresentOnEISCD: BarsResponse     = BarsResponse.Yes,
                            accountExists: BarsResponse                = BarsResponse.Yes,
                            nameMatches: BarsResponse                  = BarsResponse.Yes,
                            sortCodeSupportsDirectDebit: BarsResponse  = BarsResponse.Yes
                          ): BarsVerificationResponse =
    BarsVerificationResponse(
      accountNumberIsWellFormatted              = accountNumberIsWellFormatted,
      sortCodeIsPresentOnEISCD                  = sortCodeIsPresentOnEISCD,
      sortCodeBankName                          = Some("Some Bank"),
      accountExists                             = accountExists,
      nameMatches                               = nameMatches,
      sortCodeSupportsDirectDebit               = sortCodeSupportsDirectDebit,
      sortCodeSupportsDirectCredit              = BarsResponse.Yes,
      nonStandardAccountDetailsRequiredForBacs  = None,
      iban                                      = None,
      accountName                               = None
    )

  private def stubVerify(accountType: BankAccountType, requestJson: JsValue, response: BarsVerificationResponse): Unit =
    when(mockConnector.verify(eqArg(accountType), eqArg(requestJson))(anyArg[HeaderCarrier]))
      .thenReturn(scala.concurrent.Future.successful(response))

  private def stubVerifyFail(accountType: BankAccountType, requestJson: JsValue, ex: Throwable): Unit =
    when(mockConnector.verify(eqArg(accountType), eqArg(requestJson))(anyArg[HeaderCarrier]))
      .thenReturn(scala.concurrent.Future.failed(ex))

  "BarsService.verifyBankDetails" should {

    "make a verification request with a personal account with correct JSON and return ValidStatus on full success" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse()
      stubVerify(BankAccountType.Personal, request, response)

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map { status =>
        status shouldBe ValidStatus
      }
    }

    "make a verification request with a business account with correct SON and return ValidStatus on full success" in {
      val request  = businessJson(bankDetails)
      val response = makeResponse()
      stubVerify(BankAccountType.Business, request, response)

      service.verifyBankDetails(BankAccountType.Business, bankDetails).map { status =>
        status shouldBe ValidStatus
      }
    }

    "accept accountNumberIsWellFormatted = Indeterminate as success (ValidStatus)" in {
      val request  = businessJson(bankDetails)
      val response = makeResponse(accountNumberIsWellFormatted = BarsResponse.Indeterminate)
      stubVerify(BankAccountType.Business, request, response)

      service.verifyBankDetails(BankAccountType.Business, bankDetails).map(_ shouldBe ValidStatus)
    }

    "accept nameMatches = Partial as success (ValidStatus)" in {
      val request  = businessJson(bankDetails)
      val response = makeResponse(nameMatches = BarsResponse.Partial)
      stubVerify(BankAccountType.Business, request, response)

      service.verifyBankDetails(BankAccountType.Business, bankDetails).map(_ shouldBe ValidStatus)
    }

    "return InvalidStatus when connector fails with 400 deny list" in {
      val request = personalJson(bankDetails)

      stubVerifyFail(
        BankAccountType.Personal,
        request,
        UpstreamBarsException(status = 400, errorCode = Some("SORT_CODE_ON_DENY_LIST"), rawMessage = "deny list")
      )

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe InvalidStatus)
    }

    "return InvalidStatus when connector fails with 400 non deny list" in {
      val request = personalJson(bankDetails)

      stubVerifyFail(
        BankAccountType.Personal,
        request,
        UpstreamBarsException(status = 400, errorCode = Some("SOME_OTHER_CODE"), rawMessage = "failure")
      )

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe InvalidStatus)
    }

    "return InvalidStatus when connector fails with non-400 UpstreamBarsException" in {
      val request = personalJson(bankDetails)

      stubVerifyFail(
        BankAccountType.Personal,
        request,
        UpstreamBarsException(status = 500, errorCode = None, rawMessage = "failure")
      )

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe InvalidStatus)
    }

    "return InvalidStatus when connector throws any other exception" in {
      val request = personalJson(bankDetails)

      stubVerifyFail(BankAccountType.Personal, request, new RuntimeException("unexpected"))

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe InvalidStatus)
    }

    "return IndeterminateStatus when accountExists is Indeterminate" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(accountExists = BarsResponse.Indeterminate)
      stubVerify(BankAccountType.Personal, request, response)

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe IndeterminateStatus)
    }

    "return IndeterminateStatus when nameMatches is Indeterminate" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(nameMatches = BarsResponse.Indeterminate)
      stubVerify(BankAccountType.Personal, request, response)

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe IndeterminateStatus)
    }

    "return InvalidStatus when accountNumberIsWellFormatted is No" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(accountNumberIsWellFormatted = BarsResponse.No)
      stubVerify(BankAccountType.Personal, request, response)

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe InvalidStatus)
    }

    "return InvalidStatus when sortCodeIsPresentOnEISCD is No" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(sortCodeIsPresentOnEISCD = BarsResponse.No)
      stubVerify(BankAccountType.Personal, request, response)

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe InvalidStatus)
    }

    "return InvalidStatus when sortCodeSupportsDirectDebit is No" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(sortCodeSupportsDirectDebit = BarsResponse.No)
      stubVerify(BankAccountType.Personal, request, response)

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe InvalidStatus)
    }

    "return InvalidStatus when accountExists is No" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(accountExists = BarsResponse.No)
      stubVerify(BankAccountType.Personal, request, response)

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe InvalidStatus)
    }

    "return InvalidStatus when accountExists is Inapplicable" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(accountExists = BarsResponse.Inapplicable)
      stubVerify(BankAccountType.Personal, request, response)

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe InvalidStatus)
    }

    "return InvalidStatus when nameMatches is No" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(nameMatches = BarsResponse.No)
      stubVerify(BankAccountType.Personal, request, response)

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe InvalidStatus)
    }

    "return InvalidStatus when nameMatches is Inapplicable" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(nameMatches = BarsResponse.Inapplicable)
      stubVerify(BankAccountType.Personal, request, response)

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe InvalidStatus)
    }

    "return InvalidStatus when sortCodeSupportsDirectDebit is Indeterminate" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(sortCodeSupportsDirectDebit = BarsResponse.Indeterminate)
      stubVerify(BankAccountType.Personal, request, response)

      service.verifyBankDetails(BankAccountType.Personal, bankDetails).map(_ shouldBe InvalidStatus)
    }
  }
}