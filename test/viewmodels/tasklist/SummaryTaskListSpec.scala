/*
 * Copyright 2024 HM Revenue & Customs
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

package viewmodels.tasklist

import config.FrontendAppConfig
import fixtures.VatRegistrationFixture
import models.api._
import models.api.vatapplication.{OverseasCompliance, StoringOverseas, VatApplication}
import models.{ConditionalValue, NIPTurnover, Voluntary}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import testHelpers.VatRegSpec
import play.api.mvc.Request
import play.api.test.FakeRequest

import java.time.LocalDate
import scala.concurrent.Future

class SummaryTaskListSpec(implicit appConfig: FrontendAppConfig) extends VatRegSpec with VatRegistrationFixture {
  
  implicit val fakeRequest: Request[_] = FakeRequest()
  val summaryTaskList = SummaryTaskList
  val vatRegistrationTaskList = VatRegistrationTaskList
  val businessService = mockBusinessService
  val attachmentsService = mockAttachmentsService

  "check for summary task list row" when {
    "prerequisites are not complete" must {
      "return TLCannotStart" in {
        val row = summaryTaskList.summaryRow(None, businessService).build(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(registrationReason = Voluntary))
        ))
        row.status mustBe TLCannotStart
      }
    }

    "FRS and digital attachments prerequisites are not" must {
      "return TLNotStarted" in {
        val completedVatApplicationWithGoodsAndServicesSection: VatApplication = validVatApplication.copy(
          turnoverEstimate = Some(200000),
          overseasCompliance = Some(OverseasCompliance(
            goodsToOverseas = Some(false),
            storingGoodsForDispatch = Some(StoringOverseas)
          )),
          northernIrelandProtocol = Some(NIPTurnover(
            goodsToEU = Some(ConditionalValue(answer = false, None)),
            goodsFromEU = Some(ConditionalValue(answer = false, None)),
          )),
          startDate = None
        )

        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            fixedEstablishmentInManOrUk = false
          )),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
            hasTaxRepresentative = Some(false),
            startDate = Some(LocalDate.of(2017, 10, 10))
          )),
          flatRateScheme = None
        )

        val row = summaryTaskList.summaryRow(None, businessService).build(scheme)
        row.status mustBe TLNotStarted
      }
    }

    "FRS prerequisites are met when digital attachments tasklist section not available" must {
      "return TLNotStarted" in {
        val completedVatApplicationWithGoodsAndServicesSection: VatApplication = validVatApplication.copy(
          overseasCompliance = Some(OverseasCompliance(
            goodsToOverseas = Some(false),
            storingGoodsForDispatch = Some(StoringOverseas)
          )),
          northernIrelandProtocol = Some(NIPTurnover(
            goodsToEU = Some(ConditionalValue(answer = false, None)),
            goodsFromEU = Some(ConditionalValue(answer = false, None)),
          )),
          startDate = None
        )

        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            fixedEstablishmentInManOrUk = false
          )),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
            hasTaxRepresentative = Some(false),
            startDate = Some(LocalDate.of(2017, 10, 10))
          ))
        )

        val row = summaryTaskList.summaryRow(None, businessService).build(scheme)
        row.status mustBe TLNotStarted
      }
    }

    "digital attachments prerequisites are met when digital attachments tasklist available" must {
      "return TLNotStarted" in {
        val attachmentsTaskList = AttachmentsTaskList

        val completedVatApplicationWithGoodsAndServicesSection: VatApplication = validVatApplication.copy(
          overseasCompliance = Some(OverseasCompliance(
            goodsToOverseas = Some(false),
            storingGoodsForDispatch = Some(StoringOverseas)
          )),
          northernIrelandProtocol = Some(NIPTurnover(
            goodsToEU = Some(ConditionalValue(answer = false, None)),
            goodsFromEU = Some(ConditionalValue(answer = false, None)),
          )),
          startDate = None
        )

        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            fixedEstablishmentInManOrUk = false
          )),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
            hasTaxRepresentative = Some(false),
            startDate = Some(LocalDate.of(2017, 10, 10))
          )),
          attachments = Some(Attachments(Some(Upload)))
        )

        when(mockAttachmentsService.getAttachmentList(anyString())(any(), any())).thenReturn(Future.successful(List(IdentityEvidence, VAT2)))
        when(mockAttachmentsService.getIncompleteAttachments(anyString())(any(), any())).thenReturn(Future.successful(List.empty))

        val row = summaryTaskList.summaryRow(await(attachmentsTaskList.attachmentsRequiredRow(attachmentsService, businessService)),businessService).build(scheme)
        row.status mustBe TLNotStarted
      }
    }
  }
}
