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

import models.BankAccountDetails
import models.bars.{BankAccountType, BarsResponse, BarsVerificationResponse}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.JsObject
import testHelpers.VatSpec
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.Future

class BarsAuditServiceSpec extends VatSpec with Matchers {

  private val bankAccountDetails = BankAccountDetails("testName", "12345678", "123456", None)
  private val bankAccountType    = BankAccountType.Business

  private val barsVerificationResponse = BarsVerificationResponse(
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

  trait Setup {
    val mockAuditConnector: AuditConnector    = mock[AuditConnector]
    val mockAuthConnector: AuthConnector      = mock[AuthConnector]
    val auditCaptor: ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])

    val service = new BarsAuditService(mockAuditConnector, mockAuthConnector)

    def stubAuth(credId: Option[String] = Some("testCredId"), affinity: Option[AffinityGroup] = Some(AffinityGroup.Organisation)): Unit =
      when(mockAuthConnector.authorise[Option[Any]](any(), any())(any(), any()))
        .thenReturn(Future.successful(credId))
        .thenReturn(Future.successful(affinity))

    def verifyAuditSent(): Unit =
      verify(mockAuditConnector, times(1)).sendExplicitAudit(any(), auditCaptor.capture())(any(), any())
  }

  "sendBarsAuditEvent" should {

    "send an audit event with correct fields for a passing check" in new Setup {
      stubAuth()

      await(
        service.sendBarsAuditEvent(
          bankAccountDetails = bankAccountDetails,
          bankAccountType = bankAccountType,
          rawResponse = Some(barsVerificationResponse),
          attemptNumber = 1,
          accountStatus = "unlocked",
          checkOutcome = "pass"
        ))

      verifyAuditSent()
      val detail: JsObject = auditCaptor.getValue
      (detail \ "checkOutcome").as[String] mustBe "pass"
      (detail \ "accountStatus").as[String] mustBe "unlocked"
      (detail \ "attemptNumber").as[Int] mustBe 1
      (detail \ "credId").as[String] mustBe "testCredId"
      (detail \ "userType").as[String] mustBe "organisation"
      (detail \ "detailsSubmitted" \ "accountType").as[String] mustBe "business"
      (detail \ "validationResponse" \ "accountExists").as[String] mustBe "yes"
      (detail \ "validationResponse" \ "nameMatches").as[String] mustBe "yes"
    }

    "send an audit event without validationResponse when rawResponse is None" in new Setup {
      stubAuth()

      await(
        service.sendBarsAuditEvent(
          bankAccountDetails = bankAccountDetails,
          bankAccountType = bankAccountType,
          rawResponse = None,
          attemptNumber = 1,
          accountStatus = "unlocked",
          checkOutcome = "pass"
        ))

      verifyAuditSent()
      val detail: JsObject = auditCaptor.getValue
      (detail \ "validationResponse").asOpt[JsObject] mustBe None
    }

    "send an audit event with locked accountStatus on third failed attempt" in new Setup {
      stubAuth()

      await(
        service.sendBarsAuditEvent(
          bankAccountDetails = bankAccountDetails,
          bankAccountType = bankAccountType,
          rawResponse = Some(barsVerificationResponse),
          attemptNumber = 3,
          accountStatus = "locked",
          checkOutcome = "fail"
        ))

      verifyAuditSent()
      val detail: JsObject = auditCaptor.getValue
      (detail \ "checkOutcome").as[String] mustBe "fail"
      (detail \ "accountStatus").as[String] mustBe "locked"
      (detail \ "attemptNumber").as[Int] mustBe 3
    }

    "send an audit event with organisation userType" in new Setup {
      stubAuth(affinity = Some(AffinityGroup.Organisation))

      await(
        service.sendBarsAuditEvent(
          bankAccountDetails = bankAccountDetails,
          bankAccountType = bankAccountType,
          rawResponse = Some(barsVerificationResponse),
          attemptNumber = 1,
          accountStatus = "unlocked",
          checkOutcome = "pass"
        ))

      verifyAuditSent()
      (auditCaptor.getValue \ "userType").as[String] mustBe "organisation"

    }

    "send an audit event with agent userType for agent" in new Setup {
      stubAuth(affinity = Some(AffinityGroup.Agent))

      await(
        service.sendBarsAuditEvent(
          bankAccountDetails = bankAccountDetails,
          bankAccountType = bankAccountType,
          rawResponse = Some(barsVerificationResponse),
          attemptNumber = 1,
          accountStatus = "unlocked",
          checkOutcome = "pass"
        ))

      verifyAuditSent()
      (auditCaptor.getValue \ "userType").as[String] mustBe "agent"

    }

    "fail with InternalServerException when credId is missing" in new Setup {
      when(mockAuthConnector.authorise[Option[Any]](any(), any())(any(), any()))
        .thenReturn(Future.successful(None))

      intercept[InternalServerException] {
        await(
          service.sendBarsAuditEvent(
            bankAccountDetails = bankAccountDetails,
            bankAccountType = bankAccountType,
            rawResponse = Some(barsVerificationResponse),
            attemptNumber = 1,
            accountStatus = "unlocked",
            checkOutcome = "pass"
          ))
      }
    }

    "fail with InternalServerException when affinity group is missing" in new Setup {
      when(mockAuthConnector.authorise[Option[Any]](any(), any())(any(), any()))
        .thenReturn(Future.successful(Some("testCredId")))
        .thenReturn(Future.successful(None))

      intercept[InternalServerException] {
        await(
          service.sendBarsAuditEvent(
            bankAccountDetails = bankAccountDetails,
            bankAccountType = bankAccountType,
            rawResponse = Some(barsVerificationResponse),
            attemptNumber = 1,
            accountStatus = "unlocked",
            checkOutcome = "pass"
          ))
      }
    }
  }
}
