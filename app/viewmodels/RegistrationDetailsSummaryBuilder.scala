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
import connectors.ConfigConnector
import controllers.vatapplication.{routes => vatApplicationRoutes}
import models._
import models.api.{NETP, NonUkNonEstablished, PartyType, VatScheme}
import models.api.vatapplication._
import models.view.SummaryListRowUtils.{optSummaryListRowBoolean, optSummaryListRowSeq, optSummaryListRowString}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import services.FlatRateService
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.InternalServerException
import utils.MessageDateFormat

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

// scalastyle:off
@Singleton
class RegistrationDetailsSummaryBuilder @Inject()(configConnector: ConfigConnector,
                                                  flatRateService: FlatRateService,
                                                  govukSummaryList: GovukSummaryList)
                                                 (implicit appConfig: FrontendAppConfig) {

  val sectionId = "cya.registrationDetails"

  def build(vatScheme: VatScheme)(implicit messages: Messages): HtmlFormat.Appendable = {
    val vatApplication = vatScheme.vatApplication.getOrElse(throw new InternalServerException("[RegistrationDetailsBuilder] Missing Returns"))
    val partyType = vatScheme.eligibilitySubmissionData.map(_.partyType).getOrElse(
      throw new InternalServerException(s"[RegistrationDetailsSummaryBuilder] Couldn't construct CYA due to missing section: 'Eligibility'")
    )

    govukSummaryList(SummaryList(
      List(
        importsOrExports(vatApplication, partyType),
        applyForEori(vatApplication, partyType),
        turnoverEstimate(vatApplication),
        zeroRatedTurnover(vatScheme)
      ).flatten ++
      nipSection(vatApplication) ++
      List(
        claimRefunds(vatApplication),
        vatExemption(vatApplication)
      ).flatten ++
        netpSection(vatApplication, partyType) ++
        bankAccountSection(vatScheme) ++
        List(
            startDate(vatApplication),
            currentlyTrading(vatApplication),
            accountingPeriod(vatApplication),
            lastMonthOfAccountingYear(vatApplication),
            paymentFrequency(vatApplication),
            paymentMethod(vatApplication),
            taxRep(vatApplication)
        ).flatten ++
        flatRateSchemeSection(vatScheme)
    ))
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
    if (vatScheme.vatApplication.flatMap(_.turnoverEstimate).contains(BigDecimal(0))) None else optSummaryListRowString(
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
  private def sellOrMoveNip(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowSeq(
      s"$sectionId.sellOrMoveNip",
      Some(Seq(
        vatApplication.northernIrelandProtocol.flatMap(_.goodsToEU).map(answer =>
          if (answer.answer) messages("app.common.yes")
          else messages("app.common.no")
        ),
        vatApplication.northernIrelandProtocol.flatMap(_.goodsToEU).flatMap(_.value.map { value =>
          s"${messages(s"$sectionId.valueOfGoods")} ${Formatters.currency(value)}"
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
          s"${messages(s"$sectionId.valueOfGoods")} ${Formatters.currency(value)}"
        })
      ).flatten),
      Some(vatApplicationRoutes.ReceiveGoodsNipController.show.url)
    )

  private def netpSection(vatApplication: VatApplication, partyType: PartyType)(implicit messages: Messages): List[SummaryListRow] =
    if (partyType == NETP || partyType == NonUkNonEstablished) {
      List(
        sendGoodsOverseas(vatApplication),
        sendGoodsToEu(vatApplication),
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

  private def startDate(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.startDate",
      vatApplication.startDate match {
        case Some(date) if date.isAfter(LocalDate.now().minusYears(4).minusDays(1)) => Some(MessageDateFormat.format(date))
        case _ => None
      },
      Some(controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url)
    )

  private def accountingPeriod(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.accountingPeriod",
      (vatApplication.returnsFrequency, vatApplication.staggerStart) match {
        case (Some(Monthly), _) =>
          Some(s"$sectionId.accountingPeriod.monthly")
        case (Some(Quarterly), Some(period)) =>
          Some(s"$sectionId.accountingPeriod.${period.toString.substring(0, 3).toLowerCase()}")
        case (Some(Annual), _) =>
          Some(s"$sectionId.accountingPeriod.annual")
        case _ => throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Invalid accounting period")
      },
      Some(vatApplicationRoutes.ReturnsFrequencyController.show.url)
    )

  private def lastMonthOfAccountingYear(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.lastMonthOfAccountingYear",
      vatApplication.staggerStart match {
        case Some(period: AnnualStagger) => Some(s"$sectionId.lastMonthOfAccountingYear.${period.toString}")
        case _ => None
      },
      Some(vatApplicationRoutes.LastMonthOfAccountingYearController.show.url)
    )

  private def paymentFrequency(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.paymentFrequency",
      vatApplication.annualAccountingDetails.flatMap(_.paymentFrequency).map { paymentFrequency =>
        s"$sectionId.paymentFrequency.${paymentFrequency.toString}"
      },
      Some(vatApplicationRoutes.PaymentFrequencyController.show.url)
    )

  private def paymentMethod(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.paymentMethod",
      vatApplication.annualAccountingDetails.flatMap(_.paymentMethod).map { paymentMethod =>
        s"$sectionId.paymentMethod.${paymentMethod.toString}"
      },
      Some(vatApplicationRoutes.PaymentMethodController.show.url)
    )

  private def taxRep(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.taxRep",
      vatApplication.hasTaxRepresentative,
      Some(controllers.vatapplication.routes.TaxRepController.show.url)
    )

  private def currentlyTrading(vatApplication: VatApplication)(implicit messages: Messages): Option[SummaryListRow] = {
    optSummaryListRowBoolean(
      s"$sectionId.currentlyTrading",
      vatApplication.currentlyTrading,
      Some(controllers.vatapplication.routes.CurrentlyTradingController.show.url)
    )
  }

  private def bankAccountSection(vatScheme: VatScheme)(implicit messages: Messages): List[SummaryListRow] = {
    val bankAccount: Option[BankAccount] = vatScheme.bankAccount

    val accountIsProvidedRow = optSummaryListRowBoolean(
      s"$sectionId.companyBankAccount",
      bankAccount.map(_.isProvided),
      Some(controllers.bankdetails.routes.HasBankAccountController.show.url)
    )

    val companyBankAccountDetails = optSummaryListRowSeq(
      s"$sectionId.companyBankAccount.details",
      bankAccount.flatMap(_.details.map(BankAccountDetails.bankSeq)),
      Some(controllers.bankdetails.routes.UkBankAccountDetailsController.show.url)
    )

    val noUKBankAccount = optSummaryListRowString(
      s"$sectionId.companyBankAccount.reason",
      bankAccount.flatMap(_.reason).map {
        case BeingSetupOrNameChange => "pages.noUKBankAccount.beingSetupOrNameChange"
        case OverseasAccount => "pages.noUKBankAccount.overseasAccount"
        case NameChange => "pages.noUKBankAccount.nameChange"
        case AccountNotInBusinessName => "pages.noUKBankAccount.accountNotInBusinessName"
        case DontWantToProvide => "pages.noUKBankAccount.dontWantToProvide"
      },
      Some(controllers.bankdetails.routes.NoUKBankAccountController.show.url)
    )

    List(
      accountIsProvidedRow,
      companyBankAccountDetails,
      noUKBankAccount
    ).flatten
  }

  private def flatRateSchemeSection(vatScheme: VatScheme)(implicit messages: Messages): List[SummaryListRow] = {

    val optFlatRateScheme: Option[FlatRateScheme] = vatScheme.flatRateScheme
    val isLimitedCostTrader: Boolean = optFlatRateScheme.exists(_.limitedCostTrader.contains(true))

    val joinFrsRow = optSummaryListRowBoolean(
      s"$sectionId.joinFrs",
      optFlatRateScheme.flatMap(_.joinFrs),
      Some(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url)
    )

    val costsInclusiveRow = optSummaryListRowBoolean(
      s"$sectionId.costsInclusive",
      optFlatRateScheme.flatMap(_.overBusinessGoods),
      Some(controllers.flatratescheme.routes.AnnualCostsInclusiveController.show.url)
    )

    val estimateTotalSalesRow = optSummaryListRowString(
      s"$sectionId.estimateTotalSales",
      optFlatRateScheme.flatMap(_.estimateTotalSales.map(Formatters.currency)),
      Some(controllers.flatratescheme.routes.EstimateTotalSalesController.show.url)
    )

    val costsLimitedRow = optSummaryListRowBoolean(
      s"$sectionId.costsLimited",
      optFlatRateScheme.flatMap(_.overBusinessGoodsPercent),
      Some(controllers.flatratescheme.routes.AnnualCostsLimitedController.show.url),
      Seq(optFlatRateScheme.flatMap(_.estimateTotalSales.map(v => flatRateService.applyPercentRoundUp(v)).map(Formatters.currencyWithoutDecimal)).getOrElse("0"))
    )

    val flatRatePercentageRow = optSummaryListRowBoolean(
      s"$sectionId.flatRate",
      optFlatRateScheme.flatMap(_.useThisRate),
      Some(
        if (isLimitedCostTrader) controllers.flatratescheme.routes.RegisterForFrsController.show.url
        else controllers.flatratescheme.routes.YourFlatRateController.show.url
      ),
      Seq(
        if (isLimitedCostTrader) FlatRateService.defaultFlatRate.toString
        else optFlatRateScheme.flatMap(_.percent).getOrElse(0.0).toString
      )
    )

    val businessSectorRow = optSummaryListRowString(
      s"$sectionId.businessSector",
      if (isLimitedCostTrader) {
        None
      } else {
        optFlatRateScheme.flatMap(_.categoryOfBusiness.filter(_.nonEmpty).map(frsId => configConnector.getBusinessType(frsId).businessTypeLabel))
      },
      Some(controllers.flatratescheme.routes.ChooseBusinessTypeController.show.url)
    )

    val frsStartDate = optSummaryListRowString(
      s"$sectionId.frsStartDate",
      (vatScheme.vatApplication.flatMap(_.startDate), optFlatRateScheme.flatMap(_.frsStart)) match {
        case (Some(startDate), Some(date)) if startDate.isEqual(date) => Some(s"$sectionId.dateOfRegistration")
        case (_, Some(date)) => Some(MessageDateFormat.format(date))
        case _ => None
      },
      Some(controllers.flatratescheme.routes.StartDateController.show.url)
    )
    List(
      joinFrsRow,
      costsInclusiveRow,
      estimateTotalSalesRow,
      costsLimitedRow,
      flatRatePercentageRow,
      businessSectorRow,
      frsStartDate
    ).flatten
  }
}
