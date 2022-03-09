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
import models.PartnerEntity
import models.api._
import models.view.SummaryListRowUtils.optSummaryListRowString
import play.api.i18n.{Lang, Messages, MessagesApi}
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class ApplicantDetailsSummaryBuilderSpec extends VatRegSpec {

  class Setup {
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

    val applicantDetailsSummaryBuilder = new ApplicantDetailsSummaryBuilder()
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
          optSummaryListRowString(
            questionId = "Home address",
            optAnswer = Some("Testline1<br>Testline2<br>TE 1ST"),
            optUrl = Some("/register-for-vat/home-address/international")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Previous address",
            optAnswer = Some("Testline11<br>Testline22<br>TE1 1ST"),
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

        val result: SummaryList = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedSummaryList
      }
    }
    "called with Individual lead partner" must {
      "return the summary list for Individual" in new Setup {
        val testLeadPartnerIndividual: PartnerEntity = PartnerEntity(
          testSoleTrader,
          Individual,
          isLeadPartner = true
        )
        val vatScheme: VatScheme = validVatScheme.copy(
          partners = Some(List(testLeadPartnerIndividual)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedIndividualSummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "First Name",
            optAnswer = Some("testFirstName"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "Last Name",
            optAnswer = Some("testLastName"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "Date of birth",
            optAnswer = Some("1 January 2020"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "National Insurance number",
            optAnswer = Some("AB123456C"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
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
          optSummaryListRowString(
            questionId = "Home address",
            optAnswer = Some("Testline1<br>Testline2<br>TE 1ST"),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Previous address",
            optAnswer = Some("Testline11<br>Testline22<br>TE1 1ST"),
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

        val result: SummaryList = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedIndividualSummaryList
      }
    }
    "called with NETP lead partner" must {
      "return the summary list for NETP" in new Setup {
        val testLeadPartnerNetp: PartnerEntity = PartnerEntity(
          testSoleTrader,
          NETP,
          isLeadPartner = true
        )
        val vatScheme: VatScheme = validVatScheme.copy(
          partners = Some(List(testLeadPartnerNetp)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedNETPSummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "First Name",
            optAnswer = Some("testFirstName"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "Last Name",
            optAnswer = Some("testLastName"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "Date of birth",
            optAnswer = Some("1 January 2020"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "National Insurance number",
            optAnswer = Some("AB123456C"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
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
          optSummaryListRowString(
            questionId = "Home address",
            optAnswer = Some("Testline1<br>Testline2<br>TE 1ST"),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Previous address",
            optAnswer = Some("Testline11<br>Testline22<br>TE1 1ST"),
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

        val result: SummaryList = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedNETPSummaryList
      }
    }
    "called with Limited company lead partner" must {
      "return the summary list for Limited company" in new Setup {
        val testLeadPartnerLtdCompany: PartnerEntity = PartnerEntity(
          testLimitedCompany,
          UkCompany,
          isLeadPartner = true
        )
        val vatScheme: VatScheme = validVatScheme.copy(
          partners = Some(List(testLeadPartnerLtdCompany)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedLtdCompanySummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Lead partner’s company Unique Taxpayer Reference",
            optAnswer = Some("testCtUtr"),
            optUrl = Some("/register-for-vat/start-incorp-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company registration number",
            optAnswer = Some("testCrn"),
            optUrl = Some("/register-for-vat/start-incorp-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company name",
            optAnswer = Some("testCompanyName"),
            optUrl = Some("/register-for-vat/start-incorp-id-partner-journey")),
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
          optSummaryListRowString(
            questionId = "Home address",
            optAnswer = Some("Testline1<br>Testline2<br>TE 1ST"),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Previous address",
            optAnswer = Some("Testline11<br>Testline22<br>TE1 1ST"),
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

        val result: SummaryList = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedLtdCompanySummaryList
      }
    }
    "called with Scottish partnership lead partner" must {
      "return the summary list for Scottish partnership" in new Setup {
        val testLeadPartnerScotPartnership: PartnerEntity = PartnerEntity(
          testGeneralPartnership,
          ScotPartnership,
          isLeadPartner = true
        )
        val vatScheme: VatScheme = validVatScheme.copy(
          partners = Some(List(testLeadPartnerScotPartnership)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedScotPartnershipSummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s registered postcode",
            optAnswer = Some("AA11AA"),
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey")),
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
          optSummaryListRowString(
            questionId = "Home address",
            optAnswer = Some("Testline1<br>Testline2<br>TE 1ST"),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Previous address",
            optAnswer = Some("Testline11<br>Testline22<br>TE1 1ST"),
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

        val result: SummaryList = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedScotPartnershipSummaryList
      }
    }
    "called with Scottish limited partnership lead partner" must {
      "return the summary list for Scottish limited partnership" in new Setup {
        val testLeadPartnerScotLtdPartnership: PartnerEntity = PartnerEntity(
          testGeneralPartnership.copy(companyNumber = Some("1234567890"), companyName = Some("testPartnershipName")),
          ScotLtdPartnership,
          isLeadPartner = true
        )
        val vatScheme: VatScheme = validVatScheme.copy(
          partners = Some(List(testLeadPartnerScotLtdPartnership)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedScotLtdPartnershipSummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company number",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s Partnership name",
            optAnswer = Some("testPartnershipName"),
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s registered postcode for self assessment",
            optAnswer = Some("AA11AA"),
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey")),
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
          optSummaryListRowString(
            questionId = "Home address",
            optAnswer = Some("Testline1<br>Testline2<br>TE 1ST"),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Previous address",
            optAnswer = Some("Testline11<br>Testline22<br>TE1 1ST"),
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

        val result: SummaryList = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedScotLtdPartnershipSummaryList
      }
    }
    "called with Limited liability partnership lead partner" must {
      "return the summary list for Limited liability partnership" in new Setup {
        val testLeadPartnerLtdLiabilityPartnership: PartnerEntity = PartnerEntity(
          testGeneralPartnership.copy(companyNumber = Some("1234567890"), companyName = Some("testPartnershipName")),
          LtdLiabilityPartnership,
          isLeadPartner = true
        )
        val vatScheme: VatScheme = validVatScheme.copy(
          partners = Some(List(testLeadPartnerLtdLiabilityPartnership)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedLtdLiabilityPartnershipSummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company number",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s Partnership name",
            optAnswer = Some("testPartnershipName"),
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s registered postcode for self assessment",
            optAnswer = Some("AA11AA"),
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey")),
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
          optSummaryListRowString(
            questionId = "Home address",
            optAnswer = Some("Testline1<br>Testline2<br>TE 1ST"),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Previous address",
            optAnswer = Some("Testline11<br>Testline22<br>TE1 1ST"),
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

        val result: SummaryList = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedLtdLiabilityPartnershipSummaryList
      }
    }
    "called with Charitable org lead partner" must {
      "return the summary list for Charitable org" in new Setup {
        val testLeadPartnerCharitableOrg: PartnerEntity = PartnerEntity(
          testCharitableOrganisation,
          CharitableOrg,
          isLeadPartner = true
        )
        val vatScheme: VatScheme = validVatScheme.copy(
          partners = Some(List(testLeadPartnerCharitableOrg)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedCharitableOrgSummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Lead partner’s company registration number",
            optAnswer = Some("testCrn"),
            optUrl = Some("/register-for-vat/start-incorp-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company name",
            optAnswer = Some("testCompanyName"),
            optUrl = Some("/register-for-vat/start-incorp-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Charity’s HMRC reference number",
            optAnswer = Some("testChrn"),
            optUrl = Some("/register-for-vat/start-incorp-id-partner-journey")),
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
          optSummaryListRowString(
            questionId = "Home address",
            optAnswer = Some("Testline1<br>Testline2<br>TE 1ST"),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Previous address",
            optAnswer = Some("Testline11<br>Testline22<br>TE1 1ST"),
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

        val result: SummaryList = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedCharitableOrgSummaryList
      }
    }
    "called with Registered society lead partner" must {
      "return the summary list for Registered society" in new Setup {
        val testLeadPartnerLtdCompany: PartnerEntity = PartnerEntity(
          testRegisteredSociety,
          RegSociety,
          isLeadPartner = true
        )
        val vatScheme: VatScheme = validVatScheme.copy(
          partners = Some(List(testLeadPartnerLtdCompany)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership))
        )
        val expectedRegSocietySummaryList: SummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("testCtUtr"),
            optUrl = Some("/register-for-vat/start-incorp-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company registration number",
            optAnswer = Some("testCrn"),
            optUrl = Some("/register-for-vat/start-incorp-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s company name",
            optAnswer = Some("testCompanyName"),
            optUrl = Some("/register-for-vat/start-incorp-id-partner-journey")),
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
          optSummaryListRowString(
            questionId = "Home address",
            optAnswer = Some("Testline1<br>Testline2<br>TE 1ST"),
            optUrl = Some("/register-for-vat/home-address")),
          optSummaryListRowString(
            questionId = "Lived at current address for more than 3 years",
            optAnswer = Some("No"),
            optUrl = Some("/register-for-vat/current-address")),
          optSummaryListRowString(
            questionId = "Previous address",
            optAnswer = Some("Testline11<br>Testline22<br>TE1 1ST"),
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

        val result: SummaryList = applicantDetailsSummaryBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedRegSocietySummaryList
      }
    }
  }
}
