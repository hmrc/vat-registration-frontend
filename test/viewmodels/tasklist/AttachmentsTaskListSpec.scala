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

package viewmodels.tasklist

import config.FrontendAppConfig
import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import featuretoggle.FeatureToggleSupport
import fixtures.VatRegistrationFixture
import models.api._
import models.api.vatapplication.{OverseasCompliance, StoringOverseas, VatApplication}
import models.{ConditionalValue, GroupRegistration, NIPTurnover}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import testHelpers.VatRegSpec
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import services.{AttachmentsService, BusinessService}

import java.time.LocalDate
import scala.concurrent.Future

class AttachmentsTaskListSpec(implicit appConfig: FrontendAppConfig) extends VatRegSpec with VatRegistrationFixture with FeatureToggleSupport {

  implicit val fakeRequest: Request[_] = FakeRequest()

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val englishMessages: Messages = messagesApi.preferred(Seq(Lang("en")))
  lazy val welshMessages: Messages = messagesApi.preferred(Seq(Lang("cy")))

  override protected def afterEach(): Unit = {
    super.afterEach()
    disable(VrsNewAttachmentJourney)
  }

  val vatRegistrationTaskList: VatRegistrationTaskList.type = VatRegistrationTaskList
  val businessService: BusinessService = mockBusinessService
  val attatchmentService: AttachmentsService = mockAttachmentsService

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
    val section: AttachmentsTaskList.type = AttachmentsTaskList
  }

  "The attachments row" must {

    "be cannot start if the prerequesites are not complete" in new Setup {
      val scheme: VatScheme = emptyVatScheme.copy(eligibilitySubmissionData = Some(validEligibilitySubmissionData))
      when(mockAttachmentsService.getAttachmentList(anyString())(any(), any())).thenReturn(Future.successful(List(IdentityEvidence)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any(), any())).thenReturn(Future.successful(List.empty))

      val rowBuilder: Option[TaskListRowBuilder] = await(section.attachmentsRequiredRow(attatchmentService, businessService))
      val row: TaskListSectionRow = rowBuilder.get.build(scheme)

      row.status mustBe TLCannotStart
      row.url mustBe controllers.attachments.routes.DocumentsRequiredController.resolve.url
    }

    "be None when attachments are missing" in new Setup {
      when(mockAttachmentsService.getAttachmentList(anyString())(any(), any())).thenReturn(Future.successful(List.empty))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any(), any())).thenReturn(Future.successful(List.empty))

      val rowBuilder: Option[TaskListRowBuilder] = await(section.attachmentsRequiredRow(attatchmentService, businessService))

      rowBuilder mustBe None
    }

    "be TLCannotStart when eligible for FlatRate but no data available" in new Setup {
      val scheme: VatScheme = validVatScheme.copy(
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
      when(mockAttachmentsService.getAttachmentList(anyString())(any(), any())).thenReturn(Future.successful(List(IdentityEvidence)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any(), any())).thenReturn(Future.successful(List.empty))

      val rowBuilder: Option[TaskListRowBuilder] = await(section.attachmentsRequiredRow(attatchmentService, businessService))
      val row: TaskListSectionRow = rowBuilder.get.build(scheme)

      row.status mustBe TLCannotStart
      row.url mustBe controllers.attachments.routes.DocumentsRequiredController.resolve.url
    }

    "be not started when attachment method is not selected" in new Setup {
      val scheme: VatScheme = validVatScheme.copy(
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
      when(mockAttachmentsService.getAttachmentList(anyString())(any(), any())).thenReturn(Future.successful(List(IdentityEvidence)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any(), any())).thenReturn(Future.successful(List.empty))

      val rowBuilder: Option[TaskListRowBuilder] = await(section.attachmentsRequiredRow(attatchmentService, businessService))
      val row: TaskListSectionRow = rowBuilder.get.build(scheme)

      row.status mustBe TLNotStarted
      row.url mustBe controllers.attachments.routes.DocumentsRequiredController.resolve.url
    }

    "be not started when attachment method is not selected and no FlatRate prerequisite available" in new Setup {
      val scheme: VatScheme = validVatScheme.copy(
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
      when(mockAttachmentsService.getAttachmentList(anyString())(any(), any())).thenReturn(Future.successful(List(IdentityEvidence)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any(), any())).thenReturn(Future.successful(List.empty))

      val rowBuilder: Option[TaskListRowBuilder] = await(section.attachmentsRequiredRow(attatchmentService, businessService))
      val row: TaskListSectionRow = rowBuilder.get.build(scheme)

      row.status mustBe TLNotStarted
      row.url mustBe controllers.attachments.routes.DocumentsRequiredController.resolve.url
    }

    "be TLCannotStart when attachment method is not selected and not FlatRate scheme but vat returns data not complete" in new Setup {
      val scheme: VatScheme = validVatScheme.copy(
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
      when(mockAttachmentsService.getAttachmentList(anyString())(any(), any())).thenReturn(Future.successful(List(IdentityEvidence)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any(), any())).thenReturn(Future.successful(List.empty))

      val rowBuilder: Option[TaskListRowBuilder] = await(section.attachmentsRequiredRow(attatchmentService, businessService))
      val row: TaskListSectionRow = rowBuilder.get.build(scheme)

      row.status mustBe TLCannotStart
      row.url mustBe controllers.attachments.routes.DocumentsRequiredController.resolve.url
    }

    "be in progress when attachment method is digital attachment and there are incomplete attachments" in new Setup {
      val scheme: VatScheme = validVatScheme.copy(
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          otherBusinessInvolvement = Some(false),
          businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
        )),
        vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
          startDate = Some(LocalDate.of(2017, 10, 10))
        )),
        flatRateScheme = Some(validFlatRate),
        attachments = Some(Attachments(Some(Upload)))
      )
      when(mockAttachmentsService.getAttachmentList(anyString())(any(), any())).thenReturn(Future.successful(List(IdentityEvidence, VAT2)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any(), any())).thenReturn(Future.successful(List(VAT2)))

      val rowBuilder: Option[TaskListRowBuilder] = await(section.attachmentsRequiredRow(attatchmentService, businessService))
      val row: TaskListSectionRow = rowBuilder.get.build(scheme)

      row.status mustBe TLInProgress
      row.url mustBe controllers.attachments.routes.DocumentsRequiredController.resolve.url
    }

    "be completed when attachment method is digital attachment and all attachment are completed" in new Setup {
      val scheme: VatScheme = validVatScheme.copy(
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          otherBusinessInvolvement = Some(false),
          businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
        )),
        vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
          startDate = Some(LocalDate.of(2017, 10, 10))
        )),
        flatRateScheme = Some(validFlatRate),
        attachments = Some(Attachments(Some(Upload)))
      )
      when(mockAttachmentsService.getAttachmentList(anyString())(any(), any())).thenReturn(Future.successful(List(IdentityEvidence, VAT2)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any(), any())).thenReturn(Future.successful(List.empty))

      val rowBuilder: Option[TaskListRowBuilder] = await(section.attachmentsRequiredRow(attatchmentService, businessService))
      val row: TaskListSectionRow = rowBuilder.get.build(scheme)

      row.status mustBe TLCompleted
      row.url mustBe controllers.attachments.routes.DocumentsRequiredController.resolve.url
    }

    "be completed when attachment method is post" in new Setup {
      val scheme: VatScheme = validVatScheme.copy(
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
      when(mockAttachmentsService.getAttachmentList(anyString())(any(), any())).thenReturn(Future.successful(List(IdentityEvidence, VAT2)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any(), any())).thenReturn(Future.successful(List.empty))

      val rowBuilder: Option[TaskListRowBuilder] = await(section.attachmentsRequiredRow(attatchmentService,businessService))
      val row: TaskListSectionRow = rowBuilder.get.build(scheme)

      row.status mustBe TLCompleted
      row.url mustBe controllers.attachments.routes.DocumentsRequiredController.resolve.url
    }
  }

  "The attachments section" when {

    trait SectionSetup extends Setup {
      val scheme: VatScheme = emptyVatScheme.copy(eligibilitySubmissionData = Some(validEligibilitySubmissionData))
      when(mockAttachmentsService.getAttachmentList(anyString())(any(), any())).thenReturn(Future.successful(List(IdentityEvidence)))
      when(mockAttachmentsService.getIncompleteAttachments(anyString())(any(), any())).thenReturn(Future.successful(List.empty))

      val rowBuilder: TaskListRowBuilder = await(section.attachmentsRequiredRow(attatchmentService, businessService)).get
    }

    "VrsNewAttachmentJourney is enabled" must {
      "have the 'Additional Documents' heading" in new SectionSetup {
        enable(VrsNewAttachmentJourney)

        section.build(scheme, rowBuilder)(englishMessages, appConfig).heading mustBe englishMessages("tasklist.attachments.newAttachmentsJourney.heading")
      }

      "have the Welsh new attachments journey heading" in new SectionSetup {
        enable(VrsNewAttachmentJourney)

        section.build(scheme, rowBuilder)(welshMessages, appConfig).heading mustBe welshMessages("tasklist.attachments.newAttachmentsJourney.heading")
      }
    }

    "the attachments row is completed" must {

      def completedScheme(method: AttachmentMethod): VatScheme = validVatScheme.copy(
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          otherBusinessInvolvement = Some(false),
          businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
        )),
        vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
          startDate = Some(LocalDate.of(2017, 10, 10))
        )),
        flatRateScheme = Some(validFlatRate),
        attachments = Some(Attachments(Some(method)))
      )

      "use the 'Send by post' message when the attachment method is post and VrsNewAttachmentJourney is enabled" in new SectionSetup {
        enable(VrsNewAttachmentJourney)

        val row: TaskListSectionRow = section.build(completedScheme(Post), rowBuilder)(englishMessages, appConfig).rows.head

        row.status mustBe TLCompleted
        row.completedMessageKey mustBe Some("tasklist.sendByPost")
      }

      "use the default completed message when the attachment method is post and VrsNewAttachmentJourney is disabled" in new SectionSetup {
        disable(VrsNewAttachmentJourney)

        val row: TaskListSectionRow = section.build(completedScheme(Post), rowBuilder)(englishMessages, appConfig).rows.head

        row.status mustBe TLCompleted
        row.completedMessageKey mustBe None
      }

      "use the default completed message when the attachment method is upload" in new SectionSetup {
        enable(VrsNewAttachmentJourney)

        val row: TaskListSectionRow = section.build(completedScheme(Upload), rowBuilder)(englishMessages, appConfig).rows.head

        row.status mustBe TLCompleted
        row.completedMessageKey mustBe None
      }
    }

    "the attachments row is not completed and the attachment method is post" must {
      "use the default message" in new SectionSetup {
        enable(VrsNewAttachmentJourney)

        val row: TaskListSectionRow = section.build(scheme.copy(attachments = Some(Attachments(Some(Post)))), rowBuilder)(englishMessages, appConfig).rows.head

        row.status mustBe TLCannotStart
        row.completedMessageKey mustBe None
      }
    }

    "VrsNewAttachmentJourney is disabled" must {
      "have the 'Attachments' heading" in new SectionSetup {
        disable(VrsNewAttachmentJourney)

        section.build(scheme, rowBuilder)(englishMessages, appConfig).heading mustBe englishMessages("tasklist.attachments.heading")
      }

      "have the Welsh 'Atodiadau' heading" in new SectionSetup {
        disable(VrsNewAttachmentJourney)

        section.build(scheme, rowBuilder)(welshMessages, appConfig).heading mustBe welshMessages("tasklist.attachments.heading")
      }
    }
  }
}
