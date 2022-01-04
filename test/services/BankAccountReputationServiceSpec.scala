/*
 * Copyright 2022 HM Revenue & Customs
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

import featureswitch.core.config.{FeatureSwitching, StubBars}
import models.BankAccountDetails
import models.api.{IndeterminateStatus, InvalidStatus, ValidStatus}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json._
import testHelpers.{S4LMockSugar, VatRegSpec}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.{ExecutionContext, Future}

class BankAccountReputationServiceSpec extends VatRegSpec with S4LMockSugar with FeatureSwitching {

  class Setup {
    disable(StubBars)
    val service: BankAccountReputationService = new BankAccountReputationService(
      mockBankAccountReputationConnector,
      mockAuthClientConnector,
      mockAuditConnector
    )
  }

  "Calling validateBankDetails" should {
    val bankDetails = BankAccountDetails("testName", "12-34-56", "12345678")

    "return ValidStatus when the json returns a true" in new Setup {
      val testUserId = "testUserId"

      when(mockBankAccountReputationConnector.validateBankDetails(any())(any()))
        .thenReturn(Future.successful(validBankCheckJsonResponse))
      mockAuthenticatedInternalId(Some(testUserId))

      val testAuditRequest: JsObject = Json.obj(
        "credId" -> testUserId,
        "request" -> Json.toJson(bankDetails),
        "response" -> validBankCheckJsonResponse
      )

      service.validateBankDetails(bankDetails) returns ValidStatus

      verify(mockAuditConnector).sendExplicitAudit(ArgumentMatchers.eq("BarsValidateCheck"), ArgumentMatchers.eq(testAuditRequest)
      )(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[ExecutionContext])
    }

    "return InvalidStatus when the json returns a false" in new Setup {
      val testUserId = "testUserId"

      when(mockBankAccountReputationConnector.validateBankDetails(any())(any()))
        .thenReturn(Future.successful(invalidBankCheckJsonResponse))
      mockAuthenticatedInternalId(Some(testUserId))

      val testAuditRequest: JsObject = Json.obj(
        "credId" -> testUserId,
        "request" -> Json.toJson(bankDetails),
        "response" -> invalidBankCheckJsonResponse
      )

      service.validateBankDetails(bankDetails) returns InvalidStatus

      verify(mockAuditConnector).sendExplicitAudit(ArgumentMatchers.eq("BarsValidateCheck"), ArgumentMatchers.eq(testAuditRequest)
      )(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[ExecutionContext])
    }

    "return IndeterminateStatus when the json returns indeterminate" in new Setup {
      val testUserId = "testUserId"

      when(mockBankAccountReputationConnector.validateBankDetails(any())(any()))
        .thenReturn(Future.successful(indeterminateBankCheckJsonResponse))
      mockAuthenticatedInternalId(Some(testUserId))

      val testAuditRequest: JsObject = Json.obj(
        "credId" -> testUserId,
        "request" -> Json.toJson(bankDetails),
        "response" -> indeterminateBankCheckJsonResponse
      )

      service.validateBankDetails(bankDetails) returns IndeterminateStatus

      verify(mockAuditConnector).sendExplicitAudit(ArgumentMatchers.eq("BarsValidateCheck"), ArgumentMatchers.eq(testAuditRequest)
      )(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[ExecutionContext])
    }
    "throw an exception if the response is invalid" in new Setup {
      val testUserId = "testUserId"

      when(mockBankAccountReputationConnector.validateBankDetails(any())(any()))
        .thenReturn(Future.successful(Json.obj("accountNumberWithSortCodeIsValid"-> "false")))
      mockAuthenticatedInternalId(Some(testUserId))

      intercept[NoSuchElementException] {
        await(service.validateBankDetails(bankDetails))
      }
    }
  }
}
