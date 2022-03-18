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
import models._
import models.api._
import models.external._
import models.view.SummaryListRowUtils.optSummaryListRowString
import org.mockito.Mockito.when
import play.api.i18n.{Lang, Messages, MessagesApi}
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class GrsSummaryBuilderSpec extends VatRegSpec {

  class Setup {
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

    val grsCheckYourAnswersBuilder = new GrsSummaryBuilder(configConnector = mockConfigConnector)
  }

  "generateGrsDetailsSummaryListRows" when {

    //Incorporated Entity
    "called with Limited company party type" should {
      "return the summary list for Limited company" in new Setup {
        val testLtdCompany: ApplicantDetails = ApplicantDetails(
          entity = Some(IncorporatedEntity(
            companyNumber = testCrn,
            companyName = Some(testCompanyName),
            ctutr = Some(testCtUtr),
            chrn = None,
            dateOfIncorporation = Some(testIncorpDate),
            countryOfIncorporation = testIncorpCountry,
            identifiersMatch = true,
            registration = testRegistration,
            businessVerification = Some(BvPass),
            bpSafeId = Some(testBpSafeId)
          ))
        )
        val vatScheme: VatScheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = UkCompany
          )),
          applicantDetails = Some(testLtdCompany)
        )
        val expectedLtdCompanySummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Company registration number",
            optAnswer = Some(testCrn),
            optUrl = Some("/register-for-vat/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "Official business name",
            optAnswer = Some(testCompanyName),
            optUrl = Some("/register-for-vat/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "Unique Taxpayer Reference number",
            optAnswer = Some(testCtUtr),
            optUrl = Some("/register-for-vat/start-incorp-id-journey"))
        ).flatten)

        val result = grsCheckYourAnswersBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedLtdCompanySummaryList
      }
    }
    "called with Charitable org party type" should {
      "return the summary list for Charitable org" in new Setup {
        val testCharitableOrg: ApplicantDetails = ApplicantDetails(
          entity = Some(IncorporatedEntity(
            companyNumber = testCrn,
            companyName = Some(testCompanyName),
            ctutr = None,
            chrn = None,
            dateOfIncorporation = Some(testIncorpDate),
            countryOfIncorporation = testIncorpCountry,
            identifiersMatch = true,
            registration = testRegistration,
            businessVerification = Some(BvPass),
            bpSafeId = Some(testBpSafeId)
          ))
        )
        val vatScheme: VatScheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = CharitableOrg
          )),
          applicantDetails = Some(testCharitableOrg)
        )
        val expectedCharitableOrgSummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Company registration number",
            optAnswer = Some(testCrn),
            optUrl = Some("/register-for-vat/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "Official business name",
            optAnswer = Some(testCompanyName),
            optUrl = Some("/register-for-vat/start-incorp-id-journey"))
        ).flatten)

        val result = grsCheckYourAnswersBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedCharitableOrgSummaryList
      }
    }
    "called with Registered society party type" should {
      "return the summary list for Registered society" in new Setup {
        val testRegSociety: ApplicantDetails = ApplicantDetails(
          entity = Some(IncorporatedEntity(
            companyNumber = testCrn,
            companyName = Some(testCompanyName),
            ctutr = Some(testCtUtr),
            chrn = None,
            dateOfIncorporation = Some(testIncorpDate),
            countryOfIncorporation = testIncorpCountry,
            identifiersMatch = true,
            registration = testRegistration,
            businessVerification = Some(BvPass),
            bpSafeId = Some(testBpSafeId)
          ))
        )
        val vatScheme: VatScheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = RegSociety
          )),
          applicantDetails = Some(testRegSociety)
        )
        val expectedRegSocietySummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Company registration number",
            optAnswer = Some(testCrn),
            optUrl = Some("/register-for-vat/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "Official business name",
            optAnswer = Some(testCompanyName),
            optUrl = Some("/register-for-vat/start-incorp-id-journey")),
          optSummaryListRowString(
            questionId = "Unique Taxpayer Reference number",
            optAnswer = Some(testCtUtr),
            optUrl = Some("/register-for-vat/start-incorp-id-journey"))
        ).flatten)

        val result = grsCheckYourAnswersBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedRegSocietySummaryList
      }
    }

    //Sole Trader Identification Entity
    "called with Individual party type" should {
      "return the summary list for Individual" in new Setup {
        val testIndividual: ApplicantDetails = ApplicantDetails(
          entity = Some(SoleTraderIdEntity(
            firstName = testFirstName,
            lastName = testLastName,
            dateOfBirth = testApplicantDob,
            nino = Some(testApplicantNino),
            sautr = Some(testSautr),
            trn = None,
            registration = testRegistration,
            businessVerification = Some(BvPass),
            bpSafeId = Some(testSafeId),
            identifiersMatch = true
          ))
        )
        val vatScheme: VatScheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = Individual
          )),
          applicantDetails = Some(testIndividual)
        )
        val expectedIndividualSummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Unique Taxpayer Reference number",
            optAnswer = Some(testSautr),
            optUrl = Some("/register-for-vat/start-sti-journey"))
        ).flatten)

        val result = grsCheckYourAnswersBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedIndividualSummaryList
      }
    }
    "called with NETP party type" should {
      "return the summary list for NETP" in new Setup {
        val testNetp: ApplicantDetails = ApplicantDetails(
          entity = Some(SoleTraderIdEntity(
            firstName = testFirstName,
            lastName = testLastName,
            dateOfBirth = testApplicantDob,
            nino = None,
            sautr = Some(testSautr),
            trn = Some(testTrn),
            registration = testRegistration,
            businessVerification = Some(BvPass),
            bpSafeId = Some(testSafeId),
            identifiersMatch = true
          ))
        )
        val vatScheme: VatScheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP
          )),
          applicantDetails = Some(testNetp)
        )
        val expectedNetpSummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Unique Taxpayer Reference number",
            optAnswer = Some(testSautr),
            optUrl = Some("/register-for-vat/start-sti-journey"))
        ).flatten)

        val result = grsCheckYourAnswersBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedNetpSummaryList
      }
    }

    //Partnership Identification Entity
    "called with General Partnership party type" should {
      "return the summary list for General Partnership" in new Setup {
        val testGenPartnership: ApplicantDetails = ApplicantDetails(
          entity = Some(PartnershipIdEntity(
            sautr = Some(testSautr),
            companyName = Some(testCompanyName),
            postCode = Some(testPostcode),
            registration = testRegistration,
            businessVerification = Some(BvPass),
            bpSafeId = Some(testSafeId),
            identifiersMatch = true
          ))
        )
        val vatScheme: VatScheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = Partnership
          )),
          applicantDetails = Some(testGenPartnership)
        )

        val expectedGeneralPartnershipSummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Official business name",
            optAnswer = Some(testCompanyName),
            optUrl = Some("/register-for-vat/partnership-official-name")),
          optSummaryListRowString(
            questionId = "Unique Taxpayer Reference number",
            optAnswer = Some(testSautr),
            optUrl = Some("/register-for-vat/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Partnership’s registered postcode for self assessment",
            optAnswer = Some(testPostcode),
            optUrl = Some("/register-for-vat/start-partnership-id-journey"))
        ).flatten)

        val result = grsCheckYourAnswersBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedGeneralPartnershipSummaryList
      }
    }
    "called with Limited Partnership party type" should {
      "return the summary list for Limited Partnership" in new Setup {
        val testLtdPartnership: ApplicantDetails = ApplicantDetails(
          entity = Some(PartnershipIdEntity(
            sautr = Some(testSautr),
            companyNumber = Some(testCrn),
            companyName = Some(testCompanyName),
            postCode = Some(testPostcode),
            registration = testRegistration,
            businessVerification = Some(BvPass),
            bpSafeId = Some(testSafeId),
            identifiersMatch = true
          ))
        )
        val vatScheme: VatScheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = LtdPartnership
          )),
          applicantDetails = Some(testLtdPartnership)
        )
        val expectedLimitedPartnershipSummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Company registration number",
            optAnswer = Some(testCrn),
            optUrl = Some("/register-for-vat/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Official business name",
            optAnswer = Some(testCompanyName),
            optUrl = Some("/register-for-vat/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Unique Taxpayer Reference number",
            optAnswer = Some(testSautr),
            optUrl = Some("/register-for-vat/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Partnership’s registered postcode for self assessment",
            optAnswer = Some(testPostcode),
            optUrl = Some("/register-for-vat/start-partnership-id-journey"))
        ).flatten)

        val result = grsCheckYourAnswersBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedLimitedPartnershipSummaryList
      }
    }
    "called with Scottish Partnership party type" should {
      "return the summary list for Scottish Partnership" in new Setup {
        val testScotPartnership: ApplicantDetails = ApplicantDetails(
          entity = Some(PartnershipIdEntity(
            sautr = Some(testSautr),
            companyName = Some(testCompanyName),
            postCode = Some(testPostcode),
            registration = testRegistration,
            businessVerification = Some(BvPass),
            bpSafeId = Some(testSafeId),
            identifiersMatch = true
          ))
        )
        val vatScheme: VatScheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = ScotPartnership
          )),
          applicantDetails = Some(testScotPartnership)
        )
        val expectedScottishPartnershipSummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Official business name",
            optAnswer = Some(testCompanyName),
            optUrl = Some("/register-for-vat/partnership-official-name")),
          optSummaryListRowString(
            questionId = "Unique Taxpayer Reference number",
            optAnswer = Some(testSautr),
            optUrl = Some("/register-for-vat/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Partnership’s registered postcode for self assessment",
            optAnswer = Some(testPostcode),
            optUrl = Some("/register-for-vat/start-partnership-id-journey"))
        ).flatten)

        val result = grsCheckYourAnswersBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedScottishPartnershipSummaryList
      }
    }
    "called with Scottish Limited Partnership party type" should {
      "return the summary list for Scottish Limited Partnership" in new Setup {
        val testScotLtdPartnership: ApplicantDetails = ApplicantDetails(
          entity = Some(PartnershipIdEntity(
            sautr = Some(testSautr),
            companyNumber = Some(testCrn),
            companyName = Some(testCompanyName),
            postCode = Some(testPostcode),
            registration = testRegistration,
            businessVerification = Some(BvPass),
            bpSafeId = Some(testSafeId),
            identifiersMatch = true
          ))
        )
        val vatScheme: VatScheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = ScotLtdPartnership
          )),
          applicantDetails = Some(testScotLtdPartnership)
        )
        val expectedScottishLimitedPartnershipSummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Company registration number",
            optAnswer = Some(testCrn),
            optUrl = Some("/register-for-vat/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Official business name",
            optAnswer = Some(testCompanyName),
            optUrl = Some("/register-for-vat/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Unique Taxpayer Reference number",
            optAnswer = Some(testSautr),
            optUrl = Some("/register-for-vat/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Partnership’s registered postcode for self assessment",
            optAnswer = Some(testPostcode),
            optUrl = Some("/register-for-vat/start-partnership-id-journey"))
        ).flatten)

        val result = grsCheckYourAnswersBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedScottishLimitedPartnershipSummaryList
      }
    }
    "called with Limited Liability Partnership party type" should {
      "return the summary list for Limited Liability Partnership" in new Setup {
        val testLtdLiabilityPartnership: ApplicantDetails = ApplicantDetails(
          entity = Some(PartnershipIdEntity(
            sautr = Some(testSautr),
            companyNumber = Some(testCrn),
            companyName = Some(testCompanyName),
            postCode = Some(testPostcode),
            registration = testRegistration,
            businessVerification = Some(BvPass),
            bpSafeId = Some(testSafeId),
            identifiersMatch = true
          ))
        )
        val vatScheme: VatScheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = LtdLiabilityPartnership
          )),
          applicantDetails = Some(testLtdLiabilityPartnership)
        )
        val expectedLimitedLiabilityPartnershipSummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Company registration number",
            optAnswer = Some(testCrn),
            optUrl = Some("/register-for-vat/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Official business name",
            optAnswer = Some(testCompanyName),
            optUrl = Some("/register-for-vat/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Unique Taxpayer Reference number",
            optAnswer = Some(testSautr),
            optUrl = Some("/register-for-vat/start-partnership-id-journey")),
          optSummaryListRowString(
            questionId = "Partnership’s registered postcode for self assessment",
            optAnswer = Some(testPostcode),
            optUrl = Some("/register-for-vat/start-partnership-id-journey"))
        ).flatten)

        val result = grsCheckYourAnswersBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedLimitedLiabilityPartnershipSummaryList
      }
    }

    //Minor Entity
    "called with Unincorporated Association party type" should {
      "return the summary list for Unincorporated Association" in new Setup {
        val testUnincorpAssoc: ApplicantDetails = ApplicantDetails(
          entity = Some(MinorEntity(
            sautr = None,
            ctutr = Some(testCtUtr),
            postCode = Some(testPostcode),
            chrn = None,
            casc = None,
            registration = testRegistration,
            businessVerification = Some(BvPass),
            bpSafeId = Some(testSafeId),
            identifiersMatch = true
          ))
        )
        val vatScheme: VatScheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = UnincorpAssoc
          )),
          applicantDetails = Some(testUnincorpAssoc)
        )
        val expectedUnincorpAssocSummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Unique Taxpayer Reference number",
            optAnswer = Some(testCtUtr),
            optUrl = Some("/register-for-vat/start-minor-entity-id-journey")),
          optSummaryListRowString(
            questionId = "Unincorporated Association’s postcode",
            optAnswer = Some(testPostcode),
            optUrl = Some("/register-for-vat/start-minor-entity-id-journey"))
        ).flatten)

        val result = grsCheckYourAnswersBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedUnincorpAssocSummaryList
      }
    }
    "called with Trust party type" should {
      "return the summary list for Trust" in new Setup {
        val testTrust: ApplicantDetails = ApplicantDetails(
          entity = Some(MinorEntity(
            sautr = Some(testSautr),
            ctutr = None,
            postCode = Some(testPostcode),
            chrn = None,
            casc = None,
            registration = testRegistration,
            businessVerification = Some(BvPass),
            bpSafeId = Some(testSafeId),
            identifiersMatch = true
          ))
        )
        val vatScheme: VatScheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = Trust
          )),
          applicantDetails = Some(testTrust)
        )
        val expectedTrustSummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Unique Taxpayer Reference number",
            optAnswer = Some(testSautr),
            optUrl = Some("/register-for-vat/start-minor-entity-id-journey")),
          optSummaryListRowString(
            questionId = "Trust’s registered postcode for self assessment",
            optAnswer = Some(testPostcode),
            optUrl = Some("/register-for-vat/start-minor-entity-id-journey"))
        ).flatten)

        val result = grsCheckYourAnswersBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedTrustSummaryList
      }
    }
    "called with Non UK Company party type" should {
      "return the summary list for Non UK Company" in new Setup {
        when(mockConfigConnector.countries).thenReturn(Seq(Country(Some(testOverseasIdentifierCountry), Some(testOverseasCountryName))))

        val testNonUkNonEstablished: ApplicantDetails = ApplicantDetails(
          entity = Some(MinorEntity(
            sautr = None,
            ctutr = Some(testCtUtr),
            overseas = Some(testOverseasIdentifierDetails),
            postCode = None,
            chrn = None,
            casc = None,
            registration = testRegistration,
            businessVerification = Some(BvPass),
            bpSafeId = Some(testSafeId),
            identifiersMatch = true
          ))
        )
        val vatScheme: VatScheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NonUkNonEstablished
          )),
          applicantDetails = Some(testNonUkNonEstablished)
        )
        val expectedNonUkNonEstablishedSummaryList = SummaryList(Seq(
          optSummaryListRowString(
            questionId = "Unique Taxpayer Reference number",
            optAnswer = Some(testCtUtr),
            optUrl = Some("/register-for-vat/start-minor-entity-id-journey")),
          optSummaryListRowString(
            questionId = "Overseas tax identifier",
            optAnswer = Some(testOverseasIdentifier),
            optUrl = Some("/register-for-vat/start-minor-entity-id-journey")),
          optSummaryListRowString(
            questionId = "Overseas country",
            optAnswer = Some(testOverseasCountryName),
            optUrl = Some("/register-for-vat/start-minor-entity-id-journey"))
        ).flatten)

        val result = grsCheckYourAnswersBuilder.build(vatScheme = vatScheme)(messages = messages)
        result mustBe expectedNonUkNonEstablishedSummaryList
      }
    }
  }
}