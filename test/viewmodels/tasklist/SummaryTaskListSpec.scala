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

package viewmodels.tasklist

import models.{ConditionalValue, NIPTurnover, Voluntary}
import testHelpers.VatRegSpec
import fixtures.VatRegistrationFixture
import models.api.{Attached, Attachments, IdentityEvidence, NETP, VAT2}
import models.api.vatapplication.{OverseasCompliance, StoringOverseas, VatApplication}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when

import java.time.LocalDate
import scala.concurrent.Future

class SummaryTaskListSpec extends VatRegSpec with VatRegistrationFixture {
  val summaryTaskList: SummaryTaskList = app.injector.instanceOf[SummaryTaskList]
  val vatRegistrationTaskList: VatRegistrationTaskList = app.injector.instanceOf[VatRegistrationTaskList]

  "check for summary task list row" when {
    "prerequisites are not complete" must {
      "return TLCannotStart" in {
        val row = summaryTaskList.summaryRow(None).build(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(registrationReason = Voluntary))
        ))
        row.status mustBe TLCannotStart
      }
    }

    "FRS prerequisites are met when digital attachments tasklist section not available" must {
      "return TLCompleted" in {
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
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = NETP)),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
            startDate = Some(LocalDate.of(2017, 10, 10))
          )),
          flatRateScheme = Some(validFlatRate)
        )

        val row = summaryTaskList.summaryRow(None).build(scheme)
        row.status mustBe TLCompleted
      }
    }

    "digital attachments prerequisites are met when digital attachments tasklist available" must {
      "return TLCompleted" in {
        val attachmentsTaskList: AttachmentsTaskList = new AttachmentsTaskList(vatRegistrationTaskList, mockAttachmentsService)

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
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = NETP)),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
            startDate = Some(LocalDate.of(2017, 10, 10))
          )),
          flatRateScheme = Some(validFlatRate),
          attachments = Some(Attachments(Some(Attached)))
        )

        when(mockAttachmentsService.getAttachmentList(anyString())(any())).thenReturn(Future.successful(List(IdentityEvidence, VAT2)))
        when(mockAttachmentsService.getIncompleteAttachments(anyString())(any())).thenReturn(Future.successful(List.empty))

        val row = summaryTaskList.summaryRow(await(attachmentsTaskList.attachmentsRequiredRow)).build(scheme)
        row.status mustBe TLCompleted
      }
    }
  }
}
