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
import models.api.{BankAccountDetailsStatus, IndeterminateStatus, InvalidStatus, ValidStatus}
import models.bars.BarsErrors._
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

  // ---- fixtures ----

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

  private def stubVerify(endpoint: String, requestJson: JsValue, response: BarsVerificationResponse): Unit =
    when(mockConnector.verify(eqArg(endpoint), eqArg(requestJson))(anyArg[HeaderCarrier]))
      .thenReturn(scala.concurrent.Future.successful(response))

  private def stubVerifyFail(endpoint: String, requestJson: JsValue, ex: Throwable): Unit =
    when(mockConnector.verify(eqArg(endpoint), eqArg(requestJson))(anyArg[HeaderCarrier]))
      .thenReturn(scala.concurrent.Future.failed(ex))

  // ---- barsVerification tests ----

  "BarsService.barsVerification" should {

    "call personal endpoint with correct JSON and return Right(response) on full success" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse()
      stubVerify("personal", request, response)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Right(response))
    }

    "treat personalOrBusiness case-insensitively for personal" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse()
      stubVerify("personal", request, response)

      service.barsVerification("PERSONAL", bankDetails).map(_ shouldBe Right(response))
    }

    "call business endpoint with correct JSON and return Right(response) on success" in {
      val request  = businessJson(bankDetails)
      val response = makeResponse()
      stubVerify("business", request, response)

      service.barsVerification("business", bankDetails).map(_ shouldBe Right(response))
    }

    "accept accountNumberIsWellFormatted = Indeterminate as success (per checkBarsResponseSuccess)" in {
      val request  = businessJson(bankDetails)
      val response = makeResponse(accountNumberIsWellFormatted = BarsResponse.Indeterminate)
      stubVerify("business", request, response)

      service.barsVerification("business", bankDetails).map(_ shouldBe Right(response))
    }

    "accept nameMatches = Partial as success (per checkBarsResponseSuccess)" in {
      val request  = businessJson(bankDetails)
      val response = makeResponse(nameMatches = BarsResponse.Partial)
      stubVerify("business", request, response)

      service.barsVerification("business", bankDetails).map(_ shouldBe Right(response))
    }

    "map UpstreamBarsException 400 with SORT_CODE_ON_DENY_LIST to Left(SortCodeOnDenyList)" in {
      val request = personalJson(bankDetails)

      stubVerifyFail(
        "personal",
        request,
        UpstreamBarsException(status = 400, errorCode = Some("SORT_CODE_ON_DENY_LIST"), rawMessage = "sort code is on deny list")
      )

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(SortCodeOnDenyList))
    }

    "map UpstreamBarsException 400 (non deny list) to Left(DetailsVerificationFailed)" in {
      val request = personalJson(bankDetails)

      stubVerifyFail(
        "personal",
        request,
        UpstreamBarsException(status = 400, errorCode = Some("SOME_OTHER_CODE"), rawMessage = "failure")
      )

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(DetailsVerificationFailed))
    }

    "map non-400 UpstreamBarsException to Left(DetailsVerificationFailed)" in {
      val request = personalJson(bankDetails)

      stubVerifyFail(
        "personal",
        request,
        UpstreamBarsException(status = 500, errorCode = None, rawMessage = "failure")
      )

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(DetailsVerificationFailed))
    }

    "map any other thrown exception to Left(DetailsVerificationFailed)" in {
      val request = personalJson(bankDetails)

      stubVerifyFail("personal", request, new RuntimeException("unexpected"))

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(DetailsVerificationFailed))
    }

    "return Left(BankAccountUnverified) when accountExists is Indeterminate (checkAccountAndName)" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(accountExists = BarsResponse.Indeterminate)
      stubVerify("personal", request, response)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(BankAccountUnverified))
    }

    "return Left(BankAccountUnverified) when nameMatches is Indeterminate (checkAccountAndName)" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(nameMatches = BarsResponse.Indeterminate)
      stubVerify("personal", request, response)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(BankAccountUnverified))
    }

    "return Left(AccountDetailInvalidFormat) when accountNumberIsWellFormatted is No" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(accountNumberIsWellFormatted = BarsResponse.No)
      stubVerify("personal", request, response)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(AccountDetailInvalidFormat))
    }

    "return Left(SortCodeNotFound) when sortCodeIsPresentOnEISCD is No" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(sortCodeIsPresentOnEISCD = BarsResponse.No)
      stubVerify("personal", request, response)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(SortCodeNotFound))
    }

    "return Left(SortCodeNotSupported) when sortCodeSupportsDirectDebit is No" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(sortCodeSupportsDirectDebit = BarsResponse.No)
      stubVerify("personal", request, response)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(SortCodeNotSupported))
    }

    "return Left(AccountNotFound) when accountExists is No" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(accountExists = BarsResponse.No)
      stubVerify("personal", request, response)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(AccountNotFound))
    }

    "return Left(AccountNotFound) when accountExists is Inapplicable" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(accountExists = BarsResponse.Inapplicable)
      stubVerify("personal", request, response)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(AccountNotFound))
    }

    "return Left(NameMismatch) when nameMatches is No" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(nameMatches = BarsResponse.No)
      stubVerify("personal", request, response)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(NameMismatch))
    }

    "return Left(NameMismatch) when nameMatches is Inapplicable" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(nameMatches = BarsResponse.Inapplicable)
      stubVerify("personal", request, response)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(NameMismatch))
    }

    "return Left(DetailsVerificationFailed) when sortcode direct debit is indeterminate" in {

      val request  = personalJson(bankDetails)
      val response = makeResponse(sortCodeSupportsDirectDebit = BarsResponse.Indeterminate)
      stubVerify("personal", request, response)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(DetailsVerificationFailed))
    }
  }

  // ---- validateBankDetailsStatus tests ----

  "BarsService.validateBankDetailsStatus" should {

    "return ValidStatus when barsVerification is Right" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse()
      stubVerify("personal", request, response)

      service.validateBankDetailsStatus("personal", bankDetails).map(_ shouldBe ValidStatus)
    }

    "return IndeterminateStatus when barsVerification returns Left(BankAccountUnverified)" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(accountExists = BarsResponse.Indeterminate)
      stubVerify("personal", request, response)

      service.validateBankDetailsStatus("personal", bankDetails).map(_ shouldBe IndeterminateStatus)
    }

    "return InvalidStatus for any other Left(BarsErrors)" in {
      val request  = personalJson(bankDetails)
      val response = makeResponse(sortCodeIsPresentOnEISCD = BarsResponse.No)
      stubVerify("personal", request, response)

      service.validateBankDetailsStatus("personal", bankDetails).map(_ shouldBe InvalidStatus)
    }

    "return InvalidStatus when the connector fails with deny list (mapped to SortCodeOnDenyList)" in {
      val request = personalJson(bankDetails)

      stubVerifyFail(
        "personal",
        request,
        UpstreamBarsException(status = 400, errorCode = Some("SORT_CODE_ON_DENY_LIST"), rawMessage = "sort code failure")
      )

      service.validateBankDetailsStatus("personal", bankDetails).map(_ shouldBe InvalidStatus)
    }
  }
}