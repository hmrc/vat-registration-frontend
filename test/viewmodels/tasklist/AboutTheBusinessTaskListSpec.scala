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

import controllers.partners.PartnerIndexValidation
import featureswitch.core.config.{DigitalPartnerFlow, FeatureSwitching}
import fixtures.VatRegistrationFixture
import models.api.{Individual, Partnership, ScotPartnership, SicCode}
import models.{Business, Entity, LabourCompliance}
import testHelpers.VatRegSpec

class AboutTheBusinessTaskListSpec extends VatRegSpec with VatRegistrationFixture with FeatureSwitching {

  val section: AboutTheBusinessTaskList = app.injector.instanceOf[AboutTheBusinessTaskList]

  "Additional partners row when DigitalPartnerFlow FS enabled" must {
    val expectedRowUrl = controllers.partners.routes.PartnerEntityTypeController.showPartnerType(PartnerIndexValidation.minPartnerIndex).url

    "not be shown for non-partnership party type" in {
      val maybePartnersDetail = section.buildPartnersDetailRow(emptyVatScheme)
      maybePartnersDetail mustBe None
    }

    "be TLCannotStart if the prerequisites are not complete" in {
      enable(DigitalPartnerFlow)
      val scheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(
          validEligibilitySubmissionData.copy(partyType = ScotPartnership)
        )
      )

      val maybePartnersDetail = section.buildPartnersDetailRow(scheme)
      maybePartnersDetail must not be None

      val row = maybePartnersDetail.get.build(scheme)
      row.status mustBe TLCannotStart
      row.url mustBe expectedRowUrl
      disable(DigitalPartnerFlow)
    }

    "be TLNotStarted if the prerequisites are complete but there are no answers" in {
      enable(DigitalPartnerFlow)
      val scheme = validVatScheme.copy(
        eligibilitySubmissionData = Some(
          validEligibilitySubmissionData.copy(partyType = ScotPartnership)
        ),
        entities = Some(List(Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None)))
      )

      val maybePartnersDetail = section.buildPartnersDetailRow(scheme)
      maybePartnersDetail must not be None

      val row = maybePartnersDetail.get.build(scheme)

