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
import featureswitch.core.config.FeatureSwitching
import models.api.returns.{Returns, StoringOverseas, StoringWithinUk}
import models.api._
import models.view.SummaryListRowUtils.{optSummaryListRowBoolean, optSummaryListRowSeq, optSummaryListRowString}
import models.{BusinessContact, SicAndCompliance, TradingDetails}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.InternalServerException

// scalastyle:off
class AboutTheBusinessCheckYourAnswersBuilder extends FeatureSwitching {

  val sectionId = "cya.aboutTheBusiness"

  private def missingSection(section: String) =
    new InternalServerException(s"[AboutTheBusinessCheckYourAnswersBuilder] Couldn't construct CYA due to missing section: $section")

  def build(vatScheme: VatScheme)(implicit messages: Messages): SummaryList = {
    val businessContact = vatScheme.businessContact.getOrElse(throw missingSection("Business Contact"))
    val sicAndCompliance = vatScheme.sicAndCompliance.getOrElse(throw missingSection("Sic And Compliance"))
    val tradingDetails = vatScheme.tradingDetails
    val returns = vatScheme.returns.getOrElse(throw missingSection("Returns"))
    val partyType = vatScheme.eligibilitySubmissionData.map(_.partyType).getOrElse(throw missingSection("Eligibility"))

    SummaryList(
      List(
        ppobAddress(businessContact, partyType),
        businessEmailAddress(businessContact),
        businessDaytimePhoneNumber(businessContact),
        businessMobilePhoneNumber(businessContact),
        businessWebsite(businessContact),
        contactPreference(businessContact),
        buySellLandOrProperty(sicAndCompliance),
        businessDescription(sicAndCompliance),
        otherBusinessActivities(sicAndCompliance),
        mainBusinessActivity(sicAndCompliance),
        supplyWorkers(sicAndCompliance)
      ).flatten ++
      complianceSection(sicAndCompliance) ++
      List(
        tradingName(tradingDetails, partyType),
        importsOrExports(tradingDetails, partyType),
        applyForEori(tradingDetails, partyType),
        zeroRatedTurnover(vatScheme),
        claimRefunds(returns),
        sendGoodsOverseas(returns),
        sendGoodsToEu(returns)
      ).flatten ++
      netpSection(returns, partyType) ++
      nipSection(returns)
    )
  }

  private def ppobAddress(businessContact: BusinessContact, partyType: PartyType)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowSeq(
      s"$sectionId.ppob",
      businessContact.ppobAddress.map(Address.normalisedSeq),
      partyType match {
        case NETP =>
          Some(businessContactRoutes.InternationalPpobAddressController.show.url)
        case _ =>
          Some(businessContactRoutes.PpobAddressController.startJourney.url)
      }
    )

