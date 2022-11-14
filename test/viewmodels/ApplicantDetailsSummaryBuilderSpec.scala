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
import models.Entity
import models.api._
import models.view.SummaryListRowUtils.{optSummaryListRow, optSummaryListRowString}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.HtmlFormat
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class ApplicantDetailsSummaryBuilderSpec extends VatRegSpec {

  class Setup {
    val govukSummaryList = app.injector.instanceOf[GovukSummaryList]
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

    val applicantDetailsSummaryBuilder = new ApplicantDetailsSummaryBuilder(govukSummaryList)
  }

  "SummaryApplicantDetailsBuilder" when {
    "called by a NETP" must {
      "return the summary list without partner details" in new Setup {
        val vatScheme: VatScheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = NETP))
        )
        val expectedSummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "First Name",
            optAnswer = Some("testFirstName"),
            optUrl = Some("/register-for-vat/start-sti-journey")),
          optSummaryListRowString(
            questionId = "Last Name",
            optAnswer = Some("testLastName"),
            optUrl = Some("/register-for-vat/start-sti-journey")),
          optSummaryListRowString(
            questionId = "Date of birth",
            optAnswer = Some("1 January 2020"),
            optUrl = Some("/register-for-vat/start-sti-journey")),
          optSummaryListRowString(
            questionId = "National Insurance number",
            optAnswer = Some("AB123456C"),
            optUrl = Some("/register-for-vat/start-sti-journey")),
          optSummaryListRowString(
            questionId = "Changed name",
            optAnswer = Some("Yes"),
            optUrl = Some("/register-for-vat/changed-name")),
          optSummaryListRowString(
            questionId = "Former name",
            optAnswer = Some("New Name Cosmo"),
            optUrl = Some("/register-for-vat/what-was-previous-name")),
          optSummaryListRowString(
            questionId = "Date former name changed",
            optAnswer = Some("12 July 2000"),
            optUrl = Some("/register-for-vat/when-change")),
          optSummaryListRow(
            questionId = "Home address",
            optAnswer = Some(HtmlContent("Testline1<br>Testline2<br>TE 1ST")),
            optUrl = Some("/register-for-vat/home-address/international")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRow(
            questionId = "Previous address",
            optAnswer = Some(HtmlContent("Testline11<br>Testline22<br>TE1 1ST")),
            optUrl = Some("/register-for-vat/previous-address/international")),
          optSummaryListRowString(
            questionId = "Email address",
            optAnswer = Some("test@t.test"),
            optUrl = Some("/register-for-vat/email-address")),
          optSummaryListRowString(
            questionId = "Telephone number",
            optAnswer = Some("1234"),
            optUrl = Some("/register-for-vat/telephone-number"))
        ).flatten)

        val result = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe HtmlFormat.fill(List(govukSummaryList(expectedSummaryList)))
      }
    }
    "called with Individual lead partner" must {
      "return the summary list for Individual" in new Setup {
        val testLeadPartnerIndividual: Entity = Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None)
        val vatScheme: VatScheme = validVatScheme.copy(
          entities = Some(List(testLeadPartnerIndividual)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedIndividualSummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Partner type",
            optAnswer = Some("An actual person"),
            optUrl = Some("/register-for-vat/lead-partner-entity")),
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/partner/1/start-sti-journey")),
          optSummaryListRowString(
            questionId = "First Name",
            optAnswer = Some("testFirstName"),
            optUrl = Some("/register-for-vat/partner/1/start-sti-journey")),
          optSummaryListRowString(
            questionId = "Last Name",
            optAnswer = Some("testLastName"),
            optUrl = Some("/register-for-vat/partner/1/start-sti-journey")),
          optSummaryListRowString(
            questionId = "Date of birth",
            optAnswer = Some("1 January 2020"),
            optUrl = Some("/register-for-vat/partner/1/start-sti-journey")),
          optSummaryListRowString(
            questionId = "National Insurance number",
            optAnswer = Some("AB123456C"),
            optUrl = Some("/register-for-vat/partner/1/start-sti-journey")),
          optSummaryListRowString(
            questionId = "Role in the business",
            optAnswer = Some("Director"),
            optUrl = Some("/register-for-vat/role-in-the-business")),
          optSummaryListRowString(
            questionId = "Changed name",
            optAnswer = Some("Yes"),
            optUrl = Some("/register-for-vat/changed-name")),
          optSummaryListRowString(
            questionId = "Former name",
            optAnswer = Some("New Name Cosmo"),
            optUrl = Some("/register-for-vat/what-was-previous-name")),
          optSummaryListRowString(
            questionId = "Date former name changed",
            optAnswer = Some("12 July 2000"),
            optUrl = Some("/register-for-vat/when-change")),
          optSummaryListRow(
            questionId = "Home address",
            optAnswer = Some(HtmlContent("Testline1<br>Testline2<br>TE 1ST")),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRow(
            questionId = "Previous address",
            optAnswer = Some(HtmlContent("Testline11<br>Testline22<br>TE1 1ST")),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Email address",
            optAnswer = Some("test@t.test"),
            optUrl = Some("/register-for-vat/email-address")),
          optSummaryListRowString(
            questionId = "Telephone number",
            optAnswer = Some("1234"),
            optUrl = Some("/register-for-vat/telephone-number"))
        ).flatten)

        val result = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe HtmlFormat.fill(List(govukSummaryList(expectedIndividualSummaryList)))
      }
    }
    "called with NETP lead partner" must {
      "return the summary list for NETP" in new Setup {
        val testLeadPartnerNetp: Entity = Entity(Some(testSoleTrader), NETP, Some(true), None, None, None, None)
        val vatScheme: VatScheme = validVatScheme.copy(
          entities = Some(List(testLeadPartnerNetp)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedNETPSummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Partner type",
            optAnswer = Some("An actual person"),
            optUrl = Some("/register-for-vat/lead-partner-entity")),
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/partner/1/start-sti-journey")),
          optSummaryListRowString(
            questionId = "First Name",
            optAnswer = Some("testFirstName"),
            optUrl = Some("/register-for-vat/partner/1/start-sti-journey")),
          optSummaryListRowString(
            questionId = "Last Name",
            optAnswer = Some("testLastName"),
            optUrl = Some("/register-for-vat/partner/1/start-sti-journey")),
          optSummaryListRowString(
            questionId = "Date of birth",
            optAnswer = Some("1 January 2020"),
            optUrl = Some("/register-for-vat/partner/1/start-sti-journey")),
          optSummaryListRowString(
            questionId = "National Insurance number",
            optAnswer = Some("AB123456C"),
            optUrl = Some("/register-for-vat/partner/1/start-sti-journey")),
          optSummaryListRowString(
            questionId = "Role in the business",
            optAnswer = Some("Director"),
            optUrl = Some("/register-for-vat/role-in-the-business")),
          optSummaryListRowString(
            questionId = "Changed name",
            optAnswer = Some("Yes"),
            optUrl = Some("/register-for-vat/changed-name")),
          optSummaryListRowString(
            questionId = "Former name",
            optAnswer = Some("New Name Cosmo"),
            optUrl = Some("/register-for-vat/what-was-previous-name")),
          optSummaryListRowString(
            questionId = "Date former name changed",
            optAnswer = Some("12 July 2000"),
            optUrl = Some("/register-for-vat/when-change")),
          optSummaryListRow(
            questionId = "Home address",
            optAnswer = Some(HtmlContent("Testline1<br>Testline2<br>TE 1ST")),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRow(
            questionId = "Previous address",
            optAnswer = Some(HtmlContent("Testline11<br>Testline22<br>TE1 1ST")),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Email address",
            optAnswer = Some("test@t.test"),
            optUrl = Some("/register-for-vat/email-address")),
          optSummaryListRowString(
            questionId = "Telephone number",
            optAnswer = Some("1234"),
            optUrl = Some("/register-for-vat/telephone-number"))
        ).flatten)

        val result = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe HtmlFormat.fill(List(govukSummaryList(expectedNETPSummaryList)))
      }
    }
    "called with Limited company lead partner" must {
      "return the summary list for Limited company" in new Setup {
        val testLeadPartnerLtdCompany: Entity = Entity(Some(testLimitedCompany), UkCompany, Some(true), None, None, None, None)
        val vatScheme: VatScheme = validVatScheme.copy(
          entities = Some(List(testLeadPartnerLtdCompany)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedLtdCompanySummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Partner type",
            optAnswer = Some("A business"),
            optUrl = Some("/register-for-vat/lead-partner-entity")),
          optSummaryListRowString(
            questionId = "Business type",
            optAnswer = Some("UK company"),
            optUrl = Some("/register-for-vat/business-type-in-partnership")),
          optSummaryListRowString(
            questionId = "Lead partner’s company Unique Taxpayer Reference",
            optAnswer = Some("testCtUtr"),
            optUrl = Some("/register-for-vat/partner/1/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company registration number",
            optAnswer = Some("testCrn"),
            optUrl = Some("/register-for-vat/partner/1/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company name",
            optAnswer = Some("testCompanyName"),
            optUrl = Some("/register-for-vat/partner/1/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "First Name",
            optAnswer = Some("testFirstName"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Last Name",
            optAnswer = Some("testLastName"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Date of birth",
            optAnswer = Some("1 January 2020"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "National Insurance number",
            optAnswer = Some("AB123456C"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Role in the business",
            optAnswer = Some("Director"),
            optUrl = Some("/register-for-vat/role-in-the-business")),
          optSummaryListRowString(
            questionId = "Changed name",
            optAnswer = Some("Yes"),
            optUrl = Some("/register-for-vat/changed-name")),
          optSummaryListRowString(
            questionId = "Former name",
            optAnswer = Some("New Name Cosmo"),
            optUrl = Some("/register-for-vat/what-was-previous-name")),
          optSummaryListRowString(
            questionId = "Date former name changed",
            optAnswer = Some("12 July 2000"),
            optUrl = Some("/register-for-vat/when-change")),
          optSummaryListRow(
            questionId = "Home address",
            optAnswer = Some(HtmlContent("Testline1<br>Testline2<br>TE 1ST")),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRow(
            questionId = "Previous address",
            optAnswer = Some(HtmlContent("Testline11<br>Testline22<br>TE1 1ST")),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Email address",
            optAnswer = Some("test@t.test"),
            optUrl = Some("/register-for-vat/email-address")),
          optSummaryListRowString(
            questionId = "Telephone number",
            optAnswer = Some("1234"),
            optUrl = Some("/register-for-vat/telephone-number"))
        ).flatten)

        val result = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe HtmlFormat.fill(List(govukSummaryList(expectedLtdCompanySummaryList)))
      }
    }
    "called with Scottish partnership lead partner" must {
      "return the summary list for Scottish partnership" in new Setup {
        val testLeadPartnerScotPartnership: Entity = Entity(Some(testGeneralPartnership), ScotPartnership, Some(true), None, None, None, None)
        val vatScheme: VatScheme = validVatScheme.copy(
          entities = Some(List(testLeadPartnerScotPartnership)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedScotPartnershipSummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Partner type",
            optAnswer = Some("A business"),
            optUrl = Some("/register-for-vat/lead-partner-entity")),
          optSummaryListRowString(
            questionId = "Business type",
            optAnswer = Some("Scottish partnership"),
            optUrl = Some("/register-for-vat/business-type-in-partnership")),
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/partner/1/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s registered postcode",
            optAnswer = Some("AA11AA"),
            optUrl = Some("/register-for-vat/partner/1/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "First Name",
            optAnswer = Some("testFirstName"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Last Name",
            optAnswer = Some("testLastName"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Date of birth",
            optAnswer = Some("1 January 2020"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "National Insurance number",
            optAnswer = Some("AB123456C"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Role in the business",
            optAnswer = Some("Director"),
            optUrl = Some("/register-for-vat/role-in-the-business")),
          optSummaryListRowString(
            questionId = "Changed name",
            optAnswer = Some("Yes"),
            optUrl = Some("/register-for-vat/changed-name")),
          optSummaryListRowString(
            questionId = "Former name",
            optAnswer = Some("New Name Cosmo"),
            optUrl = Some("/register-for-vat/what-was-previous-name")),
          optSummaryListRowString(
            questionId = "Date former name changed",
            optAnswer = Some("12 July 2000"),
            optUrl = Some("/register-for-vat/when-change")),
          optSummaryListRow(
            questionId = "Home address",
            optAnswer = Some(HtmlContent("Testline1<br>Testline2<br>TE 1ST")),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRow(
            questionId = "Previous address",
            optAnswer = Some(HtmlContent("Testline11<br>Testline22<br>TE1 1ST")),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Email address",
            optAnswer = Some("test@t.test"),
            optUrl = Some("/register-for-vat/email-address")),
          optSummaryListRowString(
            questionId = "Telephone number",
            optAnswer = Some("1234"),
            optUrl = Some("/register-for-vat/telephone-number"))
        ).flatten)

        val result = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe HtmlFormat.fill(List(govukSummaryList(expectedScotPartnershipSummaryList)))
      }
    }
    "called with Scottish limited partnership lead partner" must {
      "return the summary list for Scottish limited partnership" in new Setup {
        val testLeadPartnerScotLtdPartnership: Entity = Entity(
          Some(testGeneralPartnership.copy(companyNumber = Some("1234567890"), companyName = Some("testPartnershipName"))),
          ScotLtdPartnership,
          Some(true),
          None,
          None,
          None,
          None
        )
        val vatScheme: VatScheme = validVatScheme.copy(
          entities = Some(List(testLeadPartnerScotLtdPartnership)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedScotLtdPartnershipSummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Partner type",
            optAnswer = Some("A business"),
            optUrl = Some("/register-for-vat/lead-partner-entity")),
          optSummaryListRowString(
            questionId = "Business type",
            optAnswer = Some("Scottish limited partnership"),
            optUrl = Some("/register-for-vat/business-type-in-partnership")),
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/partner/1/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company number",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/partner/1/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s Partnership name",
            optAnswer = Some("testPartnershipName"),
            optUrl = Some("/register-for-vat/partner/1/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s registered postcode for self assessment",
            optAnswer = Some("AA11AA"),
            optUrl = Some("/register-for-vat/partner/1/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "First Name",
            optAnswer = Some("testFirstName"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Last Name",
            optAnswer = Some("testLastName"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Date of birth",
            optAnswer = Some("1 January 2020"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "National Insurance number",
            optAnswer = Some("AB123456C"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Role in the business",
            optAnswer = Some("Director"),
            optUrl = Some("/register-for-vat/role-in-the-business")),
          optSummaryListRowString(
            questionId = "Changed name",
            optAnswer = Some("Yes"),
            optUrl = Some("/register-for-vat/changed-name")),
          optSummaryListRowString(
            questionId = "Former name",
            optAnswer = Some("New Name Cosmo"),
            optUrl = Some("/register-for-vat/what-was-previous-name")),
          optSummaryListRowString(
            questionId = "Date former name changed",
            optAnswer = Some("12 July 2000"),
            optUrl = Some("/register-for-vat/when-change")),
          optSummaryListRow(
            questionId = "Home address",
            optAnswer = Some(HtmlContent("Testline1<br>Testline2<br>TE 1ST")),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRow(
            questionId = "Previous address",
            optAnswer = Some(HtmlContent("Testline11<br>Testline22<br>TE1 1ST")),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Email address",
            optAnswer = Some("test@t.test"),
            optUrl = Some("/register-for-vat/email-address")),
          optSummaryListRowString(
            questionId = "Telephone number",
            optAnswer = Some("1234"),
            optUrl = Some("/register-for-vat/telephone-number"))
        ).flatten)

        val result = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe HtmlFormat.fill(List(govukSummaryList(expectedScotLtdPartnershipSummaryList)))
      }
    }
    "called with Limited liability partnership lead partner" must {
      "return the summary list for Limited liability partnership" in new Setup {
        val testLeadPartnerLtdLiabilityPartnership: Entity = Entity(
          Some(testGeneralPartnership.copy(companyNumber = Some("1234567890"), companyName = Some("testPartnershipName"))),
          LtdLiabilityPartnership,
          Some(true),
          None,
          None,
          None,
          None
        )
        val vatScheme: VatScheme = validVatScheme.copy(
          entities = Some(List(testLeadPartnerLtdLiabilityPartnership)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedLtdLiabilityPartnershipSummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Partner type",
            optAnswer = Some("A business"),
            optUrl = Some("/register-for-vat/lead-partner-entity")),
          optSummaryListRowString(
            questionId = "Business type",
            optAnswer = Some("Limited liability partnership"),
            optUrl = Some("/register-for-vat/business-type-in-partnership")),
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/partner/1/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company number",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/partner/1/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s Partnership name",
            optAnswer = Some("testPartnershipName"),
            optUrl = Some("/register-for-vat/partner/1/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s registered postcode for self assessment",
            optAnswer = Some("AA11AA"),
            optUrl = Some("/register-for-vat/partner/1/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "First Name",
            optAnswer = Some("testFirstName"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Last Name",
            optAnswer = Some("testLastName"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Date of birth",
            optAnswer = Some("1 January 2020"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "National Insurance number",
            optAnswer = Some("AB123456C"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Role in the business",
            optAnswer = Some("Director"),
            optUrl = Some("/register-for-vat/role-in-the-business")),
          optSummaryListRowString(
            questionId = "Changed name",
            optAnswer = Some("Yes"),
            optUrl = Some("/register-for-vat/changed-name")),
          optSummaryListRowString(
            questionId = "Former name",
            optAnswer = Some("New Name Cosmo"),
            optUrl = Some("/register-for-vat/what-was-previous-name")),
          optSummaryListRowString(
            questionId = "Date former name changed",
            optAnswer = Some("12 July 2000"),
            optUrl = Some("/register-for-vat/when-change")),
          optSummaryListRow(
            questionId = "Home address",
            optAnswer = Some(HtmlContent("Testline1<br>Testline2<br>TE 1ST")),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRow(
            questionId = "Previous address",
            optAnswer = Some(HtmlContent("Testline11<br>Testline22<br>TE1 1ST")),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Email address",
            optAnswer = Some("test@t.test"),
            optUrl = Some("/register-for-vat/email-address")),
          optSummaryListRowString(
            questionId = "Telephone number",
            optAnswer = Some("1234"),
            optUrl = Some("/register-for-vat/telephone-number"))
        ).flatten)

        val result = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe HtmlFormat.fill(List(govukSummaryList(expectedLtdLiabilityPartnershipSummaryList)))
      }
    }
    "called with Charitable org lead partner" must {
      "return the summary list for Charitable org" in new Setup {
        val testLeadPartnerCharitableOrg: Entity = Entity(Some(testCharitableOrganisation), CharitableOrg, Some(true), None, None, None, None)
        val vatScheme: VatScheme = validVatScheme.copy(
          entities = Some(List(testLeadPartnerCharitableOrg)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedCharitableOrgSummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Partner type",
            optAnswer = Some("A business"),
            optUrl = Some("/register-for-vat/lead-partner-entity")),
          optSummaryListRowString(
            questionId = "Business type",
            optAnswer = Some("Charitable Incorporated Organisation (CIO)"),
            optUrl = Some("/register-for-vat/business-type-in-partnership")),
          optSummaryListRowString(
            questionId = "Lead partner’s company registration number",
            optAnswer = Some("testCrn"),
            optUrl = Some("/register-for-vat/partner/1/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company name",
            optAnswer = Some("testCompanyName"),
            optUrl = Some("/register-for-vat/partner/1/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "Charity’s HMRC reference number",
            optAnswer = Some("testChrn"),
            optUrl = Some("/register-for-vat/partner/1/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "First Name",
            optAnswer = Some("testFirstName"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Last Name",
            optAnswer = Some("testLastName"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Date of birth",
            optAnswer = Some("1 January 2020"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "National Insurance number",
            optAnswer = Some("AB123456C"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Role in the business",
            optAnswer = Some("Director"),
            optUrl = Some("/register-for-vat/role-in-the-business")),
          optSummaryListRowString(
            questionId = "Changed name",
            optAnswer = Some("Yes"),
            optUrl = Some("/register-for-vat/changed-name")),
          optSummaryListRowString(
            questionId = "Former name",
            optAnswer = Some("New Name Cosmo"),
            optUrl = Some("/register-for-vat/what-was-previous-name")),
          optSummaryListRowString(
            questionId = "Date former name changed",
            optAnswer = Some("12 July 2000"),
            optUrl = Some("/register-for-vat/when-change")),
          optSummaryListRow(
            questionId = "Home address",
            optAnswer = Some(HtmlContent("Testline1<br>Testline2<br>TE 1ST")),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRow(
            questionId = "Previous address",
            optAnswer = Some(HtmlContent("Testline11<br>Testline22<br>TE1 1ST")),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Email address",
            optAnswer = Some("test@t.test"),
            optUrl = Some("/register-for-vat/email-address")),
          optSummaryListRowString(
            questionId = "Telephone number",
            optAnswer = Some("1234"),
            optUrl = Some("/register-for-vat/telephone-number"))
        ).flatten)

        val result = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe HtmlFormat.fill(List(govukSummaryList(expectedCharitableOrgSummaryList)))
      }
    }
    "called with Registered society lead partner" must {
      "return the summary list for Registered society" in new Setup {
        val testLeadPartnerLtdCompany: Entity = Entity(Some(testRegisteredSociety), RegSociety, Some(true), None, None, None, None)
        val vatScheme: VatScheme = validVatScheme.copy(
          entities = Some(List(testLeadPartnerLtdCompany)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedRegSocietySummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Partner type",
            optAnswer = Some("A business"),
            optUrl = Some("/register-for-vat/lead-partner-entity")),
          optSummaryListRowString(
            questionId = "Business type",
            optAnswer = Some("Registered society"),
            optUrl = Some("/register-for-vat/business-type-in-partnership")),
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("testCtUtr"),
            optUrl = Some("/register-for-vat/partner/1/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company registration number",
            optAnswer = Some("testCrn"),
            optUrl = Some("/register-for-vat/partner/1/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company name",
            optAnswer = Some("testCompanyName"),
            optUrl = Some("/register-for-vat/partner/1/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "First Name",
            optAnswer = Some("testFirstName"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Last Name",
            optAnswer = Some("testLastName"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Date of birth",
            optAnswer = Some("1 January 2020"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "National Insurance number",
            optAnswer = Some("AB123456C"),
            optUrl = Some("/register-for-vat/start-sti-individual-journey")),
          optSummaryListRowString(
            questionId = "Role in the business",
            optAnswer = Some("Director"),
            optUrl = Some("/register-for-vat/role-in-the-business")),
          optSummaryListRowString(
            questionId = "Changed name",
            optAnswer = Some("Yes"),
            optUrl = Some("/register-for-vat/changed-name")),
          optSummaryListRowString(
            questionId = "Former name",
            optAnswer = Some("New Name Cosmo"),
            optUrl = Some("/register-for-vat/what-was-previous-name")),
          optSummaryListRowString(
            questionId = "Date former name changed",
            optAnswer = Some("12 July 2000"),
            optUrl = Some("/register-for-vat/when-change")),
          optSummaryListRow(
            questionId = "Home address",
            optAnswer = Some(HtmlContent("Testline1<br>Testline2<br>TE 1ST")),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRow(
            questionId = "Previous address",
            optAnswer = Some(HtmlContent("Testline11<br>Testline22<br>TE1 1ST")),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Email address",
            optAnswer = Some("test@t.test"),
            optUrl = Some("/register-for-vat/email-address")),
          optSummaryListRowString(
            questionId = "Telephone number",
            optAnswer = Some("1234"),
            optUrl = Some("/register-for-vat/telephone-number"))
        ).flatten)

        val result = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe HtmlFormat.fill(List(govukSummaryList(expectedRegSocietySummaryList)))
      }
    }
  }
}
