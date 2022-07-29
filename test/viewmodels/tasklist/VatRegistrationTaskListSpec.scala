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

import fixtures.VatRegistrationFixture
import models.api.vatapplication.{OverseasCompliance, StoringOverseas, StoringWithinUk}
import models.api.{NETP, NonUkNonEstablished, PartyType, UkCompany, VatScheme}
import models.{ConditionalValue, NIPTurnover}
import testHelpers.VatRegSpec

class VatRegistrationTaskListSpec extends VatRegSpec with VatRegistrationFixture {
  val section: VatRegistrationTaskList = app.injector.instanceOf[VatRegistrationTaskList]

  "checks for goods and services row" when {
    "prerequisite not complete" must {
      "return TLCannotStart" in {
        val sectionRow = section.buildGoodsAndServicesRow.build(emptyVatScheme)
        sectionRow.status mustBe TLCannotStart
      }
    }

    "prerequisite is complete but goods and services flow hasn't started" must {
      "return TLNotStarted with correct url depending on party type" in {
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

          val sectionRow = section.buildGoodsAndServicesRow.build(schema)
          sectionRow.status mustBe TLNotStarted
          sectionRow.url mustBe expectedUrl
        }

        verifyNotStartedFlow(NETP, controllers.vatapplication.routes.TurnoverEstimateController.show.url)
        verifyNotStartedFlow(UkCompany, controllers.vatapplication.routes.ImportsOrExportsController.show.url)
      }
    }

    "prerequisite is complete and goods and services captured with core details" must {
      "return TLCompleted with no imports/exports for NETP/NonUkNonEstablished" in {

        def verifyCompletion(partyType: PartyType) = {
          val scheme = validVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = partyType)),
            business = Some(validBusiness.copy(
              hasLandAndProperty = Some(false),
              otherBusinessInvolvement = Some(false),
              businessActivities = Some(List(validBusiness.mainBusinessActivity.get))
            )),
            vatApplication = Some(validVatApplication.copy(
              tradeVatGoodsOutsideUk = None,
              eoriRequested = None,
              overseasCompliance = Some(OverseasCompliance(
                goodsToOverseas = Some(false),
                storingGoodsForDispatch = Some(StoringOverseas)
              )),
              northernIrelandProtocol = Some(NIPTurnover(
                goodsToEU = Some(ConditionalValue(answer = false, None)),
                goodsFromEU = Some(ConditionalValue(answer = false, None)),
              ))
            ))
          )

          val sectionRow = section.buildGoodsAndServicesRow.build(scheme)
          sectionRow.status mustBe TLCompleted
          sectionRow.url mustBe controllers.vatapplication.routes.TurnoverEstimateController.show.url
        }

        verifyCompletion(NETP)
        verifyCompletion(NonUkNonEstablished)
      }

      "return TLCompleted with imports/exports for non NETP" in {
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
          ))
        )

        val sectionRow = section.buildGoodsAndServicesRow.build(scheme)
        sectionRow.status mustBe TLCompleted
        sectionRow.url mustBe controllers.vatapplication.routes.ImportsOrExportsController.show.url
      }
    }

    "prerequisite is complete but goods and services details partially submitted" must {
      def verifyInProgressSectionRow(scheme: VatScheme) = {
        val sectionRow = section.buildGoodsAndServicesRow.build(scheme)
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

  "checks for bank account details row" when {
    "prerequisite not complete" must {
      "return TLCannotStart" in {
        val sectionRow = section.bankAccountDetailsRow.build(emptyVatScheme)
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


        val sectionRow = section.bankAccountDetailsRow.build(scheme)
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

        val sectionRow = section.bankAccountDetailsRow.build(scheme)
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

        val sectionRow = section.bankAccountDetailsRow.build(scheme)
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

        val sectionRow = section.bankAccountDetailsRow.build(scheme)
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

        val sectionRow = section.bankAccountDetailsRow.build(scheme)
        sectionRow.status mustBe TLCompleted
        sectionRow.url mustBe controllers.bankdetails.routes.HasBankAccountController.show.url
      }
    }
  }
}
