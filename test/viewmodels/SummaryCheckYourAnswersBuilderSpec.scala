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
import models.api.VatScheme
import models.view.SummaryListRowUtils.optSummaryListRowString
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.i18n.{Lang, Messages, MessagesApi}
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.accordion.{Accordion, Section}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class SummaryCheckYourAnswersBuilderSpec extends VatRegSpec {

  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  val govukSummaryList = new GovukSummaryList
  val mockEligibilitySummaryBuilder: EligibilitySummaryBuilder = mock[EligibilitySummaryBuilder]
  val mockGrsSummaryBuilder: GrsSummaryBuilder = mock[GrsSummaryBuilder]
  val mockTransactorDetailsSummaryBuilder: TransactorDetailsSummaryBuilder = mock[TransactorDetailsSummaryBuilder]
  val mockApplicantDetailsSummaryBuilder: ApplicantDetailsSummaryBuilder = mock[ApplicantDetailsSummaryBuilder]
  val mockAboutTheBusinessSummaryBuilder: AboutTheBusinessSummaryBuilder = mock[AboutTheBusinessSummaryBuilder]
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
  val vatRegDetailsId = "vatRegDetails"

  class Setup {
    object Builder extends SummaryCheckYourAnswersBuilder(
      govukSummaryList,
      mockEligibilitySummaryBuilder,
      mockGrsSummaryBuilder,
      mockTransactorDetailsSummaryBuilder,
      mockApplicantDetailsSummaryBuilder,
      mockAboutTheBusinessSummaryBuilder,
      mockRegistrationDetailsSummaryBuilder
    )
  }

  "generateSummaryAccordion" must {
    "combine the summary sections into an accordion" in new Setup {
      when(mockEligibilitySummaryBuilder.build(ArgumentMatchers.eq(fullEligibilityDataJson), ArgumentMatchers.eq(validVatScheme.id))(ArgumentMatchers.eq(messages)))
        .thenReturn(testSummaryList(eligibilityId))
      when(mockTransactorDetailsSummaryBuilder.build(ArgumentMatchers.eq(validVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(SummaryList())
      when(mockGrsSummaryBuilder.build(ArgumentMatchers.eq(validVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(testSummaryList(verifyBusinessId))
      when(mockApplicantDetailsSummaryBuilder.build(ArgumentMatchers.eq(validVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(testSummaryList(applicantId))
      when(mockAboutTheBusinessSummaryBuilder.build(ArgumentMatchers.eq(validVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(testSummaryList(aboutBusinessId))
      when(mockRegistrationDetailsSummaryBuilder.build(ArgumentMatchers.eq(validVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(testSummaryList(vatRegDetailsId))

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

      val result: Accordion = Builder.generateSummaryAccordion(validVatScheme, fullEligibilityDataJson)

      result mustBe expectedAccordion
    }

    "combine the summary sections into an accordion for a transactor" in new Setup {
      val testVatScheme: VatScheme = validVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(isTransactor = true)),
        transactorDetails = Some(validTransactorDetails)
      )

      when(mockEligibilitySummaryBuilder.build(ArgumentMatchers.eq(fullEligibilityDataJson), ArgumentMatchers.eq(testVatScheme.id))(ArgumentMatchers.eq(messages)))
        .thenReturn(testSummaryList(eligibilityId))
      when(mockTransactorDetailsSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(testSummaryList(transactorId))
      when(mockGrsSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(testSummaryList(verifyBusinessId))
      when(mockApplicantDetailsSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(testSummaryList(applicantId))
      when(mockAboutTheBusinessSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(testSummaryList(aboutBusinessId))
      when(mockRegistrationDetailsSummaryBuilder.build(ArgumentMatchers.eq(testVatScheme))(ArgumentMatchers.eq(messages)))
        .thenReturn(testSummaryList(vatRegDetailsId))

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
            headingContent = Text("Applicant details"),
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

      val result: Accordion = Builder.generateSummaryAccordion(testVatScheme, fullEligibilityDataJson)

      result mustBe expectedAccordion
    }
  }
}
