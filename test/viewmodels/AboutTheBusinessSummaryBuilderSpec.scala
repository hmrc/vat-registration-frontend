/*
 * Copyright 2023 HM Revenue & Customs
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
import models.api.{Address, Partnership, ScotPartnership}
import models.external.{BvPass, IncorporatedEntity, MinorEntity, PartnershipIdEntity}
import play.api.i18n.{Lang, MessagesApi}
import play.twirl.api.HtmlFormat
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class AboutTheBusinessSummaryBuilderSpec extends VatRegSpec {

  val govukSummaryList = app.injector.instanceOf[GovukSummaryList]
  val builder = app.injector.instanceOf[AboutTheBusinessSummaryBuilder]

  val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages = messagesApi.preferred(Seq(Lang("en")))
  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val sectionId = "cya.aboutTheBusiness"
  val testEmail = "test@foo.com"
  val testPhoneNumber = "123"
  val testMobileNumber = "987654"
  val testWebsite = "/test/url"
  val testNumWorkers = "12"
  val testLanguage = "English"

  import models.view.SummaryListRowUtils._

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

  "the About The Business Check Your Answers builder" when {
    "the user is not overseas" must {
      "show the non-overseas answers with a UK address" in {
        val scheme = emptyVatScheme.copy(
          applicantDetails = Some(testLtdCompany),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(true),
            otherBusinessInvolvement = Some(false),
            labourCompliance = Some(complianceWithLabour),
            businessActivities = Some(List(sicCode))
          )),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          vatApplication = Some(validVatApplication.copy(northernIrelandProtocol = Some(validNipCompliance), appliedForExemption = Some(false)))
        )

        val res = builder.build(scheme)

        res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
          rows = List(
            optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.business.routes.ConfirmTradingNameController.show.url)),
            optSummaryListRowSeq(s"$sectionId.homeAddress", Some(Address.normalisedSeq(testAddress)), Some(controllers.business.routes.PpobAddressController.startJourney.url)),
            optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(controllers.business.routes.BusinessEmailController.show.url)),
            optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(controllers.business.routes.BusinessTelephoneNumberController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.hasWebsite", Some(true), Some(controllers.business.routes.HasWebsiteController.show.url)),
            optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(controllers.business.routes.BusinessWebsiteAddressController.show.url)),
            optSummaryListRowString(s"$sectionId.correspondenceLanguage", Some(testLanguage), Some(controllers.business.routes.VatCorrespondenceController.show.url)),
            optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)),
            optSummaryListRowBoolean(s"$sectionId.buySellLandAndProperty", Some(true), Some(controllers.business.routes.LandAndPropertyController.show.url)),
            optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(controllers.business.routes.BusinessActivityDescriptionController.show.url)),
            optSummaryListRowSeq(s"$sectionId.sicCodes", Some(Seq(s"${sicCode.code} - ${sicCode.description}")), Some(controllers.sicandcompliance.routes.SicController.startICLJourney.url)),
            optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(controllers.sicandcompliance.routes.MainBusinessActivityController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(true), Some(controllers.business.routes.SupplyWorkersController.show.url)),
            optSummaryListRowString(s"$sectionId.numberOfWorkers", Some(testNumWorkers), Some(controllers.business.routes.WorkersController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.intermediarySupply", Some(true), Some(controllers.business.routes.SupplyWorkersIntermediaryController.show.url))
          ).flatten
        ))))
      }

      "hide the compliance section if the user is supplying workers" in {
        val scheme = emptyVatScheme.copy(
          applicantDetails = Some(testLtdCompany),
          business = Some(validBusiness.copy(
            otherBusinessInvolvement = Some(false),
            labourCompliance = Some(complianceWithLabour.copy(supplyWorkers = Some(false)))
          )),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          vatApplication = Some(validVatApplication.copy(northernIrelandProtocol = Some(validNipCompliance)))
        )

        val res = builder.build(scheme)

        res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
          rows = List(
            optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.business.routes.ConfirmTradingNameController.show.url)),
            optSummaryListRowSeq(s"$sectionId.homeAddress", Some(Address.normalisedSeq(testAddress)), Some(controllers.business.routes.PpobAddressController.startJourney.url)),
            optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(controllers.business.routes.BusinessEmailController.show.url)),
            optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(controllers.business.routes.BusinessTelephoneNumberController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.hasWebsite", Some(true), Some(controllers.business.routes.HasWebsiteController.show.url)),
            optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(controllers.business.routes.BusinessWebsiteAddressController.show.url)),
            optSummaryListRowString(s"$sectionId.correspondenceLanguage", Some(testLanguage), Some(controllers.business.routes.VatCorrespondenceController.show.url)),
            optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)),
            optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(controllers.business.routes.BusinessActivityDescriptionController.show.url)),
            optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(controllers.sicandcompliance.routes.MainBusinessActivityController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(false), Some(controllers.business.routes.SupplyWorkersController.show.url))
          ).flatten
        ))))
      }

      "show completed and non-lead-partner members of the partnership" when {
        "the user has added partners" in {
          val scheme = emptyVatScheme.copy(
            applicantDetails = Some(testGenPartnership),
            business = Some(validBusiness.copy(
              hasLandAndProperty = Some(true),
              otherBusinessInvolvement = Some(false),
              labourCompliance = Some(complianceWithLabour),
              businessActivities = Some(List(sicCode))
            )),
            entities = Some(validEntities),
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership)),
            vatApplication = Some(validVatApplication.copy(northernIrelandProtocol = Some(validNipCompliance), appliedForExemption = Some(false)))
          )

          val res = builder.build(scheme)

          res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
            rows = List(
              optSummaryListRowSeq(s"$sectionId.partnershipMembers", Some(List[String](testCompanyName)), Some(controllers.partners.routes.PartnerSummaryController.show.url)),
              optSummaryListRowString(s"$sectionId.partnershipName", Some(testCompanyName), Some(controllers.business.routes.PartnershipNameController.show.url)),
              optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.business.routes.ConfirmTradingNameController.show.url)),
              optSummaryListRowSeq(s"$sectionId.homeAddress", Some(Address.normalisedSeq(testAddress)), Some(controllers.business.routes.PpobAddressController.startJourney.url)),
              optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(controllers.business.routes.BusinessEmailController.show.url)),
              optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(controllers.business.routes.BusinessTelephoneNumberController.show.url)),
              optSummaryListRowBoolean(s"$sectionId.hasWebsite", Some(true), Some(controllers.business.routes.HasWebsiteController.show.url)),
              optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(controllers.business.routes.BusinessWebsiteAddressController.show.url)),
              optSummaryListRowString(s"$sectionId.correspondenceLanguage", Some(testLanguage), Some(controllers.business.routes.VatCorrespondenceController.show.url)),
              optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)),
              optSummaryListRowBoolean(s"$sectionId.buySellLandAndProperty", Some(true), Some(controllers.business.routes.LandAndPropertyController.show.url)),
              optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(controllers.business.routes.BusinessActivityDescriptionController.show.url)),
              optSummaryListRowSeq(s"$sectionId.sicCodes", Some(Seq(s"${sicCode.code} - ${sicCode.description}")), Some(controllers.sicandcompliance.routes.SicController.startICLJourney.url)),
              optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(controllers.sicandcompliance.routes.MainBusinessActivityController.show.url)),
              optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(true), Some(controllers.business.routes.SupplyWorkersController.show.url)),
              optSummaryListRowString(s"$sectionId.numberOfWorkers", Some(testNumWorkers), Some(controllers.business.routes.WorkersController.show.url)),
              optSummaryListRowBoolean(s"$sectionId.intermediarySupply", Some(true), Some(controllers.business.routes.SupplyWorkersIntermediaryController.show.url))
            ).flatten
          ))))
        }
      }

      "hide the members of the partnership row" when {
        "the user has not added partners" in {
          val scheme = emptyVatScheme.copy(
            applicantDetails = Some(testGenPartnership),
            business = Some(validBusiness.copy(
              hasLandAndProperty = Some(true),
              otherBusinessInvolvement = Some(false),
              labourCompliance = Some(complianceWithLabour),
              businessActivities = Some(List(sicCode))
            )),
            entities = Some(validEntities.slice(1,2)),
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = ScotPartnership)),
            vatApplication = Some(validVatApplication.copy(northernIrelandProtocol = Some(validNipCompliance), appliedForExemption = Some(false)))
          )

          val res = builder.build(scheme)

          res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
            rows = List(
              optSummaryListRowString(s"$sectionId.partnershipName", Some(testCompanyName), Some(controllers.business.routes.PartnershipNameController.show.url)),
              optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.business.routes.ConfirmTradingNameController.show.url)),
              optSummaryListRowSeq(s"$sectionId.homeAddress", Some(Address.normalisedSeq(testAddress)), Some(controllers.business.routes.PpobAddressController.startJourney.url)),
              optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(controllers.business.routes.BusinessEmailController.show.url)),
              optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(controllers.business.routes.BusinessTelephoneNumberController.show.url)),
              optSummaryListRowBoolean(s"$sectionId.hasWebsite", Some(true), Some(controllers.business.routes.HasWebsiteController.show.url)),
              optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(controllers.business.routes.BusinessWebsiteAddressController.show.url)),
              optSummaryListRowString(s"$sectionId.correspondenceLanguage", Some(testLanguage), Some(controllers.business.routes.VatCorrespondenceController.show.url)),
              optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)),
              optSummaryListRowBoolean(s"$sectionId.buySellLandAndProperty", Some(true), Some(controllers.business.routes.LandAndPropertyController.show.url)),
              optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(controllers.business.routes.BusinessActivityDescriptionController.show.url)),
              optSummaryListRowSeq(s"$sectionId.sicCodes", Some(Seq(s"${sicCode.code} - ${sicCode.description}")), Some(controllers.sicandcompliance.routes.SicController.startICLJourney.url)),
              optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(controllers.sicandcompliance.routes.MainBusinessActivityController.show.url)),
              optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(true), Some(controllers.business.routes.SupplyWorkersController.show.url)),
              optSummaryListRowString(s"$sectionId.numberOfWorkers", Some(testNumWorkers), Some(controllers.business.routes.WorkersController.show.url)),
              optSummaryListRowBoolean(s"$sectionId.intermediarySupply", Some(true), Some(controllers.business.routes.SupplyWorkersIntermediaryController.show.url))
            ).flatten
          ))))
        }
      }
    }

    "called with Unincorporated Association party type" should {
      "return the summary list with business name included" in {
        val companyName = "minor entity company"

        val testUnincorpAssoc: ApplicantDetails = ApplicantDetails(
          entity = Some(MinorEntity(
            companyName = Some(companyName),
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
        val scheme = emptyVatScheme.copy(
          applicantDetails = Some(testUnincorpAssoc),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(true),
            otherBusinessInvolvement = Some(false),
            labourCompliance = Some(complianceWithLabour),
            businessActivities = Some(List(sicCode))
          )),
          entities = Some(validEntities.slice(1, 2)),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = ScotPartnership)),
          vatApplication = Some(validVatApplication.copy(northernIrelandProtocol = Some(validNipCompliance), appliedForExemption = Some(false)))
        )

        val res = builder.build(scheme)

        res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
          rows = List(
            optSummaryListRowString(s"$sectionId.businessName", Some(companyName), Some(controllers.grs.routes.MinorEntityIdController.startJourney.url)),
            optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.business.routes.ConfirmTradingNameController.show.url)),
            optSummaryListRowSeq(s"$sectionId.homeAddress", Some(Address.normalisedSeq(testAddress)), Some(controllers.business.routes.PpobAddressController.startJourney.url)),
            optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(controllers.business.routes.BusinessEmailController.show.url)),
            optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(controllers.business.routes.BusinessTelephoneNumberController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.hasWebsite", Some(true), Some(controllers.business.routes.HasWebsiteController.show.url)),
            optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(controllers.business.routes.BusinessWebsiteAddressController.show.url)),
            optSummaryListRowString(s"$sectionId.correspondenceLanguage", Some(testLanguage), Some(controllers.business.routes.VatCorrespondenceController.show.url)),
            optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)),
            optSummaryListRowBoolean(s"$sectionId.buySellLandAndProperty", Some(true), Some(controllers.business.routes.LandAndPropertyController.show.url)),
            optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(controllers.business.routes.BusinessActivityDescriptionController.show.url)),
            optSummaryListRowSeq(s"$sectionId.sicCodes", Some(Seq(s"${sicCode.code} - ${sicCode.description}")), Some(controllers.sicandcompliance.routes.SicController.startICLJourney.url)),
            optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(controllers.sicandcompliance.routes.MainBusinessActivityController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(true), Some(controllers.business.routes.SupplyWorkersController.show.url)),
            optSummaryListRowString(s"$sectionId.numberOfWorkers", Some(testNumWorkers), Some(controllers.business.routes.WorkersController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.intermediarySupply", Some(true), Some(controllers.business.routes.SupplyWorkersIntermediaryController.show.url))
          ).flatten
        ))))
      }
    }
  }
}