  private def businessEmailAddress(businessContact: BusinessContact)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.emailBusiness",
      businessContact.companyContactDetails.map(_.email),
      Some(businessContactRoutes.BusinessContactDetailsController.show.url)
    )

  private def businessDaytimePhoneNumber(businessContact: BusinessContact)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.daytimePhoneBusiness",
      businessContact.companyContactDetails.flatMap(_.phoneNumber),
      Some(businessContactRoutes.BusinessContactDetailsController.show.url)
    )

  private def businessMobilePhoneNumber(businessContact: BusinessContact)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.mobileBusiness",
      businessContact.companyContactDetails.flatMap(_.mobileNumber),
      Some(businessContactRoutes.BusinessContactDetailsController.show.url)
    )

  private def businessWebsite(businessContact: BusinessContact)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.website",
      businessContact.companyContactDetails.flatMap(_.websiteAddress),
      Some(businessContactRoutes.BusinessContactDetailsController.show.url)
    )

  private def contactPreference(businessContact: BusinessContact)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.contactPreference",
      businessContact.contactPreference.map(_.toString),
      Some(controllers.routes.ContactPreferenceController.showContactPreference.url)
    )

  private def buySellLandOrProperty(sicAndCompliance: SicAndCompliance)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.buySellLandAndProperty",
      sicAndCompliance.hasLandAndProperty,
      Some(controllers.routes.ContactPreferenceController.showContactPreference.url)
    )

  private def businessDescription(sicAndCompliance: SicAndCompliance)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.businessDescription",
      sicAndCompliance.description.map(_.description),
      Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)
    )

  private def mainBusinessActivity(sicAndCompliance: SicAndCompliance)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.mainSicCode",
      sicAndCompliance.mainBusinessActivity.flatMap(_.mainBusinessActivity.map(_.description)),
      Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity.url)
    )

  private def otherBusinessActivities(sicAndCompliance: SicAndCompliance)(implicit messages: Messages): Option[SummaryListRow] =
    if (sicAndCompliance.businessActivities.exists(_.sicCodes.nonEmpty == true)) {
      optSummaryListRowSeq(
        s"$sectionId.sicCodes",
        sicAndCompliance.businessActivities.map(codes => codes.sicCodes.map(
          sicCode => sicCode.code + " - " + sicCode.description
        )),
        Some(controllers.routes.SicAndComplianceController.returnToICL.url)
      )
    } else {
      None
    }

  private def supplyWorkers(sicAndCompliance: SicAndCompliance)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.supplyWorkers",
      sicAndCompliance.supplyWorkers.map(_.yesNo),
      Some(sicAndCompRoutes.SupplyWorkersController.show.url)
    )

  private def numberOfWorkers(sicAndCompliance: SicAndCompliance)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.numberOfWorkers",
      sicAndCompliance.workers.map(_.numberOfWorkers.toString),
      Some(sicAndCompRoutes.WorkersController.show.url)
    )

  private def intermediaryArrangingSupplyOfWorkers(sicAndCompliance: SicAndCompliance)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.intermediarySupply",
      sicAndCompliance.intermediarySupply.map(_.yesNo),
      Some(sicAndCompRoutes.SupplyWorkersIntermediaryController.show.url)
    )

  private def tradingName(tradingDetails: Option[TradingDetails], partyType: PartyType)(implicit messages: Messages): Option[SummaryListRow] = {
    val tradingNameOptional: Boolean = Seq(UkCompany, RegSociety, CharitableOrg, NonUkNonEstablished, Trust, UnincorpAssoc).contains(partyType)

    optSummaryListRowString(
      if (tradingNameOptional) {
        s"$sectionId.tradingName"
      } else {
        s"$sectionId.mandatoryName"
      },
      tradingDetails.flatMap(_.tradingNameView.flatMap(_.tradingName)) match {
        case None => Some("app.common.no")
        case optTradingName => optTradingName
      },
      if (tradingNameOptional) {
        Some(controllers.registration.business.routes.TradingNameController.show.url)
      } else {
        Some(controllers.registration.business.routes.MandatoryTradingNameController.show.url)
      }
    )
  }

  private def importsOrExports(tradingDetails: Option[TradingDetails], partyType: PartyType)(implicit messages: Messages): Option[SummaryListRow] =
    if (partyType == NETP || partyType == NonUkNonEstablished) {
      None
    } else {
      optSummaryListRowBoolean(
        s"$sectionId.importsOrExports",
        tradingDetails.flatMap(_.tradeVatGoodsOutsideUk),
        Some(controllers.registration.business.routes.ImportsOrExportsController.show.url)
      )
    }

  private def applyForEori(tradingDetails: Option[TradingDetails], partyType: PartyType)(implicit messages: Messages): Option[SummaryListRow] =
    if (partyType == NETP || partyType == NonUkNonEstablished) {
      None
    } else {
      optSummaryListRowBoolean(
        s"$sectionId.applyForEori",
        tradingDetails.flatMap(_.euGoods),
        Some(controllers.registration.business.routes.ApplyForEoriController.show.url)
      )
    }


  private def zeroRatedTurnover(vatScheme: VatScheme)(implicit messages: Messages): Option[SummaryListRow] =
    if (vatScheme.eligibilitySubmissionData.map(_.estimates.turnoverEstimate).contains(0)) None else optSummaryListRowString(
      s"$sectionId.zeroRated",
      vatScheme.returns.flatMap(_.zeroRatedSupplies.map(Formatters.currency)),
      Some(returnsRoutes.ZeroRatedSuppliesController.show.url)
    )

  private def claimRefunds(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.claimRefunds",
      returns.reclaimVatOnMostReturns,
      Some(returnsRoutes.ClaimRefundsController.show.url)
    )

  private def sendGoodsOverseas(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.sendGoodsOverseas",
      returns.overseasCompliance.flatMap(_.goodsToOverseas),
      Some(returnsRoutes.SendGoodsOverseasController.show.url)
    )

  private def sendGoodsToEu(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.sendGoodsToEu",
      returns.overseasCompliance.flatMap(_.goodsToEu),
      Some(returnsRoutes.SendEUGoodsController.show.url)
    )

  private def storingGoods(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.storingGoods",
      returns.overseasCompliance.flatMap(_.storingGoodsForDispatch).map {
        case StoringWithinUk => s"$sectionId.storingGoods.uk"
        case StoringOverseas => s"$sectionId.storingGoods.overseas"
      },
      Some(returnsRoutes.StoringGoodsController.show.url)
    )

  private def dispatchFromWarehouse(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.dispatchFromWarehouse",
      returns.overseasCompliance.flatMap(_.usingWarehouse),
      Some(returnsRoutes.DispatchFromWarehouseController.show.url)
    )

  private def warehouseNumber(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.warehouseNumber",
      returns.overseasCompliance.flatMap(_.fulfilmentWarehouseNumber),
      Some(returnsRoutes.WarehouseNumberController.show.url)
    )

  private def warehouseName(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.warehouseName",
      returns.overseasCompliance.flatMap(_.fulfilmentWarehouseName),
      Some(returnsRoutes.WarehouseNameController.show.url)
    )

  private def sellOrMoveNip(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowSeq(
      s"$sectionId.sellOrMoveNip",
      Some(Seq(
        returns.northernIrelandProtocol.flatMap(_.goodsToEU).map(answer =>
          if (answer.answer) messages("app.common.yes")
          else messages("app.common.no")
        ),
        returns.northernIrelandProtocol.flatMap(_.goodsToEU).flatMap(_.value.map { value =>
          s"${messages("cya.aboutTheBusiness.valueOfGoods")} ${Formatters.currency(value)}"
        })
      ).flatten),
      Some(returnsRoutes.SellOrMoveNipController.show.url)
    )

  private def receiveGoodsNip(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowSeq(
      s"$sectionId.receiveGoodsNip",
      Some(Seq(
        returns.northernIrelandProtocol.flatMap(_.goodsFromEU).map(answer =>
          if (answer.answer) messages("app.common.yes")
          else messages("app.common.no")
        ),
        returns.northernIrelandProtocol.flatMap(_.goodsFromEU).flatMap(_.value.map { value =>
          s"${messages("cya.aboutTheBusiness.valueOfGoods")} ${Formatters.currency(value)}"
        })
      ).flatten),
      Some(returnsRoutes.ReceiveGoodsNipController.show.url)
    )

  private def complianceSection(sicAndCompliance: SicAndCompliance)(implicit messages: Messages): List[SummaryListRow] =
    if (sicAndCompliance.supplyWorkers.exists(_.yesNo == true)) {
      List(
        numberOfWorkers(sicAndCompliance),
        intermediaryArrangingSupplyOfWorkers(sicAndCompliance)
      ).flatten
    } else {
      Nil
    }

  private def netpSection(returns: Returns, partyType: PartyType)(implicit messages: Messages): List[SummaryListRow] =
    if (partyType == NETP || partyType == NonUkNonEstablished) {
      List(
        storingGoods(returns),
        dispatchFromWarehouse(returns)
      ).flatten ++ {
        if (returns.overseasCompliance.exists(_.usingWarehouse.contains(true))) {
          List(
            warehouseNumber(returns),
            warehouseName(returns)
          ).flatten
        } else {
          Nil
        }
      }
    }
    else
    {
      Nil
    }

  private def nipSection(returns: Returns)(implicit messages: Messages): List[SummaryListRow] = {
    if (returns.northernIrelandProtocol.isDefined) {
      List(
        sellOrMoveNip(returns),
        receiveGoodsNip(returns)
      ).flatten
    } else {
      Nil
    }

  }

}
