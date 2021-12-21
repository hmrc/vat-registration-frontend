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
import models.api.{CharitableOrg, Individual, LtdLiabilityPartnership, NETP, RegSociety, ScotLtdPartnership, ScotPartnership, UkCompany}
import models.view.SummaryListRowUtils.optSummaryListRowString
import play.api.i18n.{Lang, Messages, MessagesApi}
import testHelpers.VatRegSpec

class SummaryCheckYourAnswersBuilderSpec extends VatRegSpec {

  class Setup {
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  }

  "leadPartnershipSection" when {

    "called with Individual party type" should {
      "return the summary list for Individual" in new Setup {
        val testLeadPartnerIndividual: PartnerEntity = PartnerEntity(
          testSoleTrader,
          Individual,
          isLeadPartner = true
        )
        val vatScheme = validVatScheme.copy(partners = Some(List(testLeadPartnerIndividual)))
        val expectedIndividualSummaryList = Seq(
          optSummaryListRowString(
            questionId = "Lead partner’s first name",
            optAnswer = Some("testFirstName"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s last name",
            optAnswer = Some("testLastName"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s date of birth",
            optAnswer = Some("1 January 2020"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s National Insurance number",
            optAnswer = Some("AB123456C"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey"))
        ).flatten

        val summaryCheckYourAnswersBuilder = new SummaryCheckYourAnswersBuilder(configConnector = mockConfigConnector, flatRateService = mockFlatRateService)
        val result = summaryCheckYourAnswersBuilder.leadPartnershipSection(vatScheme = vatScheme, partyType = Individual, messages = messages)
        result mustBe expectedIndividualSummaryList
      }
    }

    "called with NETP party type" should {
      "return the summary list for NETP" in new Setup {
        val testLeadPartnerNetp: PartnerEntity = PartnerEntity(
          testSoleTrader,
          NETP,
          isLeadPartner = true
        )
        val vatScheme = validVatScheme.copy(partners = Some(List(testLeadPartnerNetp)))
        val expectedNetpSummaryList = Seq(
          optSummaryListRowString(
            questionId = "Lead partner’s first name",
            optAnswer = Some("testFirstName"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s last name",
            optAnswer = Some("testLastName"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s date of birth",
            optAnswer = Some("1 January 2020"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s National Insurance number",
            optAnswer = Some("AB123456C"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/start-sti-partner-journey"))
        ).flatten

        val summaryCheckYourAnswersBuilder = new SummaryCheckYourAnswersBuilder(configConnector = mockConfigConnector, flatRateService = mockFlatRateService)
        val result = summaryCheckYourAnswersBuilder.leadPartnershipSection(vatScheme = vatScheme, partyType = NETP, messages = messages)
        result mustBe expectedNetpSummaryList
      }
    }

    "called with Limited company party type" should {
      "return the summary list for Limited company" in new Setup {
        val testLeadPartnerLtdCompany: PartnerEntity = PartnerEntity(
          testLimitedCompany,
          UkCompany,
          isLeadPartner = true
        )
        val vatScheme = validVatScheme.copy(partners = Some(List(testLeadPartnerLtdCompany)))
        val expectedLtdCompanySummaryList = Seq(
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
            optUrl = Some("/register-for-vat/start-incorp-id-partner-journey"))
        ).flatten

        val summaryCheckYourAnswersBuilder = new SummaryCheckYourAnswersBuilder(configConnector = mockConfigConnector, flatRateService = mockFlatRateService)
        val result = summaryCheckYourAnswersBuilder.leadPartnershipSection(vatScheme = vatScheme, partyType = UkCompany, messages = messages)
        result mustBe expectedLtdCompanySummaryList
      }
    }

    "called with Scottish partnership party type" should {
      "return the summary list for Scottish partnership" in new Setup {
        val testLeadPartnerScotPartnership: PartnerEntity = PartnerEntity(
          testGeneralPartnership,
          ScotPartnership,
          isLeadPartner = true
        )
        val vatScheme = validVatScheme.copy(partners = Some(List(testLeadPartnerScotPartnership)))
        val expectedScotPartnershipSummaryList = Seq(
          optSummaryListRowString(
            questionId = "Lead partner’s Unique Taxpayer Reference",
            optAnswer = Some("1234567890"),
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey")),
          optSummaryListRowString(
            questionId = "Lead partner’s registered postcode",
            optAnswer = Some("AA11AA"),
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey"))
        ).flatten

        val summaryCheckYourAnswersBuilder = new SummaryCheckYourAnswersBuilder(configConnector = mockConfigConnector, flatRateService = mockFlatRateService)
        val result = summaryCheckYourAnswersBuilder.leadPartnershipSection(vatScheme = vatScheme, partyType = ScotPartnership, messages = messages)
        result mustBe expectedScotPartnershipSummaryList
      }
    }

    "called with Scottish limited partnership party type" should {
      "return the summary list for Scottish limited partnership" in new Setup {
        val testLeadPartnerScotLtdPartnership: PartnerEntity = PartnerEntity(
          testGeneralPartnership.copy(companyNumber = Some("1234567890"), companyName = Some("testPartnershipName")),
          ScotLtdPartnership,
          isLeadPartner = true
        )
        val vatScheme = validVatScheme.copy(partners = Some(List(testLeadPartnerScotLtdPartnership)))
        val expectedScotLtdPartnershipSummaryList = Seq(
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
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey"))
        ).flatten

        val summaryCheckYourAnswersBuilder = new SummaryCheckYourAnswersBuilder(configConnector = mockConfigConnector, flatRateService = mockFlatRateService)
        val result = summaryCheckYourAnswersBuilder.leadPartnershipSection(vatScheme = vatScheme, partyType = ScotLtdPartnership, messages = messages)
        result mustBe expectedScotLtdPartnershipSummaryList
      }
    }

    "called with Limited liability partnership party type" should {
      "return the summary list for Limited liability partnership" in new Setup {
        val testLeadPartnerScotLtdPartnership: PartnerEntity = PartnerEntity(
          testGeneralPartnership.copy(companyNumber = Some("1234567890"), companyName = Some("testPartnershipName")),
          LtdLiabilityPartnership,
          isLeadPartner = true
        )
        val vatScheme = validVatScheme.copy(partners = Some(List(testLeadPartnerScotLtdPartnership)))
        val expectedLtdLiabilityPartnershipSummaryList = Seq(
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
            optUrl = Some("/register-for-vat/start-partnership-id-partner-journey"))
        ).flatten

        val summaryCheckYourAnswersBuilder = new SummaryCheckYourAnswersBuilder(configConnector = mockConfigConnector, flatRateService = mockFlatRateService)
        val result = summaryCheckYourAnswersBuilder.leadPartnershipSection(vatScheme = vatScheme, partyType = LtdLiabilityPartnership, messages = messages)
        result mustBe expectedLtdLiabilityPartnershipSummaryList
      }
    }

    "called with Charitable org party type" should {
      "return the summary list for Charitable org" in new Setup {
        val testLeadPartnerLtdCompany: PartnerEntity = PartnerEntity(
          testCharitableOrganisation,
          CharitableOrg,
          isLeadPartner = true
        )
        val vatScheme = validVatScheme.copy(partners = Some(List(testLeadPartnerLtdCompany)))
        val expectedCharitableOrgSummaryList = Seq(
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
            optUrl = Some("/register-for-vat/start-incorp-id-partner-journey"))
        ).flatten

        val summaryCheckYourAnswersBuilder = new SummaryCheckYourAnswersBuilder(configConnector = mockConfigConnector, flatRateService = mockFlatRateService)
        val result = summaryCheckYourAnswersBuilder.leadPartnershipSection(vatScheme = vatScheme, partyType = CharitableOrg, messages = messages)
        result mustBe expectedCharitableOrgSummaryList
      }
    }

    "called with Registered society party type" should {
      "return the summary list for Registered society" in new Setup {
        val testLeadPartnerLtdCompany: PartnerEntity = PartnerEntity(
          testRegisteredSociety,
          RegSociety,
          isLeadPartner = true
        )
        val vatScheme = validVatScheme.copy(partners = Some(List(testLeadPartnerLtdCompany)))
        val expectedRegSocietySummaryList = Seq(
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
            optUrl = Some("/register-for-vat/start-incorp-id-partner-journey"))
        ).flatten

        val summaryCheckYourAnswersBuilder = new SummaryCheckYourAnswersBuilder(configConnector = mockConfigConnector, flatRateService = mockFlatRateService)
        val result = summaryCheckYourAnswersBuilder.leadPartnershipSection(vatScheme = vatScheme, partyType = RegSociety, messages = messages)
        result mustBe expectedRegSocietySummaryList
      }
    }
  }

}
