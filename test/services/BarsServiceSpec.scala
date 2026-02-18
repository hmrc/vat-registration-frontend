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

  private def mkResp(
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

  private def stubVerify(endpoint: String, requestJson: JsValue, resp: BarsVerificationResponse): Unit =
    when(mockConnector.verify(eqArg(endpoint), eqArg(requestJson))(anyArg[HeaderCarrier]))
      .thenReturn(scala.concurrent.Future.successful(resp))

  private def stubVerifyFail(endpoint: String, requestJson: JsValue, ex: Throwable): Unit =
    when(mockConnector.verify(eqArg(endpoint), eqArg(requestJson))(anyArg[HeaderCarrier]))
      .thenReturn(scala.concurrent.Future.failed(ex))

  // ---- barsVerification tests ----

  "BarsService.barsVerification" should {

    "call personal endpoint with correct JSON and return Right(response) on full success" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp()
      stubVerify("personal", req, resp)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Right(resp))
    }

    "treat personalOrBusiness case-insensitively for personal" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp()
      stubVerify("personal", req, resp)

      service.barsVerification("PeRsOnAl", bankDetails).map(_ shouldBe Right(resp))
    }

    "call business endpoint with correct JSON and return Right(response) on success" in {
      val req  = businessJson(bankDetails)
      val resp = mkResp()
      stubVerify("business", req, resp)

      service.barsVerification("business", bankDetails).map(_ shouldBe Right(resp))
    }

    "use business endpoint for any non-'personal' string" in {
      val req  = businessJson(bankDetails)
      val resp = mkResp()
      stubVerify("business", req, resp)

      service.barsVerification("anything-else", bankDetails).map(_ shouldBe Right(resp))
    }

    "accept accountNumberIsWellFormatted = Indeterminate as success (per checkBarsResponseSuccess)" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp(accountNumberIsWellFormatted = BarsResponse.Indeterminate)
      stubVerify("personal", req, resp)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Right(resp))
    }

    "accept nameMatches = Partial as success (per checkBarsResponseSuccess)" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp(nameMatches = BarsResponse.Partial)
      stubVerify("personal", req, resp)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Right(resp))
    }

    "map UpstreamBarsException 400 with SORT_CODE_ON_DENY_LIST to Left(SortCodeOnDenyList)" in {
      val req = personalJson(bankDetails)

      stubVerifyFail(
        "personal",
        req,
        UpstreamBarsException(status = 400, errorCode = Some("SORT_CODE_ON_DENY_LIST"), rawMessage = "boom")
      )

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(SortCodeOnDenyList))
    }

    "map UpstreamBarsException 400 (non deny list) to Left(DetailsVerificationFailed)" in {
      val req = personalJson(bankDetails)

      stubVerifyFail(
        "personal",
        req,
        UpstreamBarsException(status = 400, errorCode = Some("SOME_OTHER_CODE"), rawMessage = "boom")
      )

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(DetailsVerificationFailed))
    }

    "map non-400 UpstreamBarsException to Left(DetailsVerificationFailed)" in {
      val req = personalJson(bankDetails)

      stubVerifyFail(
        "personal",
        req,
        UpstreamBarsException(status = 500, errorCode = None, rawMessage = "boom")
      )

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(DetailsVerificationFailed))
    }

    "map any other thrown exception to Left(DetailsVerificationFailed)" in {
      val req = personalJson(bankDetails)

      stubVerifyFail("personal", req, new RuntimeException("unexpected"))

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(DetailsVerificationFailed))
    }

    "return Left(BankAccountUnverified) when accountExists is Indeterminate (checkAccountAndName)" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp(accountExists = BarsResponse.Indeterminate)
      stubVerify("personal", req, resp)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(BankAccountUnverified))
    }

    "return Left(BankAccountUnverified) when nameMatches is Indeterminate (checkAccountAndName)" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp(nameMatches = BarsResponse.Indeterminate)
      stubVerify("personal", req, resp)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(BankAccountUnverified))
    }

    "return Left(AccountDetailInvalidFormat) when accountNumberIsWellFormatted is No" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp(accountNumberIsWellFormatted = BarsResponse.No)
      stubVerify("personal", req, resp)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(AccountDetailInvalidFormat))
    }

    "return Left(SortCodeNotFound) when sortCodeIsPresentOnEISCD is No" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp(sortCodeIsPresentOnEISCD = BarsResponse.No)
      stubVerify("personal", req, resp)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(SortCodeNotFound))
    }

    "return Left(SortCodeNotSupported) when sortCodeSupportsDirectDebit is No" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp(sortCodeSupportsDirectDebit = BarsResponse.No)
      stubVerify("personal", req, resp)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(SortCodeNotSupported))
    }

    "return Left(AccountNotFound) when accountExists is No" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp(accountExists = BarsResponse.No)
      stubVerify("personal", req, resp)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(AccountNotFound))
    }

    "return Left(AccountNotFound) when accountExists is Inapplicable" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp(accountExists = BarsResponse.Inapplicable)
      stubVerify("personal", req, resp)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(AccountNotFound))
    }

    "return Left(NameMismatch) when nameMatches is No" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp(nameMatches = BarsResponse.No)
      stubVerify("personal", req, resp)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(NameMismatch))
    }

    "return Left(NameMismatch) when nameMatches is Inapplicable" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp(nameMatches = BarsResponse.Inapplicable)
      stubVerify("personal", req, resp)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(NameMismatch))
    }

    "return Left(DetailsVerificationFailed) when success predicate fails but none of the detailed checks fail (fallback)" in {
      // success requires sortCodeSupportsDirectDebit == Yes,
      // but detailed check only fails when it is No.
      val req  = personalJson(bankDetails)
      val resp = mkResp(sortCodeSupportsDirectDebit = BarsResponse.Indeterminate)
      stubVerify("personal", req, resp)

      service.barsVerification("personal", bankDetails).map(_ shouldBe Left(DetailsVerificationFailed))
    }
  }

  // ---- validateBankDetailsStatus tests ----

  "BarsService.validateBankDetailsStatus" should {

    "return ValidStatus when barsVerification is Right" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp()
      stubVerify("personal", req, resp)

      service.validateBankDetailsStatus("personal", bankDetails).map(_ shouldBe ValidStatus)
    }

    "return IndeterminateStatus when barsVerification returns Left(BankAccountUnverified)" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp(accountExists = BarsResponse.Indeterminate)
      stubVerify("personal", req, resp)

      service.validateBankDetailsStatus("personal", bankDetails).map(_ shouldBe IndeterminateStatus)
    }

    "return InvalidStatus for any other Left(BarsErrors)" in {
      val req  = personalJson(bankDetails)
      val resp = mkResp(sortCodeIsPresentOnEISCD = BarsResponse.No) // -> SortCodeNotFound
      stubVerify("personal", req, resp)

      service.validateBankDetailsStatus("personal", bankDetails).map(_ shouldBe InvalidStatus)
    }

    "return InvalidStatus when the connector fails with deny list (mapped to SortCodeOnDenyList)" in {
      val req = personalJson(bankDetails)

      stubVerifyFail(
        "personal",
        req,
        UpstreamBarsException(status = 400, errorCode = Some("SORT_CODE_ON_DENY_LIST"), rawMessage = "boom")
      )

      service.validateBankDetailsStatus("personal", bankDetails).map(_ shouldBe InvalidStatus)
    }
  }
}