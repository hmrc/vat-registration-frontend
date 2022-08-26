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

import fixtures.VatRegistrationFixture
import models.api.vatapplication.{OverseasCompliance, StoringOverseas, VatApplication}
import models.api._
import models.{ConditionalValue, GroupRegistration, NIPTurnover}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import testHelpers.VatRegSpec

import java.time.LocalDate
import scala.concurrent.Future

class AttachmentsTaskListSpec extends VatRegSpec with VatRegistrationFixture {

  val vatRegistrationTaskList = app.injector.instanceOf[VatRegistrationTaskList]

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

  trait Setup {
    val section: AttachmentsTaskList = new AttachmentsTaskList(vatRegistrationTaskList, mockAttachmentsService)
  }

  "The attachments row" must {

    "be cannot start if the prerequesites are not complete" in new Setup {
      val scheme = emptyVatScheme.copy(eligibilitySubmissionData = Some(validEligibilitySubmissionData))
      when(mockAttachmentsService.getAttachmentList(anyString())(any())).thenReturn(Future.successful(List(IdentityEvidence)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any())).thenReturn(Future.successful(List.empty))

      val rowBuilder = await(section.attachmentsRequiredRow)
      val row = rowBuilder.get.build(scheme)

      row.status mustBe TLCannotStart
      row.url mustBe controllers.attachments.routes.AttachmentMethodController.show.url
    }

    "be None when attachments are missing" in new Setup {
      when(mockAttachmentsService.getAttachmentList(anyString())(any())).thenReturn(Future.successful(List.empty))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any())).thenReturn(Future.successful(List.empty))

      val rowBuilder = await(section.attachmentsRequiredRow)

      rowBuilder mustBe None
    }

    "be TLCannotStart when eligible for FlatRate but no data available" in new Setup {
      val scheme = validVatScheme.copy(
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          otherBusinessInvolvement = Some(false),
          businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
        )),
        vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
          startDate = Some(LocalDate.of(2017, 10, 10))
        )),
        attachments = None,
        flatRateScheme = None
      )
      when(mockAttachmentsService.getAttachmentList(anyString())(any())).thenReturn(Future.successful(List(IdentityEvidence)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any())).thenReturn(Future.successful(List.empty))

      val rowBuilder = await(section.attachmentsRequiredRow)
      val row = rowBuilder.get.build(scheme)

      row.status mustBe TLCannotStart
      row.url mustBe controllers.attachments.routes.AttachmentMethodController.show.url
    }

    "be not started when attachment method is not selected" in new Setup {
      val scheme = validVatScheme.copy(
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          otherBusinessInvolvement = Some(false),
          businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
        )),
        vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
          startDate = Some(LocalDate.of(2017, 10, 10))
        )),
        flatRateScheme = Some(validFlatRate),
        attachments = None
      )
      when(mockAttachmentsService.getAttachmentList(anyString())(any())).thenReturn(Future.successful(List(IdentityEvidence)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any())).thenReturn(Future.successful(List.empty))

      val rowBuilder = await(section.attachmentsRequiredRow)
      val row = rowBuilder.get.build(scheme)

      row.status mustBe TLNotStarted
      row.url mustBe controllers.attachments.routes.AttachmentMethodController.show.url
    }

    "be not started when attachment method is not selected and no FlatRate prerequisite available" in new Setup {
      val scheme = validVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(registrationReason = GroupRegistration)),
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          otherBusinessInvolvement = Some(false),
          businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
        )),
        vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
          startDate = Some(LocalDate.of(2017, 10, 10))
        )),
        attachments = None
      )
      when(mockAttachmentsService.getAttachmentList(anyString())(any())).thenReturn(Future.successful(List(IdentityEvidence)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any())).thenReturn(Future.successful(List.empty))

      val rowBuilder = await(section.attachmentsRequiredRow)
      val row = rowBuilder.get.build(scheme)

      row.status mustBe TLNotStarted
      row.url mustBe controllers.attachments.routes.AttachmentMethodController.show.url
    }

    "be TLCannotStart when attachment method is not selected and not FlatRate scheme but vat returns data not complete" in new Setup {
      val scheme = validVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(registrationReason = GroupRegistration)),
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          otherBusinessInvolvement = Some(false),
          businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
        )),
        vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
          returnsFrequency = None,
          startDate = Some(LocalDate.of(2017, 10, 10))
        )),
        attachments = None
      )
      when(mockAttachmentsService.getAttachmentList(anyString())(any())).thenReturn(Future.successful(List(IdentityEvidence)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any())).thenReturn(Future.successful(List.empty))

      val rowBuilder = await(section.attachmentsRequiredRow)
      val row = rowBuilder.get.build(scheme)

      row.status mustBe TLCannotStart
      row.url mustBe controllers.attachments.routes.AttachmentMethodController.show.url
    }

    "be in progress when attachment method is digital attachment and there are incomplete attachments" in new Setup {
      val scheme = validVatScheme.copy(
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
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any())).thenReturn(Future.successful(List(VAT2)))

      val rowBuilder = await(section.attachmentsRequiredRow)
      val row = rowBuilder.get.build(scheme)

      row.status mustBe TLInProgress
      row.url mustBe controllers.attachments.routes.AttachmentMethodController.show.url
    }

    "be completed when attachment method is digital attachment and all attachment are completed" in new Setup {
      val scheme = validVatScheme.copy(
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

      val rowBuilder = await(section.attachmentsRequiredRow)
      val row = rowBuilder.get.build(scheme)

      row.status mustBe TLCompleted
      row.url mustBe controllers.attachments.routes.AttachmentMethodController.show.url
    }

    "be completed when attachment method is post" in new Setup {
      val scheme = validVatScheme.copy(
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          otherBusinessInvolvement = Some(false),
          businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
        )),
        vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
          startDate = Some(LocalDate.of(2017, 10, 10))
        )),
        flatRateScheme = Some(validFlatRate),
        attachments = Some(Attachments(Some(Post)))
      )
      when(mockAttachmentsService.getAttachmentList(anyString())(any())).thenReturn(Future.successful(List(IdentityEvidence, VAT2)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any())).thenReturn(Future.successful(List.empty))

      val rowBuilder = await(section.attachmentsRequiredRow)
      val row = rowBuilder.get.build(scheme)

      row.status mustBe TLCompleted
      row.url mustBe controllers.attachments.routes.AttachmentMethodController.show.url
    }

    "be completed when attachment method is email" in new Setup {
      val scheme = validVatScheme.copy(
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          otherBusinessInvolvement = Some(false),
          businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
        )),
        vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
          startDate = Some(LocalDate.of(2017, 10, 10))
        )),
        flatRateScheme = Some(validFlatRate),
        attachments = Some(Attachments(Some(EmailMethod)))
      )
      when(mockAttachmentsService.getAttachmentList(anyString())(any())).thenReturn(Future.successful(List(IdentityEvidence, VAT2)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any())).thenReturn(Future.successful(List.empty))

      val rowBuilder = await(section.attachmentsRequiredRow)
      val row = rowBuilder.get.build(scheme)

      row.status mustBe TLCompleted
      row.url mustBe controllers.attachments.routes.AttachmentMethodController.show.url
    }
  }
}
