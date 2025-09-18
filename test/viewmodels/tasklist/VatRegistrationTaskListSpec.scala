/*
 * Copyright 2024 HM Revenue & Customs
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
import fixtures.VatRegistrationFixture
import models._
import models.api._
import models.api.vatapplication.{OverseasCompliance, StoringOverseas, StoringWithinUk, VatApplication}
import services.BusinessService
import testHelpers.VatRegSpec

import java.time.LocalDate

class VatRegistrationTaskListSpec(implicit appConfig: FrontendAppConfig) extends VatRegSpec with VatRegistrationFixture {
  val section: VatRegistrationTaskList.type = VatRegistrationTaskList

  val businessService: BusinessService = mockBusinessService

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

  "checks for goods and services row" when {
    "prerequisite not complete" must {
      "return TLCannotStart" in {
        val sectionRow = section.goodsAndServicesRow(businessService).build(emptyVatScheme)
        sectionRow.status mustBe TLCannotStart
      }
    }

    "prerequisite is complete but goods and services flow hasn't started" must {
      "return TLNotStarted with correct imports exports for both UK and non-UK party types" in {
        def verifyNotStartedFlow(partyType: PartyType, expectedUrl: String) = {
          val schema = validVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = partyType)),
            business = Some(validBusiness.copy(
              hasLandAndProperty = Some(false),
              otherBusinessInvolvement = Some(true),
              businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
            )),
            otherBusinessInvolvements = Some(List(
              otherBusinessInvolvementWithVrn,
              otherBusinessInvolvementWithUtr,
              otherBusinessInvolvementWithoutVrnUtr
            )),
            vatApplication = None
          )

          val sectionRow = section.goodsAndServicesRow(businessService).build(schema)
          sectionRow.status mustBe TLNotStarted
          sectionRow.url mustBe expectedUrl
        }

        verifyNotStartedFlow(NETP, controllers.vatapplication.routes.ImportsOrExportsController.show.url)
        verifyNotStartedFlow(UkCompany, controllers.vatapplication.routes.ImportsOrExportsController.show.url)
      }
    }

    "prerequisite is complete and goods and services captured with core details" must {
      "return TLCompleted" in {
        val scheme = validVatScheme.copy(
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection)
        )

        val sectionRow = section.goodsAndServicesRow(businessService).build(scheme)
        sectionRow.status mustBe TLCompleted
        sectionRow.url mustBe controllers.vatapplication.routes.ImportsOrExportsController.show.url
      }
    }

    "prerequisite is complete but goods and services details partially submitted" must {
      def verifyInProgressSectionRow(scheme: VatScheme) = {
        val sectionRow = section.goodsAndServicesRow(businessService).build(scheme)
        sectionRow.status mustBe TLInProgress
        sectionRow.url mustBe controllers.vatapplication.routes.ImportsOrExportsController.show.url
      }

      "return TLInProgress" in {
        val scheme = validVatScheme.copy(
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          ))
        )

        verifyInProgressSectionRow(scheme)
      }

      "return TLInProgress for compliance with goods to overseas set, but no EU option" in {
        val scheme = validVatScheme.copy(
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(validVatApplication.copy(
            overseasCompliance = Some(OverseasCompliance(
              goodsToOverseas = Some(true),
              storingGoodsForDispatch = Some(StoringOverseas)
            ))
          ))
        )

        verifyInProgressSectionRow(scheme)
      }

      "return TLInProgress when all core and compliance details available, but warehouse details missing when StoringWithinUk option is selected" in {
        val scheme = validVatScheme.copy(
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(validVatApplication.copy(
            overseasCompliance = Some(OverseasCompliance(
              goodsToOverseas = Some(true),
              goodsToEu = Some(true),
              storingGoodsForDispatch = Some(StoringWithinUk),
              usingWarehouse = Some(true)
            ))
          ))
        )

        verifyInProgressSectionRow(scheme)
      }
    }
  }

  "checks for vat registration date tasklist row" when {

    "rendering registration date row" must {

      "resolve to None if registration reason available but not eligible for registration date flow" in {
        section.resolveVATRegistrationDateRow(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            registrationReason = NonUk,
            fixedEstablishmentInManOrUk = false
          ))
        ),
          businessService) mustBe None
      }

      "resolve to registration date row if registration reason available and eligible for registration date flow" in {
        Seq(ForwardLook, BackwardLook, GroupRegistration, Voluntary, IntendingTrader, SuppliesOutsideUk).map { reason =>
          val scheme = emptyVatScheme.copy(eligibilitySubmissionData =
            Some(validEligibilitySubmissionData.copy(registrationReason = reason))
          )

          section.resolveVATRegistrationDateRow(scheme, businessService).map(_.build(scheme).url) mustBe
            Some(controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url)
        }
      }

      "return None if no registration reason available" in {
        section.resolveVATRegistrationDateRow(emptyVatScheme, businessService) mustBe None
      }
    }

    "prerequisite not complete" must {
      "return TLCannotStart" in {
        val sectionRow = section.registrationDateRow(businessService).build(emptyVatScheme)
        sectionRow.status mustBe TLCannotStart
      }
    }

    "prerequisite is complete but registration date flow hasn't started" must {
      "return TLNotStarted with correct url" in {
        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(true),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          otherBusinessInvolvements = Some(List(
            otherBusinessInvolvementWithVrn,
            otherBusinessInvolvementWithUtr,
            otherBusinessInvolvementWithoutVrnUtr
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection)
        )

        val sectionRow = section.registrationDateRow(businessService).build(scheme)
        sectionRow.status mustBe TLNotStarted
        sectionRow.url mustBe controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url
      }
    }

    "prerequisite is complete and registration date available but currently trading check not complete for voluntary" must {
      "return TLInProgress with correct url" in {
        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(registrationReason = Voluntary)),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
            startDate = Some(LocalDate.of(2017, 10, 10))
          ))
        )

        val sectionRow = section.registrationDateRow(businessService).build(scheme)
        sectionRow.status mustBe TLInProgress
        sectionRow.url mustBe controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url
      }
    }

    "prerequisite is complete and vat registration date captured" must {
      "return TLCompleted" in {
        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            fixedEstablishmentInManOrUk = true
          )),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
            startDate = Some(LocalDate.of(2017, 10, 10))
          ))
        )

        val sectionRow = section.registrationDateRow(businessService).build(scheme)
        sectionRow.status mustBe TLCompleted
        sectionRow.url mustBe controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url
      }
    }
  }

  "checks for bank account details row" when {
    "prerequisite not complete" must {
      "return TLCannotStart" in {
        val sectionRow = section.bankAccountDetailsRow(businessService).build(emptyVatScheme)
        sectionRow.status mustBe TLCannotStart
      }
    }

    "prerequisite is complete but bank account details hasn't started" must {
      "return TLNotStarted with correct url" in {

        val scheme = validVatScheme.copy(
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(validVatApplication.copy(
            overseasCompliance = Some(OverseasCompliance(
              goodsToOverseas = Some(false),
              storingGoodsForDispatch = Some(StoringOverseas)
            )),
            northernIrelandProtocol = Some(NIPTurnover(
              goodsToEU = Some(ConditionalValue(answer = false, None)),
              goodsFromEU = Some(ConditionalValue(answer = false, None)),
            ))
          )),
          bankAccount = None
        )


        val sectionRow = section.bankAccountDetailsRow(businessService).build(scheme)
        sectionRow.status mustBe TLNotStarted
        sectionRow.url mustBe controllers.bankdetails.routes.HasBankAccountController.show.url
      }
    }

    "prerequisite is complete but bank account details flow is still in progress" must {
      "return TLInProgress if user has bank account but no account details is provided" in {
        val scheme = validVatScheme.copy(
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(validVatApplication.copy(
            overseasCompliance = Some(OverseasCompliance(
              goodsToOverseas = Some(false),
              storingGoodsForDispatch = Some(StoringOverseas)
            )),
            northernIrelandProtocol = Some(NIPTurnover(
              goodsToEU = Some(ConditionalValue(answer = false, None)),
              goodsFromEU = Some(ConditionalValue(answer = false, None)),
            ))
          )),
          bankAccount = Some(validUkBankAccount.copy(
            isProvided = true,
            details = None
          ))
        )

        val sectionRow = section.bankAccountDetailsRow(businessService).build(scheme)
        sectionRow.status mustBe TLInProgress
        sectionRow.url mustBe controllers.bankdetails.routes.HasBankAccountController.show.url
      }

      "return TLInProgress if user doesn't have bank account and no reason is provided" in {

        val scheme = validVatScheme.copy(
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(validVatApplication.copy(
            overseasCompliance = Some(OverseasCompliance(
              goodsToOverseas = Some(false),
              storingGoodsForDispatch = Some(StoringOverseas)
            )),
            northernIrelandProtocol = Some(NIPTurnover(
              goodsToEU = Some(ConditionalValue(answer = false, None)),
              goodsFromEU = Some(ConditionalValue(answer = false, None)),
            ))
          )),
          bankAccount = Some(validUkBankAccount.copy(
            isProvided = false,
            reason = None
          ))
        )

        val sectionRow = section.bankAccountDetailsRow(businessService).build(scheme)
        sectionRow.status mustBe TLInProgress
        sectionRow.url mustBe controllers.bankdetails.routes.HasBankAccountController.show.url
      }
    }

    "prerequisite is complete and all bank account details is captured" must {
      "return TLCompleted if user has bank account and provides account details" in {
        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(validVatApplication.copy(
            turnoverEstimate = Some(testTurnover),
            zeroRatedSupplies = Some(testZeroRatedSupplies),
            northernIrelandProtocol = Some(NIPTurnover(
              goodsToEU = Some(ConditionalValue(answer = false, None)),
              goodsFromEU = Some(ConditionalValue(answer = false, None)),
            )),
            claimVatRefunds = Some(false)
          )),
          bankAccount = Some(validUkBankAccount)
        )

        val sectionRow = section.bankAccountDetailsRow(businessService).build(scheme)
        sectionRow.status mustBe TLCompleted
        sectionRow.url mustBe controllers.bankdetails.routes.HasBankAccountController.show.url
      }

      "return TLCompleted if user doesn't have a bank account and provides reason for no bank account" in {
        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(validVatApplication.copy(
            turnoverEstimate = Some(testTurnover),
            zeroRatedSupplies = Some(testZeroRatedSupplies),
            northernIrelandProtocol = Some(NIPTurnover(
              goodsToEU = Some(ConditionalValue(answer = false, None)),
              goodsFromEU = Some(ConditionalValue(answer = false, None)),
            )),
            claimVatRefunds = Some(false)
          )),
          bankAccount = Some(noUkBankAccount)
        )

        val sectionRow = section.bankAccountDetailsRow(businessService).build(scheme)
        sectionRow.status mustBe TLCompleted
        sectionRow.url mustBe controllers.bankdetails.routes.HasBankAccountController.show.url
      }
    }
  }

  "checks for vat returns tasklist row" when {
    val vatApplicationWithNoReturns = completedVatApplicationWithGoodsAndServicesSection.copy(
      returnsFrequency = None, staggerStart = None
    )

    "prerequisite not complete" must {
      "return TLCannotStart" in {
        val sectionRow = section.vatReturnsRow(businessService).build(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData)
        ))
        sectionRow.status mustBe TLCannotStart
      }
    }

    "prerequisite is complete but vat returns flow hasn't started" must {

      "return TLNotStarted with correct url" in {
        val schema = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(true),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          otherBusinessInvolvements = Some(List(
            otherBusinessInvolvementWithVrn,
            otherBusinessInvolvementWithUtr,
            otherBusinessInvolvementWithoutVrnUtr
          )),
          vatApplication = Some(vatApplicationWithNoReturns.copy(
            startDate = Some(LocalDate.of(2017, 10, 10))
          ))
        )

        val sectionRow = section.vatReturnsRow(businessService).build(schema)
        sectionRow.status mustBe TLNotStarted
        sectionRow.url mustBe controllers.vatapplication.routes.ReturnsFrequencyController.show.url
      }
    }

    "prerequisite is complete, for TOGC with no requirement for start date, but vat returns flow hasn't started" must {
      "return TLNotStarted with correct url" in {
        val schema = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(true),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          otherBusinessInvolvements = Some(List(
            otherBusinessInvolvementWithVrn,
            otherBusinessInvolvementWithUtr,
            otherBusinessInvolvementWithoutVrnUtr
          )),
          vatApplication = Some(vatApplicationWithNoReturns)
        )

        val sectionRow = section.vatReturnsRow(businessService).build(schema)
        sectionRow.status mustBe TLNotStarted
        sectionRow.url mustBe controllers.vatapplication.routes.ReturnsFrequencyController.show.url
      }
    }

    "goods and service prerequisite is complete, for NonUK with no bank details or start date, but vat returns flow hasn't started" must {
      "return TLNotStarted with correct url" in {
        val schema = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            registrationReason = NonUk,
            partyType = NETP,
            fixedEstablishmentInManOrUk = false
          )),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(true),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          otherBusinessInvolvements = Some(List(
            otherBusinessInvolvementWithVrn,
            otherBusinessInvolvementWithUtr,
            otherBusinessInvolvementWithoutVrnUtr
          )),
          vatApplication = Some(vatApplicationWithNoReturns),
          bankAccount = None
        )

        val sectionRow = section.vatReturnsRow(businessService).build(schema)
        sectionRow.status mustBe TLNotStarted
        sectionRow.url mustBe controllers.vatapplication.routes.ReturnsFrequencyController.show.url
      }
    }

    "prerequisite is complete and all vat returns data captured except tax rep for non-uk journey" must {
      "return TLInProgress" in {
        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            fixedEstablishmentInManOrUk = false
          )),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
            startDate = Some(LocalDate.of(2017, 10, 10))
          ))
        )

        val sectionRow = section.vatReturnsRow(businessService).build(scheme)
        sectionRow.status mustBe TLInProgress
        sectionRow.url mustBe controllers.vatapplication.routes.ReturnsFrequencyController.show.url
      }
    }

    "prerequisite is complete and all vat returns data captured" must {
      "return TLCompleted" in {
        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            fixedEstablishmentInManOrUk = false
          )),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
            hasTaxRepresentative = Some(false),
            startDate = Some(LocalDate.of(2017, 10, 10))
          ))
        )

        val sectionRow = section.vatReturnsRow(businessService).build(scheme)
        sectionRow.status mustBe TLCompleted
        sectionRow.url mustBe controllers.vatapplication.routes.ReturnsFrequencyController.show.url
      }
    }
  }

  "checks for flat rate scheme row" when {
    "rendering the row " must {
      "resolve to None if turnover estimate exceeds the limit" in {
        section.resolveFlatRateSchemeRow(
          validVatScheme.copy(
            vatApplication = Some(validVatApplication.copy(turnoverEstimate = Some(BigDecimal(150001))))
          ),
          businessService
        ) mustBe None
      }
      "resolve to None if registration reason is group registration" in {
        section.resolveFlatRateSchemeRow(
          validVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(registrationReason = GroupRegistration))
          ),
          businessService
        ) mustBe None
      }
      "resolve to flat rate scheme row if turnover estimate is within the limit and registration reason is other than group registration" in {
        val scheme = validVatScheme.copy(
          vatApplication = Some(validVatApplication.copy(turnoverEstimate = Some(BigDecimal(149999)))),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(registrationReason = ForwardLook))
        )
        section.resolveFlatRateSchemeRow(scheme, businessService).map(_.build(scheme).url) mustBe
          Some(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url)
      }
    }
    "prerequisite not complete" must {
      "return task list cannot start" in {
        val sectionRow = section.flatRateSchemeRow(businessService).build(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData)
        ))
        sectionRow.status mustBe TLCannotStart
      }
    }

    "prerequisite is complete but flat rate scheme flow hasn't started with its value None" must {
      "return task list not started with correct url" in {
        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            fixedEstablishmentInManOrUk = false
          )),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
            hasTaxRepresentative = Some(false),
            startDate = Some(LocalDate.of(2017, 10, 10))
          )),
          flatRateScheme = None
        )

        val sectionRow = section.flatRateSchemeRow(businessService).build(scheme)
        sectionRow.status mustBe TLNotStarted
        sectionRow.url mustBe controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url
      }
    }

    "prerequisite is complete but flat rate scheme flow hasn't started having an empty collection of data" must {
      "return list not started with correct url" in {
        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            fixedEstablishmentInManOrUk = false
          )),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
            hasTaxRepresentative = Some(false),
            startDate = Some(LocalDate.of(2017, 10, 10))
          )),
          flatRateScheme = Some(FlatRateScheme(None, None, None, None, None, None, None, None, None))
        )

        val sectionRow = section.flatRateSchemeRow(businessService).build(scheme)
        sectionRow.status mustBe TLNotStarted
        sectionRow.url mustBe controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url
      }
    }

    "prerequisite is complete but flat rate scheme has partial data" must {
      "return task list in progress" in {
        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            fixedEstablishmentInManOrUk = false
          )),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
            hasTaxRepresentative = Some(false),
            startDate = Some(LocalDate.of(2017, 10, 10))
          )),
          flatRateScheme = Some(FlatRateScheme(joinFrs = Some(true)))
        )

        val sectionRow = section.flatRateSchemeRow(businessService).build(scheme)
        sectionRow.status mustBe TLInProgress
        sectionRow.url mustBe controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url
      }
    }

    "prerequisite is complete and all flat rate scheme data captured" must {
      "return task list completed" in {
        val scheme = validVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            fixedEstablishmentInManOrUk = false
          )),
          business = Some(validBusiness.copy(
            hasLandAndProperty = Some(false),
            otherBusinessInvolvement = Some(false),
            businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
          )),
          vatApplication = Some(completedVatApplicationWithGoodsAndServicesSection.copy(
            hasTaxRepresentative = Some(false),
            startDate = Some(LocalDate.of(2017, 10, 10))
          )),
          flatRateScheme = Some(validFlatRate)
        )

        val sectionRow = section.flatRateSchemeRow(businessService).build(scheme)
        sectionRow.status mustBe TLCompleted
        sectionRow.url mustBe controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url
      }
    }
  }
}