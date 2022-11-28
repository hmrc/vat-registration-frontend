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

package viewmodels

import config.FrontendAppConfig
import models.OtherBusinessInvolvement
import models.api.VatScheme
import models.view.SummaryListRowUtils.optSummaryListRowString
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.JsObject
import play.twirl.api.HtmlFormat
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.accordion.{Accordion, Section}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class SummaryCheckYourAnswersBuilderSpec extends VatRegSpec {

  val govukSummaryList: GovukSummaryList = app.injector.instanceOf[GovukSummaryList]
  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  val mockEligibilitySummaryBuilder: EligibilitySummaryBuilder = mock[EligibilitySummaryBuilder]
  val mockGrsSummaryBuilder: GrsSummaryBuilder = mock[GrsSummaryBuilder]
  val mockTransactorDetailsSummaryBuilder: TransactorDetailsSummaryBuilder = mock[TransactorDetailsSummaryBuilder]
  val mockApplicantDetailsSummaryBuilder: ApplicantDetailsSummaryBuilder = mock[ApplicantDetailsSummaryBuilder]
  val mockAboutTheBusinessSummaryBuilder: AboutTheBusinessSummaryBuilder = mock[AboutTheBusinessSummaryBuilder]
  val mockOtherBusinessInvolvementSummaryBuilder: OtherBusinessInvolvementSummaryBuilder = mock[OtherBusinessInvolvementSummaryBuilder]
  val mockRegistrationDetailsSummaryBuilder: RegistrationDetailsSummaryBuilder = mock[RegistrationDetailsSummaryBuilder]

  def testSummaryList(id: String): SummaryList = SummaryList(optSummaryListRowString(
    id,
    Some("testAnswer"),
    Some("testUrl")
  ).toSeq)

  val eligibilityId = "eligibility"
  val transactorId = "transactor"
  val verifyBusinessId = "verifyBusiness"
  val applicantId = "applicant"
  val aboutBusinessId = "aboutBusiness"
  val otherBusinessId = "otherBusiness"
  val vatRegDetailsId = "vatRegDetails"

  class Setup {
    object Builder extends SummaryCheckYourAnswersBuilder(
      mockEligibilitySummaryBuilder,
      mockGrsSummaryBuilder,
      mockTransactorDetailsSummaryBuilder,
      mockApplicantDetailsSummaryBuilder,
      mockAboutTheBusinessSummaryBuilder,
      mockOtherBusinessInvolvementSummaryBuilder,
      mockRegistrationDetailsSummaryBuilder
    )
  }

  val testVrn = "testVrn"

  val testObi: OtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some(testCompanyName),
    hasVrn = Some(true),
    vrn = Some(testVrn),
    stillTrading = Some(true)
  )

  val testObiSection = List(testObi, testObi)
  val testVatScheme: VatScheme = validVatScheme.copy(eligibilityData = Some(fullEligibilityDataJson.as[JsObject]))

  "generateSummaryAccordion" must {
    "combine the summary sections into an accordion" in new Setup {
      when(mockEligibilitySummaryBuilder.build(ArgumentMatchers.eq(fullEligibilityDataJson), ArgumentMatchers.eq(testVatScheme.registrationId))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(eligibilityId)))))
      when(mockTransactorDetailsSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.empty)
      when(mockGrsSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(verifyBusinessId)))))
      when(mockApplicantDetailsSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(applicantId)))))
      when(mockAboutTheBusinessSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages), ArgumentMatchers.eq(appConfig)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(aboutBusinessId)))))
      when(mockOtherBusinessInvolvementSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.empty)
      when(mockRegistrationDetailsSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(vatRegDetailsId)))))

      val expectedAccordion: Accordion = Accordion(
        items = Seq(
          Section(
            headingContent = Text("Registration reason"),
            content = HtmlContent(govukSummaryList(testSummaryList(eligibilityId)))
          ),
          Section(
            headingContent = Text("Verify your business"),
            content = HtmlContent(govukSummaryList(testSummaryList(verifyBusinessId)))
          ),
          Section(
            headingContent = Text("About you"),
            content = HtmlContent(govukSummaryList(testSummaryList(applicantId)))
          ),
          Section(
            headingContent = Text("About the business"),
            content = HtmlContent(govukSummaryList(testSummaryList(aboutBusinessId)))
          ),
          Section(
            headingContent = Text("VAT Registration Details"),
            content = HtmlContent(govukSummaryList(testSummaryList(vatRegDetailsId)))
          )
        )
      )

      val result: Accordion = Builder.generateSummaryAccordion(testVatScheme)

      result mustBe expectedAccordion
    }

    "combine the summary sections into an accordion when the user has other business involvements" in new Setup {
      when(mockEligibilitySummaryBuilder.build(ArgumentMatchers.eq(fullEligibilityDataJson), ArgumentMatchers.eq(testVatScheme.registrationId))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(eligibilityId)))))
      when(mockTransactorDetailsSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.empty)
      when(mockGrsSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(verifyBusinessId)))))
      when(mockApplicantDetailsSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(applicantId)))))
      when(mockAboutTheBusinessSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages), ArgumentMatchers.eq(appConfig)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(aboutBusinessId)))))
      when(mockOtherBusinessInvolvementSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(otherBusinessId)))))
      when(mockRegistrationDetailsSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(vatRegDetailsId)))))

      val expectedAccordion: Accordion = Accordion(
        items = Seq(
          Section(
            headingContent = Text("Registration reason"),
            content = HtmlContent(govukSummaryList(testSummaryList(eligibilityId)))
          ),
          Section(
            headingContent = Text("Verify your business"),
            content = HtmlContent(govukSummaryList(testSummaryList(verifyBusinessId)))
          ),
          Section(
            headingContent = Text("About you"),
            content = HtmlContent(govukSummaryList(testSummaryList(applicantId)))
          ),
          Section(
            headingContent = Text("About the business"),
            content = HtmlContent(govukSummaryList(testSummaryList(aboutBusinessId)))
          ),
          Section(
            headingContent = Text("Other business involvements"),
            content = HtmlContent(govukSummaryList(testSummaryList(otherBusinessId)))
          ),
          Section(
            headingContent = Text("VAT Registration Details"),
            content = HtmlContent(govukSummaryList(testSummaryList(vatRegDetailsId)))
          )
        )
      )

      val result: Accordion = Builder.generateSummaryAccordion(testVatScheme)

      result mustBe expectedAccordion
    }

    "combine the summary sections into an accordion for a transactor" in new Setup {
      val testTransactorVatScheme: VatScheme = testVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(isTransactor = true)),
        transactorDetails = Some(validTransactorDetails)
      )

      when(mockEligibilitySummaryBuilder.build(ArgumentMatchers.eq(fullEligibilityDataJson), ArgumentMatchers.eq(testTransactorVatScheme.registrationId))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(eligibilityId)))))
      when(mockTransactorDetailsSummaryBuilder.build(ArgumentMatchers.eq(testTransactorVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(transactorId)))))
      when(mockGrsSummaryBuilder.build(ArgumentMatchers.eq(testTransactorVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(verifyBusinessId)))))
      when(mockApplicantDetailsSummaryBuilder.build(ArgumentMatchers.eq(testTransactorVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(applicantId)))))
      when(mockAboutTheBusinessSummaryBuilder.build(ArgumentMatchers.eq(testTransactorVatScheme))(ArgumentMatchers.eq(messages), ArgumentMatchers.eq(appConfig)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(aboutBusinessId)))))
      when(mockOtherBusinessInvolvementSummaryBuilder.build(ArgumentMatchers.eq(testTransactorVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.empty)
      when(mockRegistrationDetailsSummaryBuilder.build(ArgumentMatchers.eq(testTransactorVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(HtmlFormat.fill(List(govukSummaryList(testSummaryList(vatRegDetailsId)))))

      val expectedAccordion: Accordion = Accordion(
        items = Seq(
          Section(
            headingContent = Text("Registration reason"),
            content = HtmlContent(govukSummaryList(testSummaryList(eligibilityId)))
          ),
          Section(
            headingContent = Text("About you"),
            content = HtmlContent(govukSummaryList(testSummaryList(transactorId)))
          ),
          Section(
            headingContent = Text("Verify your business"),
            content = HtmlContent(govukSummaryList(testSummaryList(verifyBusinessId)))
          ),
          Section(
            headingContent = Text("About the business contact"),
            content = HtmlContent(govukSummaryList(testSummaryList(applicantId)))
          ),
          Section(
            headingContent = Text("About the business"),
            content = HtmlContent(govukSummaryList(testSummaryList(aboutBusinessId)))
          ),
          Section(
            headingContent = Text("VAT Registration Details"),
            content = HtmlContent(govukSummaryList(testSummaryList(vatRegDetailsId)))
          )
        )
      )

      val result: Accordion = Builder.generateSummaryAccordion(testTransactorVatScheme)

      result mustBe expectedAccordion
    }
  }
}
