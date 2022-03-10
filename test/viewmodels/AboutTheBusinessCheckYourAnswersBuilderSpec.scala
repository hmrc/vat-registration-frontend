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

import controllers.registration.business.{routes => businessContactRoutes}
import controllers.registration.returns.{routes => returnsRoutes}
import controllers.registration.sicandcompliance.{routes => sicAndCompRoutes}
import models._
import models.api.returns.{OverseasCompliance, StoringWithinUk}
import models.api.{Address, NETP}
import play.api.i18n.{Lang, MessagesApi}
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class AboutTheBusinessCheckYourAnswersBuilderSpec extends VatRegSpec {

  object Builder extends AboutTheBusinessCheckYourAnswersBuilder

  val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages = messagesApi.preferred(Seq(Lang("en")))

  val sectionId = "cya.aboutTheBusiness"
  val testEmail = "test@foo.com"
  val testPhoneNumber = "123"
  val testMobileNumber = "987654"
  val testWebsite = "/test/url"
  val testNumWorkers = "12"
  val testZeroRated = "£10,000.50"
  val testNipAmount = "Value of goods: £1.00"
  val testWarehouseNumber = "testWarehouseName"
  val testWarehouseName = "testWarehouseNumber"
  val testTradingDetails = TradingDetails(tradingNameView = Some(TradingNameView(true, Some(testTradingName))), euGoods = Some(true))

  import models.view.SummaryListRowUtils._

  "the About The Business Check Your Answers Builder" when {
    "the user is not overseas" must {
      "show the non-overseas answers with a UK address" in {
        val scheme = emptyVatScheme.copy(
          businessContact = Some(validBusinessContactDetails),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          returns = Some(validReturns.copy(northernIrelandProtocol = Some(validNipCompliance))),
          sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(
            businessActivities = Some(BusinessActivities(List(sicCode))),
            hasLandAndProperty = Some(true)
          )),
          tradingDetails = Some(testTradingDetails)
        )

        val res = Builder.build(scheme)

        res mustBe SummaryList(
          List(
            optSummaryListRowSeq(s"$sectionId.ppob", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.PpobAddressController.startJourney.url)),
            optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.mobileBusiness", Some(testMobileNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(controllers.routes.ContactPreferenceController.showContactPreference.url)),
            optSummaryListRowBoolean(s"$sectionId.buySellLandAndProperty", Some(true), Some(controllers.routes.ContactPreferenceController.showContactPreference.url)),
            optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
            optSummaryListRowSeq(s"$sectionId.sicCodes", Some(Seq(s"${sicCode.code} - ${sicCode.description}")), Some(controllers.routes.SicAndComplianceController.returnToICL.url)),
            optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity.url)),
            optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(true), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
            optSummaryListRowString(s"$sectionId.numberOfWorkers", Some(testNumWorkers), Some(sicAndCompRoutes.WorkersController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.intermediarySupply", Some(true), Some(sicAndCompRoutes.SupplyWorkersIntermediaryController.show.url)),
            optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.registration.business.routes.TradingNameController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.applyForEori", Some(true), Some(controllers.registration.business.routes.ApplyForEoriController.show.url)),
            optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url)),
            optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("Yes", testNipAmount)), Some(returnsRoutes.SellOrMoveNipController.show.url)),
            optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("Yes", testNipAmount)), Some(returnsRoutes.ReceiveGoodsNipController.show.url))
          ).flatten
        )
      }
      "hide the zero rated row if the user's turnover is £0" in {
        val scheme = emptyVatScheme.copy(
          businessContact = Some(validBusinessContactDetails),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(estimates = TurnoverEstimates(0))),
          returns = Some(validReturns),
          sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(businessActivities = Some(BusinessActivities(List(sicCode))))),
          tradingDetails = Some(testTradingDetails)
        )

        val res = Builder.build(scheme)

        res mustBe SummaryList(
          List(
            optSummaryListRowSeq(s"$sectionId.ppob", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.PpobAddressController.startJourney.url)),
            optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.mobileBusiness", Some(testMobileNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(controllers.routes.ContactPreferenceController.showContactPreference.url)),
            optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
            optSummaryListRowSeq(s"$sectionId.sicCodes", Some(Seq(s"${sicCode.code} - ${sicCode.description}")), Some(controllers.routes.SicAndComplianceController.returnToICL.url)),
            optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity.url)),
            optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(true), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
            optSummaryListRowString(s"$sectionId.numberOfWorkers", Some(testNumWorkers), Some(sicAndCompRoutes.WorkersController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.intermediarySupply", Some(true), Some(sicAndCompRoutes.SupplyWorkersIntermediaryController.show.url)),
            optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.registration.business.routes.TradingNameController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.applyForEori", Some(true), Some(controllers.registration.business.routes.ApplyForEoriController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url))
          ).flatten
        )
      }
      "hide the other business activities row if the user only has one business activity" in {
        val scheme = emptyVatScheme.copy(
          businessContact = Some(validBusinessContactDetails),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          returns = Some(validReturns.copy(northernIrelandProtocol = Some(validNipCompliance))),
          sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour),
          tradingDetails = Some(testTradingDetails)
        )

        val res = Builder.build(scheme)

        res mustBe SummaryList(
          List(
            optSummaryListRowSeq(s"$sectionId.ppob", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.PpobAddressController.startJourney.url)),
            optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.mobileBusiness", Some(testMobileNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(controllers.routes.ContactPreferenceController.showContactPreference.url)),
            optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
            optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity.url)),
            optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(true), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
            optSummaryListRowString(s"$sectionId.numberOfWorkers", Some(testNumWorkers), Some(sicAndCompRoutes.WorkersController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.intermediarySupply", Some(true), Some(sicAndCompRoutes.SupplyWorkersIntermediaryController.show.url)),
            optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.registration.business.routes.TradingNameController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.applyForEori", Some(true), Some(controllers.registration.business.routes.ApplyForEoriController.show.url)),
            optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url)),
            optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("Yes", testNipAmount)), Some(returnsRoutes.SellOrMoveNipController.show.url)),
            optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("Yes", testNipAmount)), Some(returnsRoutes.ReceiveGoodsNipController.show.url))
          ).flatten
        )
      }
      "hide the compliance section if the user is supplying workers" in {
        val scheme = emptyVatScheme.copy(
          businessContact = Some(validBusinessContactDetails),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          returns = Some(validReturns.copy(northernIrelandProtocol = Some(validNipCompliance))),
          sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(supplyWorkers = Some(SupplyWorkers(false)))),
          tradingDetails = Some(testTradingDetails)
        )

        val res = Builder.build(scheme)

        res mustBe SummaryList(
          List(
            optSummaryListRowSeq(s"$sectionId.ppob", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.PpobAddressController.startJourney.url)),
            optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.mobileBusiness", Some(testMobileNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(controllers.routes.ContactPreferenceController.showContactPreference.url)),
            optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
            optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity.url)),
            optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(false), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
            optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.registration.business.routes.TradingNameController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.applyForEori", Some(true), Some(controllers.registration.business.routes.ApplyForEoriController.show.url)),
            optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url)),
            optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("Yes", testNipAmount)), Some(returnsRoutes.SellOrMoveNipController.show.url)),
            optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("Yes", testNipAmount)), Some(returnsRoutes.ReceiveGoodsNipController.show.url))
          ).flatten
        )
      }
      "not show the NIP compliance values if the user answered No to both questions" in {
        val scheme = emptyVatScheme.copy(
          businessContact = Some(validBusinessContactDetails),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          returns = Some(validReturns.copy(northernIrelandProtocol = Some(validNipCompliance.copy(
            goodsToEU = Some(ConditionalValue(false, None)),
            goodsFromEU = Some(ConditionalValue(false, None))
          )))),
          sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(supplyWorkers = Some(SupplyWorkers(false)))),
          tradingDetails = Some(testTradingDetails)
        )

        val res = Builder.build(scheme)

        res mustBe SummaryList(
          List(
            optSummaryListRowSeq(s"$sectionId.ppob", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.PpobAddressController.startJourney.url)),
            optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.mobileBusiness", Some(testMobileNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
            optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(controllers.routes.ContactPreferenceController.showContactPreference.url)),
            optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
            optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity.url)),
            optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(false), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
            optSummaryListRowString(s"$sectionId.tradingName", Some(testTradingName), Some(controllers.registration.business.routes.TradingNameController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.applyForEori", Some(true), Some(controllers.registration.business.routes.ApplyForEoriController.show.url)),
            optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
            optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url)),
            optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("No")), Some(returnsRoutes.SellOrMoveNipController.show.url)),
            optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("No")), Some(returnsRoutes.ReceiveGoodsNipController.show.url))
          ).flatten
        )
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
            sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(supplyWorkers = Some(SupplyWorkers(false)))),
            tradingDetails = Some(testTradingDetails)
          )

          val res = Builder.build(scheme)

          res mustBe SummaryList(
            List(
              optSummaryListRowSeq(s"$sectionId.ppob", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.InternationalPpobAddressController.show.url)),
              optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
              optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
              optSummaryListRowString(s"$sectionId.mobileBusiness", Some(testMobileNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
              optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
              optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(controllers.routes.ContactPreferenceController.showContactPreference.url)),
              optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
              optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity.url)),
              optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(false), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
              optSummaryListRowString(s"$sectionId.mandatoryName", Some(testTradingName), Some(controllers.registration.business.routes.MandatoryTradingNameController.show.url)),
              optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
              optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url)),
              optSummaryListRowBoolean(s"$sectionId.sendGoodsOverseas", Some(true), Some(returnsRoutes.SendGoodsOverseasController.show.url)),
              optSummaryListRowBoolean(s"$sectionId.sendGoodsToEu", Some(false), Some(returnsRoutes.SendEUGoodsController.show.url)),
              optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("No")), Some(returnsRoutes.SellOrMoveNipController.show.url)),
              optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("No")), Some(returnsRoutes.ReceiveGoodsNipController.show.url))
            ).flatten
          )
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
              sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(supplyWorkers = Some(SupplyWorkers(false)))),
              tradingDetails = Some(testTradingDetails)
            )

            val res = Builder.build(scheme)

            res mustBe SummaryList(
              List(
                optSummaryListRowSeq(s"$sectionId.ppob", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.InternationalPpobAddressController.show.url)),
                optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
                optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
                optSummaryListRowString(s"$sectionId.mobileBusiness", Some(testMobileNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
                optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
                optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(controllers.routes.ContactPreferenceController.showContactPreference.url)),
                optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
                optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity.url)),
                optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(false), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
                optSummaryListRowString(s"$sectionId.mandatoryName", Some(testTradingName), Some(controllers.registration.business.routes.MandatoryTradingNameController.show.url)),
                optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.sendGoodsOverseas", Some(true), Some(returnsRoutes.SendGoodsOverseasController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.sendGoodsToEu", Some(true), Some(returnsRoutes.SendEUGoodsController.show.url)),
                optSummaryListRowString(s"$sectionId.storingGoods", Some(s"$sectionId.storingGoods.uk"), Some(returnsRoutes.StoringGoodsController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.dispatchFromWarehouse", Some(true), Some(returnsRoutes.DispatchFromWarehouseController.show.url)),
                optSummaryListRowString(s"$sectionId.warehouseNumber", Some(testWarehouseNumber), Some(returnsRoutes.WarehouseNumberController.show.url)),
                optSummaryListRowString(s"$sectionId.warehouseName", Some(testWarehouseName), Some(returnsRoutes.WarehouseNameController.show.url)),
                optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("No")), Some(returnsRoutes.SellOrMoveNipController.show.url)),
                optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("No")), Some(returnsRoutes.ReceiveGoodsNipController.show.url))
              ).flatten
            )
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
              sicAndCompliance = Some(s4lVatSicAndComplianceWithLabour.copy(supplyWorkers = Some(SupplyWorkers(false)))),
              tradingDetails = Some(testTradingDetails)
            )

            val res = Builder.build(scheme)

            res mustBe SummaryList(
              List(
                optSummaryListRowSeq(s"$sectionId.ppob", Some(Address.normalisedSeq(testAddress)), Some(businessContactRoutes.InternationalPpobAddressController.show.url)),
                optSummaryListRowString(s"$sectionId.emailBusiness", Some(testEmail), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
                optSummaryListRowString(s"$sectionId.daytimePhoneBusiness", Some(testPhoneNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
                optSummaryListRowString(s"$sectionId.mobileBusiness", Some(testMobileNumber), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
                optSummaryListRowString(s"$sectionId.website", Some(testWebsite), Some(businessContactRoutes.BusinessContactDetailsController.show.url)),
                optSummaryListRowString(s"$sectionId.contactPreference", Some(ContactPreference.email), Some(controllers.routes.ContactPreferenceController.showContactPreference.url)),
                optSummaryListRowString(s"$sectionId.businessDescription", Some(testBusinessActivityDescription), Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)),
                optSummaryListRowString(s"$sectionId.mainSicCode", Some(sicCode.description), Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity.url)),
                optSummaryListRowBoolean(s"$sectionId.supplyWorkers", Some(false), Some(sicAndCompRoutes.SupplyWorkersController.show.url)),
                optSummaryListRowString(s"$sectionId.mandatoryName", Some(testTradingName), Some(controllers.registration.business.routes.MandatoryTradingNameController.show.url)),
                optSummaryListRowString(s"$sectionId.zeroRated", Some(testZeroRated), Some(returnsRoutes.ZeroRatedSuppliesController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.claimRefunds", Some(false), Some(returnsRoutes.ClaimRefundsController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.sendGoodsOverseas", Some(true), Some(returnsRoutes.SendGoodsOverseasController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.sendGoodsToEu", Some(true), Some(returnsRoutes.SendEUGoodsController.show.url)),
                optSummaryListRowString(s"$sectionId.storingGoods", Some(s"$sectionId.storingGoods.uk"), Some(returnsRoutes.StoringGoodsController.show.url)),
                optSummaryListRowBoolean(s"$sectionId.dispatchFromWarehouse", Some(false), Some(returnsRoutes.DispatchFromWarehouseController.show.url)),
                optSummaryListRowSeq(s"$sectionId.sellOrMoveNip", Some(Seq("No")), Some(returnsRoutes.SellOrMoveNipController.show.url)),
                optSummaryListRowSeq(s"$sectionId.receiveGoodsNip", Some(Seq("No")), Some(returnsRoutes.ReceiveGoodsNipController.show.url))
              ).flatten
            )
          }
        }
      }
    }
  }

}
