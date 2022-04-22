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

import connectors.ConfigConnector
import controllers.returns.{routes => returnsRoutes}
import featureswitch.core.config.FeatureSwitching
import models._
import models.api.returns._
import models.api.{NETP, NonUkNonEstablished, PartyType, VatScheme}
import models.view.SummaryListRowUtils.{optSummaryListRowBoolean, optSummaryListRowSeq, optSummaryListRowString}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import services.FlatRateService
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.InternalServerException

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

// scalastyle:off
@Singleton
class RegistrationDetailsSummaryBuilder @Inject()(configConnector: ConfigConnector,
                                                  flatRateService: FlatRateService,
                                                  govukSummaryList: GovukSummaryList) extends FeatureSwitching {

  val presentationFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM y")
  val sectionId = "cya.registrationDetails"

  def build(vatScheme: VatScheme)(implicit messages: Messages): HtmlFormat.Appendable = {
    val partyType = vatScheme.eligibilitySubmissionData.map(_.partyType).getOrElse(throw new InternalServerException("Eligibility"))
    val returns = vatScheme.returns.getOrElse(throw new InternalServerException("[RegistrationDetailsBuilder] Returns"))

    govukSummaryList(SummaryList(
      List(
        startDate(returns),
        accountingPeriod(returns),
        lastMonthOfAccountingYear(returns),
        paymentFrequency(returns),
        paymentMethod(returns)
      ).flatten ++
        bankAccountSection(vatScheme, partyType) ++
        flatRateSchemeSection(vatScheme, partyType)
    ))
  }

  private def startDate(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.startDate",
      returns.startDate match {
        case Some(date) => Some(date.format(presentationFormatter))
        case _ => None
      },
      Some(controllers.returns.routes.VatRegStartDateResolverController.resolve.url)
    )

  private def accountingPeriod(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.accountingPeriod",
      (returns.returnsFrequency, returns.staggerStart) match {
        case (Some(Monthly), _) =>
          Some(s"$sectionId.accountingPeriod.monthly")
        case (Some(Quarterly), Some(period)) =>
          Some(s"$sectionId.accountingPeriod.${period.toString.substring(0, 3).toLowerCase()}")
        case (Some(Annual), _) =>
          Some(s"$sectionId.accountingPeriod.annual")
        case _ => throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Invalid accounting period")
      },
      Some(returnsRoutes.ReturnsController.accountPeriodsPage.url)
    )

  private def lastMonthOfAccountingYear(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.lastMonthOfAccountingYear",
      returns.staggerStart match {
        case Some(period: AnnualStagger) => Some(s"$sectionId.lastMonthOfAccountingYear.${period.toString}")
        case _ => None
      },
      Some(returnsRoutes.LastMonthOfAccountingYearController.show.url)
    )

  private def paymentFrequency(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.paymentFrequency",
      returns.annualAccountingDetails.flatMap(_.paymentFrequency).map { paymentFrequency =>
        s"$sectionId.paymentFrequency.${paymentFrequency.toString}"
      },
      Some(returnsRoutes.PaymentFrequencyController.show.url)
    )

  private def paymentMethod(returns: Returns)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.paymentMethod",
      returns.annualAccountingDetails.flatMap(_.paymentMethod).map { paymentMethod =>
        s"$sectionId.paymentMethod.${paymentMethod.toString}"
      },
      Some(returnsRoutes.PaymentMethodController.show.url)
    )

  private def bankAccountSection(vatScheme: VatScheme, partyType: PartyType)(implicit messages: Messages): List[SummaryListRow] = {
    val bankAccount: Option[BankAccount] = vatScheme.bankAccount

    val accountIsProvidedRow = optSummaryListRowBoolean(
      s"$sectionId.companyBankAccount",
      bankAccount.map(_.isProvided),
      Some(controllers.bankdetails.routes.HasBankAccountController.show.url)
    )

    val companyBankAccountDetails = optSummaryListRowSeq(
      s"$sectionId.companyBankAccount.details",
      partyType match {
        case NETP | NonUkNonEstablished => bankAccount.flatMap(_.overseasDetails.map(OverseasBankDetails.overseasBankSeq))
        case _ => bankAccount.flatMap(_.details.map(BankAccountDetails.bankSeq))
      },
      partyType match {
        case NETP | NonUkNonEstablished => Some(controllers.bankdetails.routes.OverseasBankAccountController.show.url)
        case _ => Some(controllers.bankdetails.routes.UkBankAccountDetailsController.show.url)
      }
    )

    val noUKBankAccount = optSummaryListRowString(
      s"$sectionId.companyBankAccount.reason",
      bankAccount.flatMap(_.reason).map {
        case BeingSetup => "pages.noUKBankAccount.reason.beingSetup"
        case OverseasAccount => "pages.noUKBankAccount.reason.overseasAccount"
        case NameChange => "pages.noUKBankAccount.reason.nameChange"
      },
      Some(controllers.bankdetails.routes.NoUKBankAccountController.show.url)
    )

    List(
      accountIsProvidedRow,
      companyBankAccountDetails,
      noUKBankAccount
    ).flatten
  }

  private def flatRateSchemeSection(vatScheme: VatScheme, partyType: PartyType)(implicit messages: Messages): List[SummaryListRow] = {

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
      Some(controllers.flatratescheme.routes.FlatRateController.annualCostsInclusivePage.url)
    )

    val estimateTotalSalesRow = optSummaryListRowString(
      s"$sectionId.estimateTotalSales",
      optFlatRateScheme.flatMap(_.estimateTotalSales.map("%,d".format(_))).map(sales => s"£$sales"),
      Some(controllers.flatratescheme.routes.EstimateTotalSalesController.estimateTotalSales.url)
    )

    val costsLimitedRow = optSummaryListRowBoolean(
      s"$sectionId.costsLimited",
      optFlatRateScheme.flatMap(_.overBusinessGoodsPercent),
      Some(controllers.flatratescheme.routes.FlatRateController.annualCostsLimitedPage.url),
      Seq(optFlatRateScheme.flatMap(_.estimateTotalSales.map(v => flatRateService.applyPercentRoundUp(v))).map("%,d".format(_)).getOrElse("0"))
    )

    val flatRatePercentageRow = optSummaryListRowBoolean(
      s"$sectionId.flatRate",
      optFlatRateScheme.flatMap(_.useThisRate),
      Some(
        if (isLimitedCostTrader) controllers.flatratescheme.routes.FlatRateController.registerForFrsPage.url
        else controllers.flatratescheme.routes.FlatRateController.yourFlatRatePage.url
      ),
      Seq(
        if (isLimitedCostTrader) FlatRateService.defaultFlatRate.toString
        else optFlatRateScheme.flatMap(_.percent).getOrElse(0.0).toString
      )
    )

    val businessSectorRow = optSummaryListRowString(
      s"$sectionId.businessSector",
      optFlatRateScheme.flatMap(_.categoryOfBusiness.filter(_.nonEmpty).map(frsId => configConnector.getBusinessTypeDetails(frsId)._1)),
      Some(controllers.flatratescheme.routes.ChooseBusinessTypeController.show.url)
    )

    val frsStartDate = optSummaryListRowString(
      s"$sectionId.frsStartDate",
      (vatScheme.returns.flatMap(_.startDate), optFlatRateScheme.flatMap(_.frsStart.flatMap(_.date))) match {
        case (Some(startDate), Some(date)) if startDate.isEqual(date) => Some(s"$sectionId.dateOfRegistration")
        case (_, Some(date)) => Some(date.format(presentationFormatter))
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