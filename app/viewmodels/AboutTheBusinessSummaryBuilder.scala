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

import controllers.returns.{routes => returnsRoutes}
import featureswitch.core.config.FeatureSwitching
import models.api._
import models.api.returns.{Returns, StoringOverseas, StoringWithinUk}
import models.view.SummaryListRowUtils._
import models.{Business, TradingDetails}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.InternalServerException

import javax.inject.Inject

// scalastyle:off
class AboutTheBusinessSummaryBuilder @Inject()(govukSummaryList: GovukSummaryList) extends FeatureSwitching {

  val sectionId = "cya.aboutTheBusiness"

  private def missingSection(section: String) =
    new InternalServerException(s"[AboutTheBusinessCheckYourAnswersBuilder] Couldn't construct CYA due to missing section: $section")

  def build(vatScheme: VatScheme)(implicit messages: Messages): HtmlFormat.Appendable = {
    val business = vatScheme.business.getOrElse(throw missingSection("Business details"))
    val tradingDetails = vatScheme.tradingDetails
    val returns = vatScheme.returns.getOrElse(throw missingSection("Returns"))
    val partyType = vatScheme.eligibilitySubmissionData.map(_.partyType).getOrElse(throw missingSection("Eligibility"))

    HtmlFormat.fill(List(
      govukSummaryList(SummaryList(
        rows = List(
          ppobAddress(business, partyType),
          businessEmailAddress(business),
          businessDaytimePhoneNumber(business),
          businessHasWebsite(business),
          businessWebsite(business),
          contactPreference(business),
          buySellLandOrProperty(business),
          businessDescription(business),
          otherBusinessActivities(business),
          mainBusinessActivity(business),
          otherBusinessInvolvements(business),
          supplyWorkers(business)
        ).flatten ++
          complianceSection(business) ++
          List(
            tradingName(tradingDetails, partyType),
            importsOrExports(tradingDetails, partyType),
            applyForEori(tradingDetails, partyType),
            turnoverEstimate(returns),
            zeroRatedTurnover(vatScheme)
          ).flatten ++
          nipSection(returns) ++
          List(
            claimRefunds(returns),
            vatExemption(returns),
            sendGoodsOverseas(returns),
            sendGoodsToEu(returns)
          ).flatten ++
          netpSection(returns, partyType)
      ))
    ))
  }

  private def ppobAddress(business: Business, partyType: PartyType)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowSeq(
      s"$sectionId.homeAddress",
      business.ppobAddress.map(Address.normalisedSeq),
      partyType match {
        case NETP =>
          Some(controllers.business.routes.InternationalPpobAddressController.show.url)
        case _ =>
          Some(controllers.business.routes.PpobAddressController.startJourney.url)
      }
    )

  private def businessEmailAddress(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.emailBusiness",
      business.email,
      Some(controllers.business.routes.BusinessEmailController.show.url)
    )

  private def businessDaytimePhoneNumber(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.daytimePhoneBusiness",
      business.telephoneNumber,
      Some(controllers.business.routes.BusinessTelephoneNumberController.show.url)
    )

  private def businessHasWebsite(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.hasWebsite",
      business.hasWebsite,
      Some(controllers.business.routes.HasWebsiteController.show.url)
    )

  private def businessWebsite(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.website",
      business.website,
      Some(controllers.business.routes.BusinessWebsiteAddressController.show.url)
    )

  private def contactPreference(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.contactPreference",
      business.contactPreference.map(_.toString),
      Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)
    )

  private def buySellLandOrProperty(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.buySellLandAndProperty",
      business.hasLandAndProperty,
      Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)
    )

  private def businessDescription(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.businessDescription",
      business.businessDescription,
      Some(controllers.business.routes.BusinessActivityDescriptionController.show.url)
    )

  private def mainBusinessActivity(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.mainSicCode",
      business.mainBusinessActivity.map(_.description),
      Some(controllers.business.routes.SicController.showMainBusinessActivity.url)
    )

  private def otherBusinessInvolvements(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.obi",
      business.otherBusinessInvolvement,
      Some(controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url)
    )

  private def otherBusinessActivities(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    if (business.businessActivities.exists(_.nonEmpty == true)) {
      optSummaryListRowSeq(
        s"$sectionId.sicCodes",
        business.businessActivities.map(codes => codes.map(
          sicCode => sicCode.code + " - " + sicCode.description
        )),
        Some(controllers.business.routes.SicController.returnToICL.url)
      )
    } else {
      None
    }

  private def supplyWorkers(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.supplyWorkers",
      business.labourCompliance.flatMap(_.supplyWorkers),
      Some(controllers.business.routes.SupplyWorkersController.show.url)
    )

  private def numberOfWorkers(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.numberOfWorkers",
      business.labourCompliance.flatMap(_.numOfWorkersSupplied.map(_.toString)),
      Some(controllers.business.routes.WorkersController.show.url)
    )

  private def intermediaryArrangingSupplyOfWorkers(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.intermediarySupply",
      business.labourCompliance.flatMap(_.intermediaryArrangement),
      Some(controllers.business.routes.SupplyWorkersIntermediaryController.show.url)
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
        Some(controllers.business.routes.TradingNameController.show.url)
      } else {
        Some(controllers.business.routes.MandatoryTradingNameController.show.url)
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
        Some(controllers.business.routes.ImportsOrExportsController.show.url)
      )
    }

  private def applyForEori(tradingDetails: Option[TradingDetails], partyType: PartyType)(implicit messages: Messages): Option[SummaryListRow] =
    if (partyType == NETP || partyType == NonUkNonEstablished) {
      None
    } else {
      optSummaryListRowBoolean(
        s"$sectionId.applyForEori",
        tradingDetails.flatMap(_.euGoods),
        Some(controllers.business.routes.ApplyForEoriController.show.url)
      )
    }

  private def turnoverEstimate(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.turnoverEstimate",
      returns.turnoverEstimate.map(Formatters.currency),
      Some(returnsRoutes.TurnoverEstimateController.show.url)
    )

  private def zeroRatedTurnover(vatScheme: VatScheme)(implicit messages: Messages): Option[SummaryListRow] =
    if (vatScheme.returns.flatMap(_.turnoverEstimate).contains(0)) None else optSummaryListRowString(
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

  private def vatExemption(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.vatExemption",
      returns.appliedForExemption,
      Some(returnsRoutes.VatExemptionController.show.url)
    )

  private def sendGoodsOverseas(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] = {
    if (returns.overseasCompliance.exists(_.goodsToOverseas.contains(true))) {
      optSummaryListRowBoolean(
        s"$sectionId.sendGoodsOverseas",
        returns.overseasCompliance.flatMap(_.goodsToOverseas),
        Some(returnsRoutes.SendGoodsOverseasController.show.url)
      )
    } else {
      None
    }
  }

  private def sendGoodsToEu(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] = {
    if (returns.overseasCompliance.exists(_.goodsToEu.contains(true))) {
      optSummaryListRowBoolean(
        s"$sectionId.sendGoodsToEu",
        returns.overseasCompliance.flatMap(_.goodsToEu),
        Some(returnsRoutes.SendEUGoodsController.show.url)
      )
    } else {
      None
    }
  }

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

  private def complianceSection(business: Business)(implicit messages: Messages): List[SummaryListRow] =
    if (business.labourCompliance.exists(_.supplyWorkers.contains(true))) {
      List(
        numberOfWorkers(business),
        intermediaryArrangingSupplyOfWorkers(business)
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
    else {
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
