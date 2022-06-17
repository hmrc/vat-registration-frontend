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

import controllers.business.{routes => businessContactRoutes}
import controllers.returns.{routes => returnsRoutes}
import controllers.sicandcompliance.{routes => sicAndCompRoutes}
import models._
import models.api.returns.{OverseasCompliance, StoringWithinUk}
import models.api.{Address, NETP, VatScheme}
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

  val sectionId = "cya.aboutTheBusiness"
  val testEmail = "test@foo.com"
  val testPhoneNumber = "123"
  val testMobileNumber = "987654"
  val testWebsite = "/test/url"
  val testNumWorkers = "12"
  val testTurnoverEstimate = "£100.00"
  val testZeroTurnoverEstimate = "£0.00"
  val testZeroRated = "£10,000.50"
  val testNipAmount = "Value of goods: £1.00"
  val testWarehouseNumber = "testWarehouseName"
  val testWarehouseName = "testWarehouseNumber"
  val testTradingDetails = TradingDetails(tradingNameView = Some(TradingNameView(true, Some(testTradingName))), euGoods = Some(true))
  val testVrn = "testVrn"

  import models.view.SummaryListRowUtils._

  "the About The Business Check Your Answers builder" when {
    "the user is not overseas" must {
      "show the non-overseas answers with a UK address" in {
        val scheme = emptyVatScheme.copy(
          businessContact = Some(validBusinessContactDetails),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          returns = Some(validReturns.copy(northernIrelandProtocol = Some(validNipCompliance), appliedForExemption = Some(false))),
          sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(
            businessActivities = Some(BusinessActivities(List(sicCode))),
            hasLandAndProperty = Some(true),
            otherBusinessInvolvement = Some(false)
          )),
          tradingDetails = Some(testTradingDetails)
        )

        verifySummaryPageContents(scheme)
      }

      "show the non-overseas answers with a UK address and no company contact details" in {
        val scheme = emptyVatScheme.copy(
          businessContact = Some(validBusinessContactDetails),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          returns = Some(validReturns.copy(northernIrelandProtocol = Some(validNipCompliance), appliedForExemption = Some(false))),
          sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(
            businessActivities = Some(BusinessActivities(List(sicCode))),
            hasLandAndProperty = Some(true),
            otherBusinessInvolvement = Some(false)
          )),
          tradingDetails = Some(testTradingDetails)
        )

        verifySummaryPageContents(scheme)
      }

      "hide the zero rated row if the user's turnover is £0" in {
        val scheme = emptyVatScheme.copy(
          businessContact = Some(validBusinessContactDetails),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          returns = Some(validReturns.copy(turnoverEstimate = Some(0))),
          sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(businessActivities = Some(BusinessActivities(List(sicCode))), otherBusinessInvolvement = Some(false))),
          tradingDetails = Some(testTradingDetails)
        )

        val res = builder.build(scheme)

        res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
          rows = List(
            optSummaryListRowSeq(s"$sectionId.homeAddress", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.PpobAddressController.startJourney.url)),
            optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessEmailController.show.url)),
            optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessTelephoneNumberController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.hasWebsite", Some(true), Some(businessContactRoutes.HasWebsiteController.show.url)),
            optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessWebsiteAddressController.show.url)),
            optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(businessContactRoutes.ContactPreferenceController.showContactPreference.url)),
            optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
            optSummaryListRowSeq(s"$sectionId.sicCodes", Some(Seq(s"${sicCode.code} - ${sicCode.description}")), Some(sicAndCompRoutes.SicAndComplianceController.returnToICL.url)),
            optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(sicAndCompRoutes.SicAndComplianceController.showMainBusinessActivity.url)),
            optSummaryListRowBoolean(s"$sectionId.obi", Some(false), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(true), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
            optSummaryListRowString(s"$sectionId.numberOfWorkers", Some(testNumWorkers), Some(sicAndCompRoutes.WorkersController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.intermediarySupply", Some(true), Some(sicAndCompRoutes.SupplyWorkersIntermediaryController.show.url)),
            optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.business.routes.TradingNameController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.applyForEori", Some(true), Some(controllers.business.routes.ApplyForEoriController.show.url)),
            optSummaryListRowString(s"$sectionId.turnoverEstimate", Some(testZeroTurnoverEstimate), Some(returnsRoutes.TurnoverEstimateController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url))
          ).flatten
        ))))
      }

      "hide the other business activities row if the user only has one business activity" in {
        val scheme = emptyVatScheme.copy(
          businessContact = Some(validBusinessContactDetails),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          returns = Some(validReturns.copy(northernIrelandProtocol = Some(validNipCompliance))),
          sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(otherBusinessInvolvement = Some(false))),
          tradingDetails = Some(testTradingDetails)
        )

        val res = builder.build(scheme)

        res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
          rows = List(
            optSummaryListRowSeq(s"$sectionId.homeAddress", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.PpobAddressController.startJourney.url)),
            optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessEmailController.show.url)),
            optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessTelephoneNumberController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.hasWebsite", Some(true), Some(businessContactRoutes.HasWebsiteController.show.url)),
            optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessWebsiteAddressController.show.url)),
            optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(businessContactRoutes.ContactPreferenceController.showContactPreference.url)),
            optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
            optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(sicAndCompRoutes.SicAndComplianceController.showMainBusinessActivity.url)),
            optSummaryListRowBoolean(s"$sectionId.obi", Some(false), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(true), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
            optSummaryListRowString(s"$sectionId.numberOfWorkers", Some(testNumWorkers), Some(sicAndCompRoutes.WorkersController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.intermediarySupply", Some(true), Some(sicAndCompRoutes.SupplyWorkersIntermediaryController.show.url)),
            optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.business.routes.TradingNameController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.applyForEori", Some(true), Some(controllers.business.routes.ApplyForEoriController.show.url)),
            optSummaryListRowString(s"$sectionId.turnoverEstimate", Some(testTurnoverEstimate), Some(returnsRoutes.TurnoverEstimateController.show.url)),
            optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
            optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("Yes", testNipAmount)), Some(returnsRoutes.SellOrMoveNipController.show.url)),
            optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("Yes", testNipAmount)), Some(returnsRoutes.ReceiveGoodsNipController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url))
          ).flatten
        ))))
      }

      "hide the compliance section if the user is supplying workers" in {
        val scheme = emptyVatScheme.copy(
          businessContact = Some(validBusinessContactDetails),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          returns = Some(validReturns.copy(northernIrelandProtocol = Some(validNipCompliance))),
          sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(supplyWorkers = Some(SupplyWorkers(false)), otherBusinessInvolvement = Some(false))),
          tradingDetails = Some(testTradingDetails)
        )

        val res = builder.build(scheme)

        res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
          rows = List(
            optSummaryListRowSeq(s"$sectionId.homeAddress", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.PpobAddressController.startJourney.url)),
            optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessEmailController.show.url)),
            optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessTelephoneNumberController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.hasWebsite", Some(true), Some(businessContactRoutes.HasWebsiteController.show.url)),
            optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessWebsiteAddressController.show.url)),
            optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(businessContactRoutes.ContactPreferenceController.showContactPreference.url)),
            optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
            optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(sicAndCompRoutes.SicAndComplianceController.showMainBusinessActivity.url)),
            optSummaryListRowBoolean(s"$sectionId.obi", Some(false), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(false), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
            optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.business.routes.TradingNameController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.applyForEori", Some(true), Some(controllers.business.routes.ApplyForEoriController.show.url)),
            optSummaryListRowString(s"$sectionId.turnoverEstimate", Some(testTurnoverEstimate), Some(returnsRoutes.TurnoverEstimateController.show.url)),
            optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
            optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("Yes", testNipAmount)), Some(returnsRoutes.SellOrMoveNipController.show.url)),
            optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("Yes", testNipAmount)), Some(returnsRoutes.ReceiveGoodsNipController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url))
          ).flatten
        ))))
      }

      "not show the NIP compliance values if the user answered No to both questions" in {
        val scheme = emptyVatScheme.copy(
          businessContact = Some(validBusinessContactDetails),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          returns = Some(validReturns.copy(northernIrelandProtocol = Some(validNipCompliance.copy(
            goodsToEU = Some(ConditionalValue(false, None)),
            goodsFromEU = Some(ConditionalValue(false, None))
          )))),
          sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(supplyWorkers = Some(SupplyWorkers(false)), otherBusinessInvolvement = Some(false))),
          tradingDetails = Some(testTradingDetails)
        )

        val res = builder.build(scheme)

        res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
          rows = List(
            optSummaryListRowSeq(s"$sectionId.homeAddress", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.PpobAddressController.startJourney.url)),
            optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessEmailController.show.url)),
            optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessTelephoneNumberController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.hasWebsite", Some(true), Some(businessContactRoutes.HasWebsiteController.show.url)),
            optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessWebsiteAddressController.show.url)),
            optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(businessContactRoutes.ContactPreferenceController.showContactPreference.url)),
            optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
            optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(sicAndCompRoutes.SicAndComplianceController.showMainBusinessActivity.url)),
            optSummaryListRowBoolean(s"$sectionId.obi", Some(false), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(false), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
            optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.business.routes.TradingNameController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.applyForEori", Some(true), Some(controllers.business.routes.ApplyForEoriController.show.url)),
            optSummaryListRowString(s"$sectionId.turnoverEstimate", Some(testTurnoverEstimate), Some(returnsRoutes.TurnoverEstimateController.show.url)),
            optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
            optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("No")), Some(returnsRoutes.SellOrMoveNipController.show.url)),
            optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("No")), Some(returnsRoutes.ReceiveGoodsNipController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url))
          ).flatten
        ))))
      }
    }

    "the user is overseas" when {
      "the user is not sending goods to the EU" must {
        "show the overseas answers with an international address, mandatory trading name, without questions regarding sending goods to the EU" in {
          val scheme = emptyVatScheme.copy(
            businessContact = Some(validBusinessContactDetails),
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = NETP)),
            returns = Some(validReturns.copy(
              northernIrelandProtocol = Some(validNipCompliance.copy(
                goodsToEU = Some(ConditionalValue(false, None)),
                goodsFromEU = Some(ConditionalValue(false, None))
              )),
              overseasCompliance = Some(OverseasCompliance(
                goodsToOverseas = Some(true),
                goodsToEu = Some(false)
              ))
            )),
            sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(supplyWorkers = Some(SupplyWorkers(false)), otherBusinessInvolvement = Some(false))),
            tradingDetails = Some(testTradingDetails)
          )

          val res = builder.build(scheme)

          res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
            rows = List(
              optSummaryListRowSeq(s"$sectionId.homeAddress", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.InternationalPpobAddressController.show.url)),
              optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessEmailController.show.url)),
              optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessTelephoneNumberController.show.url)),
              optSummaryListRowBoolean(s"$sectionId.hasWebsite", Some(true), Some(businessContactRoutes.HasWebsiteController.show.url)),
              optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessWebsiteAddressController.show.url)),
              optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(businessContactRoutes.ContactPreferenceController.showContactPreference.url)),
              optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
              optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(sicAndCompRoutes.SicAndComplianceController.showMainBusinessActivity.url)),
              optSummaryListRowBoolean(s"$sectionId.obi", Some(false), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url)),
              optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(false), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
              optSummaryListRowString(s"$sectionId.mandatoryName", Some(testTradingName), Some(controllers.business.routes.MandatoryTradingNameController.show.url)),
              optSummaryListRowString(s"$sectionId.turnoverEstimate", Some(testTurnoverEstimate), Some(returnsRoutes.TurnoverEstimateController.show.url)),
              optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
              optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("No")), Some(returnsRoutes.SellOrMoveNipController.show.url)),
              optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("No")), Some(returnsRoutes.ReceiveGoodsNipController.show.url)),
              optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url)),
              optSummaryListRowBoolean(s"$sectionId.sendGoodsOverseas", Some(true), Some(returnsRoutes.SendGoodsOverseasController.show.url))
            ).flatten
          ))))
        }
      }

      "the user is sending goods to the EU" when {
        "the user is using a dispatch warehouse" must {
          "show the overseas answers with the EU questions, dispatch questions and international address" in {
            val scheme = emptyVatScheme.copy(
              businessContact = Some(validBusinessContactDetails),
              eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = NETP)),
              returns = Some(validReturns.copy(
                northernIrelandProtocol = Some(validNipCompliance.copy(
                  goodsToEU = Some(ConditionalValue(false, None)),
                  goodsFromEU = Some(ConditionalValue(false, None)),
                )),
                overseasCompliance = Some(OverseasCompliance(
                  goodsToOverseas = Some(true),
                  goodsToEu = Some(true),
                  storingGoodsForDispatch = Some(StoringWithinUk),
                  usingWarehouse = Some(true),
                  fulfilmentWarehouseNumber = Some(testWarehouseNumber),
                  fulfilmentWarehouseName = Some(testWarehouseName)
                ))
              )),
              sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(supplyWorkers = Some(SupplyWorkers(false)), otherBusinessInvolvement = Some(false))),
              tradingDetails = Some(testTradingDetails)
            )

            val res = builder.build(scheme)

            res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
              rows = List(
                optSummaryListRowSeq(s"$sectionId.homeAddress", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.InternationalPpobAddressController.show.url)),
                optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessEmailController.show.url)),
                optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessTelephoneNumberController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.hasWebsite", Some(true), Some(businessContactRoutes.HasWebsiteController.show.url)),
                optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessWebsiteAddressController.show.url)),
                optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(businessContactRoutes.ContactPreferenceController.showContactPreference.url)),
                optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
                optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(sicAndCompRoutes.SicAndComplianceController.showMainBusinessActivity.url)),
                optSummaryListRowBoolean(s"$sectionId.obi", Some(false), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(false), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
                optSummaryListRowString(s"$sectionId.mandatoryName", Some(testTradingName), Some(controllers.business.routes.MandatoryTradingNameController.show.url)),
                optSummaryListRowString(s"$sectionId.turnoverEstimate", Some(testTurnoverEstimate), Some(returnsRoutes.TurnoverEstimateController.show.url)),
                optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
                optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("No")), Some(returnsRoutes.SellOrMoveNipController.show.url)),
                optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("No")), Some(returnsRoutes.ReceiveGoodsNipController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.sendGoodsOverseas", Some(true), Some(returnsRoutes.SendGoodsOverseasController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.sendGoodsToEu", Some(true), Some(returnsRoutes.SendEUGoodsController.show.url)),
                optSummaryListRowString(s"$sectionId.storingGoods", Some(s"$sectionId.storingGoods.uk"), Some(returnsRoutes.StoringGoodsController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.dispatchFromWarehouse", Some(true), Some(returnsRoutes.DispatchFromWarehouseController.show.url)),
                optSummaryListRowString(s"$sectionId.warehouseNumber", Some(testWarehouseNumber), Some(returnsRoutes.WarehouseNumberController.show.url)),
                optSummaryListRowString(s"$sectionId.warehouseName", Some(testWarehouseName), Some(returnsRoutes.WarehouseNameController.show.url))
              ).flatten
            ))))
          }
        }

        "the user is not using a dispatch warehouse" must {
          "show the overseas answers with the EU questions with an international address, minus the dispatch questions" in {
            val scheme = emptyVatScheme.copy(
              businessContact = Some(validBusinessContactDetails),
              eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = NETP)),
              returns = Some(validReturns.copy(
                northernIrelandProtocol = Some(validNipCompliance.copy(
                  goodsToEU = Some(ConditionalValue(false, None)),
                  goodsFromEU = Some(ConditionalValue(false, None)),
                )),
                overseasCompliance = Some(OverseasCompliance(
                  goodsToOverseas = Some(true),
                  goodsToEu = Some(true),
                  storingGoodsForDispatch = Some(StoringWithinUk),
                  usingWarehouse = Some(false)
                ))
              )),
              sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(supplyWorkers = Some(SupplyWorkers(false)), otherBusinessInvolvement = Some(false))),
              tradingDetails = Some(testTradingDetails)
            )

            val res = builder.build(scheme)

            res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
              rows = List(
                optSummaryListRowSeq(s"$sectionId.homeAddress", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.InternationalPpobAddressController.show.url)),
                optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessEmailController.show.url)),
                optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessTelephoneNumberController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.hasWebsite", Some(true), Some(businessContactRoutes.HasWebsiteController.show.url)),
                optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessWebsiteAddressController.show.url)),
                optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(businessContactRoutes.ContactPreferenceController.showContactPreference.url)),
                optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
                optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(sicAndCompRoutes.SicAndComplianceController.showMainBusinessActivity.url)),
                optSummaryListRowBoolean(s"$sectionId.obi", Some(false), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(false), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
                optSummaryListRowString(s"$sectionId.mandatoryName", Some(testTradingName), Some(controllers.business.routes.MandatoryTradingNameController.show.url)),
                optSummaryListRowString(s"$sectionId.turnoverEstimate", Some(testTurnoverEstimate), Some(returnsRoutes.TurnoverEstimateController.show.url)),
                optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
                optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("No")), Some(returnsRoutes.SellOrMoveNipController.show.url)),
                optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("No")), Some(returnsRoutes.ReceiveGoodsNipController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.sendGoodsOverseas", Some(true), Some(returnsRoutes.SendGoodsOverseasController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.sendGoodsToEu", Some(true), Some(returnsRoutes.SendEUGoodsController.show.url)),
                optSummaryListRowString(s"$sectionId.storingGoods", Some(s"$sectionId.storingGoods.uk"), Some(returnsRoutes.StoringGoodsController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.dispatchFromWarehouse", Some(false), Some(returnsRoutes.DispatchFromWarehouseController.show.url))
              ).flatten
            ))))
          }
        }
      }
    }
  }

  private def verifySummaryPageContents(scheme: VatScheme) = {
    val res = builder.build(scheme)

    res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(
      rows = List(
        optSummaryListRowSeq(s"$sectionId.homeAddress", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.PpobAddressController.startJourney.url)),
        optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessEmailController.show.url)),
        optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessTelephoneNumberController.show.url)),
        optSummaryListRowBoolean(s"$sectionId.hasWebsite", Some(true), Some(businessContactRoutes.HasWebsiteController.show.url)),
        optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessWebsiteAddressController.show.url)),
        optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(businessContactRoutes.ContactPreferenceController.showContactPreference.url)),
        optSummaryListRowBoolean(s"$sectionId.buySellLandAndProperty", Some(true), Some(businessContactRoutes.ContactPreferenceController.showContactPreference.url)),
        optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
        optSummaryListRowSeq(s"$sectionId.sicCodes", Some(Seq(s"${sicCode.code} - ${sicCode.description}")), Some(sicAndCompRoutes.SicAndComplianceController.returnToICL.url)),
        optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(sicAndCompRoutes.SicAndComplianceController.showMainBusinessActivity.url)),
        optSummaryListRowBoolean(s"$sectionId.obi", Some(false), Some(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url)),
        optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(true), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
        optSummaryListRowString(s"$sectionId.numberOfWorkers", Some(testNumWorkers), Some(sicAndCompRoutes.WorkersController.show.url)),
        optSummaryListRowBoolean(s"$sectionId.intermediarySupply", Some(true), Some(sicAndCompRoutes.SupplyWorkersIntermediaryController.show.url)),
        optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.business.routes.TradingNameController.show.url)),
        optSummaryListRowBoolean(s"$sectionId.applyForEori", Some(true), Some(controllers.business.routes.ApplyForEoriController.show.url)),
        optSummaryListRowString(s"$sectionId.turnoverEstimate", Some(testTurnoverEstimate), Some(returnsRoutes.TurnoverEstimateController.show.url)),
        optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
        optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("Yes", testNipAmount)), Some(returnsRoutes.SellOrMoveNipController.show.url)),
        optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("Yes", testNipAmount)), Some(returnsRoutes.ReceiveGoodsNipController.show.url)),
        optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url)),
        optSummaryListRowBoolean(s"$sectionId.vatExemption", Some(false), Some(returnsRoutes.VatExemptionController.show.url))
      ).flatten
    ))))
  }
}
