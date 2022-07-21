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

import controllers.vatapplication.{routes => vatApplicationRoutes}
import featureswitch.core.config.FeatureSwitching
import models.Business
import models.api._
import models.api.vatapplication.{StoringOverseas, StoringWithinUk, VatApplication}
import models.view.SummaryListRowUtils._
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
    val vatApplication = vatScheme.vatApplication.getOrElse(throw missingSection("Vat Application"))
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
            tradingName(business, partyType),
            importsOrExports(vatApplication, partyType),
            applyForEori(vatApplication, partyType),
            turnoverEstimate(vatApplication),
            zeroRatedTurnover(vatScheme)
          ).flatten ++
          nipSection(vatApplication) ++
          List(
            claimRefunds(vatApplication),
            vatExemption(vatApplication),
            sendGoodsOverseas(vatApplication),
            sendGoodsToEu(vatApplication)
          ).flatten ++
          netpSection(vatApplication, partyType)
      ))
    ))
  }

  private def ppobAddress(business: Business, partyType: PartyType)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowSeq(
      s"$sectionId.homeAddress",
      business.ppobAddress.map(Address.normalisedSeq),
      partyType match {
        case NETP | NonUkNonEstablished =>
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

  private def tradingName(business: Business, partyType: PartyType)(implicit messages: Messages): Option[SummaryListRow] = {
    val tradingNameOptional = Business.tradingNameOptional(partyType)

    optSummaryListRowString(
      if (tradingNameOptional) {
        s"$sectionId.tradingName"
      } else {
        s"$sectionId.mandatoryName"
      },
      business.tradingName match {
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

  private def importsOrExports(vatApplication: VatApplication, partyType: PartyType)(implicit messages: Messages): Option[SummaryListRow] =
    if (partyType == NETP || partyType == NonUkNonEstablished) {
      None
    } else {
      optSummaryListRowBoolean(
        s"$sectionId.importsOrExports",
        vatApplication.tradeVatGoodsOutsideUk,
        Some(controllers.vatapplication.routes.ImportsOrExportsController.show.url)
      )
    }

  private def applyForEori(vatApplication: VatApplication, partyType: PartyType)(implicit messages: Messages): Option[SummaryListRow] =
    if (partyType == NETP || partyType == NonUkNonEstablished) {
      None
    } else {
      optSummaryListRowBoolean(
        s"$sectionId.applyForEori",
        vatApplication.eoriRequested,
        Some(controllers.vatapplication.routes.ApplyForEoriController.show.url)
      )
    }

  private def turnoverEstimate(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.turnoverEstimate",
      vatApplication.turnoverEstimate.map(Formatters.currency),
      Some(vatApplicationRoutes.TurnoverEstimateController.show.url)
    )

  private def zeroRatedTurnover(vatScheme: VatScheme)(implicit messages: Messages): Option[SummaryListRow] =
    if (vatScheme.vatApplication.flatMap(_.turnoverEstimate).contains(0)) None else optSummaryListRowString(
      s"$sectionId.zeroRated",
      vatScheme.vatApplication.flatMap(_.zeroRatedSupplies.map(Formatters.currency)),
      Some(vatApplicationRoutes.ZeroRatedSuppliesController.show.url)
    )

  private def claimRefunds(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.claimRefunds",
      vatApplication.claimVatRefunds,
      Some(vatApplicationRoutes.ClaimRefundsController.show.url)
    )

  private def vatExemption(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.vatExemption",
      vatApplication.appliedForExemption,
      Some(vatApplicationRoutes.VatExemptionController.show.url)
    )

  private def sendGoodsOverseas(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] = {
    if (vatApplication.overseasCompliance.exists(_.goodsToOverseas.contains(true))) {
      optSummaryListRowBoolean(
        s"$sectionId.sendGoodsOverseas",
        vatApplication.overseasCompliance.flatMap(_.goodsToOverseas),
        Some(vatApplicationRoutes.SendGoodsOverseasController.show.url)
      )
    } else {
      None
    }
  }

  private def sendGoodsToEu(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] = {
    if (vatApplication.overseasCompliance.exists(_.goodsToEu.contains(true))) {
      optSummaryListRowBoolean(
        s"$sectionId.sendGoodsToEu",
        vatApplication.overseasCompliance.flatMap(_.goodsToEu),
        Some(vatApplicationRoutes.SendEUGoodsController.show.url)
      )
    } else {
      None
    }
  }

  private def storingGoods(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.storingGoods",
      vatApplication.overseasCompliance.flatMap(_.storingGoodsForDispatch).map {
        case StoringWithinUk => s"$sectionId.storingGoods.uk"
        case StoringOverseas => s"$sectionId.storingGoods.overseas"
      },
      Some(vatApplicationRoutes.StoringGoodsController.show.url)
    )

  private def dispatchFromWarehouse(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.dispatchFromWarehouse",
      vatApplication.overseasCompliance.flatMap(_.usingWarehouse),
      Some(vatApplicationRoutes.DispatchFromWarehouseController.show.url)
    )

  private def warehouseNumber(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.warehouseNumber",
      vatApplication.overseasCompliance.flatMap(_.fulfilmentWarehouseNumber),
      Some(vatApplicationRoutes.WarehouseNumberController.show.url)
    )

  private def warehouseName(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.warehouseName",
      vatApplication.overseasCompliance.flatMap(_.fulfilmentWarehouseName),
      Some(vatApplicationRoutes.WarehouseNameController.show.url)
    )

  private def sellOrMoveNip(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowSeq(
      s"$sectionId.sellOrMoveNip",
      Some(Seq(
        vatApplication.northernIrelandProtocol.flatMap(_.goodsToEU).map(answer =>
          if (answer.answer) messages("app.common.yes")
          else messages("app.common.no")
        ),
        vatApplication.northernIrelandProtocol.flatMap(_.goodsToEU).flatMap(_.value.map { value =>
          s"${messages("cya.aboutTheBusiness.valueOfGoods")} ${Formatters.currency(value)}"
        })
      ).flatten),
      Some(vatApplicationRoutes.SellOrMoveNipController.show.url)
    )

  private def receiveGoodsNip(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowSeq(
      s"$sectionId.receiveGoodsNip",
      Some(Seq(
        vatApplication.northernIrelandProtocol.flatMap(_.goodsFromEU).map(answer =>
          if (answer.answer) messages("app.common.yes")
          else messages("app.common.no")
        ),
        vatApplication.northernIrelandProtocol.flatMap(_.goodsFromEU).flatMap(_.value.map { value =>
          s"${messages("cya.aboutTheBusiness.valueOfGoods")} ${Formatters.currency(value)}"
        })
      ).flatten),
      Some(vatApplicationRoutes.ReceiveGoodsNipController.show.url)
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

  private def netpSection(vatApplication: VatApplication, partyType: PartyType)(implicit messages: Messages): List[SummaryListRow] =
    if (partyType == NETP || partyType == NonUkNonEstablished) {
      List(
        storingGoods(vatApplication),
        dispatchFromWarehouse(vatApplication)
      ).flatten ++ {
        if (vatApplication.overseasCompliance.exists(_.usingWarehouse.contains(true))) {
          List(
            warehouseNumber(vatApplication),
            warehouseName(vatApplication)
          ).flatten
        } else {
          Nil
        }
      }
    }
    else {
      Nil
    }

  private def nipSection(vatApplication: VatApplication)(implicit messages: Messages): List[SummaryListRow] = {
    if (vatApplication.northernIrelandProtocol.isDefined) {
      List(
        sellOrMoveNip(vatApplication),
        receiveGoodsNip(vatApplication)
      ).flatten
    } else {
      Nil
    }

  }

}