      row.status mustBe TLNotStarted
      row.url mustBe expectedRowUrl
      disable(DigitalPartnerFlow)
    }

    "be TLCompleted if the prerequisites are complete and there are all answers" in {
      enable(DigitalPartnerFlow)
      val scheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(
          validEligibilitySubmissionData.copy(partyType = ScotPartnership)
        ),
        applicantDetails = Some(completeApplicantDetails),
        business = Some(validBusiness),
        entities = Some(List(
          Entity(None, Individual, Some(true), None, None, None, None),
          Entity(Some(testSoleTrader), Individual, Some(false), None, Some(testAddress), Some("test@test.com"), Some("12334483"))
        ))
      )

      val maybePartnersDetail = section.buildPartnersDetailRow(scheme)
      maybePartnersDetail must not be None

      val row = maybePartnersDetail.get.build(scheme)
      row.status mustBe TLCompleted
      row.url mustBe expectedRowUrl
      disable(DigitalPartnerFlow)
    }
  }

  "The business details row" must {
    "be cannot start if the prerequisites are not complete" in {
      val scheme = emptyVatScheme

      val row = section.businessDetailsRow.build(scheme)

      row.status mustBe TLCannotStart
      row.url mustBe controllers.routes.TradingNameResolverController.resolve.url
    }

    "be not started if the prerequisites are complete but there are no answers" in {
      val scheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData),
        applicantDetails = Some(completeApplicantDetails)
      )

      val row = section.businessDetailsRow.build(scheme)

      row.status mustBe TLNotStarted
      row.url mustBe controllers.routes.TradingNameResolverController.resolve.url
    }

    "be in progress if the prerequisites are complete and there are some answers" in {
      val scheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData),
        applicantDetails = Some(completeApplicantDetails),
        business = Some(Business(ppobAddress = Some(testAddress)))
      )

      val row = section.businessDetailsRow.build(scheme)

      row.status mustBe TLInProgress
      row.url mustBe controllers.routes.TradingNameResolverController.resolve.url
    }

    "be in progress if the prerequisites are complete and there are all answers but companyName not defined for GeneralPartnership" in {
      val scheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = Partnership)),
        applicantDetails = Some(completeApplicantDetails.copy(entity = Some(testGeneralPartnership))),
        entities = Some(List(Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None))),
        business = Some(validBusiness)
      )
      val row = section.businessDetailsRow.build(scheme)

      row.status mustBe TLInProgress
      row.url mustBe controllers.routes.TradingNameResolverController.resolve.url
    }

    "be completed if the prerequisites are complete and there are all answers" in {
      val scheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData),
        applicantDetails = Some(completeApplicantDetails),
        business = Some(validBusiness)
      )

      val row = section.businessDetailsRow.build(scheme)

      row.status mustBe TLCompleted
      row.url mustBe controllers.routes.TradingNameResolverController.resolve.url
    }
  }

  "checks for business activities row" when {
    val complianceSicCode = SicCode("42110", "code with compliance", "")
    val nonComplianceSicCode = SicCode("88888", "code with no compliance", "")

    "business details not available" must {
      "return TLCannotStart" in {
        val sectionRow = section.businessActivitiesRow.build(emptyVatScheme)
        sectionRow.status mustBe TLCannotStart
      }
    }

    "business details available but no activity details captured" must {
      "return TLNotStarted" in {
        val schema = validVatScheme.copy(business = Some(validBusiness.copy(
          mainBusinessActivity = None, businessDescription = None
        )))
        val sectionRow = section.businessActivitiesRow.build(schema)
        sectionRow.status mustBe TLNotStarted
        sectionRow.url mustBe controllers.business.routes.LandAndPropertyController.show.url
      }
    }

    "business details available but activity details partially captured" must {
      "return TLInProgress" in {
        val schema = validVatScheme.copy(
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(true), mainBusinessActivity = None, businessDescription = None
          ))
        )
        val sectionRow = section.businessActivitiesRow.build(schema)
        sectionRow.status mustBe TLInProgress
        sectionRow.url mustBe controllers.business.routes.LandAndPropertyController.show.url
      }

      "return TLInProgress if compliance siccode chosen but compliance details not complete" in {
        val schema = validVatScheme.copy(
          business = Some(validBusiness.copy(
            mainBusinessActivity = Some(complianceSicCode),
            labourCompliance = Some(LabourCompliance(None, Some(false), Some(true))),
            businessActivities = Some(List(complianceSicCode))
          ))
        )
        val sectionRow = section.businessActivitiesRow.build(schema)
        sectionRow.status mustBe TLInProgress
        sectionRow.url mustBe controllers.business.routes.LandAndPropertyController.show.url
      }
    }

    "business details available and activity details captured" must {
      "return TLCompleted for siccode with no compliance details needed" in {
        val schema = validVatScheme.copy(
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            mainBusinessActivity = Some(nonComplianceSicCode),
            businessActivities = Some(List(nonComplianceSicCode))
          ))
        )
        val sectionRow = section.businessActivitiesRow.build(schema)
        sectionRow.status mustBe TLCompleted
        sectionRow.url mustBe controllers.business.routes.LandAndPropertyController.show.url
      }

      "return TLCompleted for compliance siccode with required compliance details" in {
        val schema = validVatScheme.copy(
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            mainBusinessActivity = Some(complianceSicCode),
            businessActivities = Some(List(complianceSicCode)),
            labourCompliance = Some(complianceWithLabour)
          ))
        )
        val sectionRow = section.businessActivitiesRow.build(schema)
        sectionRow.status mustBe TLCompleted
        sectionRow.url mustBe controllers.business.routes.LandAndPropertyController.show.url
      }
    }
  }

  "The other business involvements row" must {
    "be cannot start if the prerequisites are not complete" in {
      val row = section.otherBusinessInvolvementsRow.build(emptyVatScheme)

      row.status mustBe TLCannotStart
      row.url mustBe controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url
    }

    "be not started if the prerequisites are complete but 'Is involved in other business' question is not answered" in {
      val scheme = validVatScheme.copy(
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          businessActivities = Some(List(sicCode)),
          otherBusinessInvolvement = None
        ))
      )

      val row = section.otherBusinessInvolvementsRow.build(scheme)

      row.status mustBe TLNotStarted
      row.url mustBe controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url
    }

    "be completed if the prerequisites are complete and 'Is involved in other business' question is answered as 'No'" in {
      val scheme = validVatScheme.copy(
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          businessActivities = Some(List(sicCode)),
          otherBusinessInvolvement = Some(false)
        ))
      )

      val row = section.otherBusinessInvolvementsRow.build(scheme)

      row.status mustBe TLCompleted
      row.url mustBe controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url
    }

    "be in progress if the prerequisites are complete and the other business involvements list is empty" in {
      val scheme = validVatScheme.copy(
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          businessActivities = Some(List(sicCode)),
          otherBusinessInvolvement = Some(true)
        )),
        otherBusinessInvolvements = Some(List.empty)
      )

      val row = section.otherBusinessInvolvementsRow.build(scheme)

      row.status mustBe TLInProgress
      row.url mustBe controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url
    }

    "be in progress if the prerequisites are complete and the other business involvements list is None" in {
      val scheme = validVatScheme.copy(
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          businessActivities = Some(List(sicCode)),
          otherBusinessInvolvement = Some(true)
        )),
        otherBusinessInvolvements = None
      )

      val row = section.otherBusinessInvolvementsRow.build(scheme)

      row.status mustBe TLInProgress
      row.url mustBe controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url
    }

    "be in progress and redirect to other business involvements summary page " +
      "if the prerequisites are complete and the other business involvements list has items with partial details" in {
      val scheme = validVatScheme.copy(
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          businessActivities = Some(List(sicCode)),
          otherBusinessInvolvement = Some(true)
        )),
        otherBusinessInvolvements = Some(List(
          otherBusinessInvolvementWithPartialData,
          otherBusinessInvolvementWithVrn
        ))
      )

      val row = section.otherBusinessInvolvementsRow.build(scheme)

      row.status mustBe TLInProgress
      row.url mustBe controllers.otherbusinessinvolvements.routes.ObiSummaryController.show.url
    }

    "be completed and redirect to other business involvements summary page " +
      "if the prerequisites are complete and the other business involvements list has items with required data" in {
      val scheme = validVatScheme.copy(
        business = Some(validBusiness.copy(
          hasLandAndProperty = Some(false),
          businessActivities = Some(List(sicCode)),
          otherBusinessInvolvement = Some(true)
        )),
        otherBusinessInvolvements = Some(List(
          otherBusinessInvolvementWithVrn,
          otherBusinessInvolvementWithUtr,
          otherBusinessInvolvementWithoutVrnUtr
        ))
      )

      val row = section.otherBusinessInvolvementsRow.build(scheme)

      row.status mustBe TLCompleted
      row.url mustBe controllers.otherbusinessinvolvements.routes.ObiSummaryController.show.url
    }
  }
}
