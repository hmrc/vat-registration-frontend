/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.registration.applicant.{routes => applicantRoutes}
import controllers.registration.business.{routes => businessContactRoutes}
import controllers.registration.returns.{routes => returnsRoutes}
import controllers.registration.sicandcompliance.{routes => sicAndCompRoutes}
import featureswitch.core.config.{FeatureSwitching, UseSoleTraderIdentification}
import models._
import models.api.returns._
import models.api.{Address, Individual, VatScheme, _}
import models.external._
import models.view.SummaryListRowUtils._
import play.api.i18n.Messages
import services.FlatRateService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.InternalServerException

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

// scalastyle:off
@Singleton
class SummaryCheckYourAnswersBuilder @Inject()(configConnector: ConfigConnector,
                                               flatRateService: FlatRateService) extends FeatureSwitching {

  val presentationFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM y")
  val sectionId: String = "pages.summary.directorDetails"

  def generateSummaryList(implicit vatScheme: VatScheme, messages: Messages): SummaryList = {
    implicit val partyType: PartyType = vatScheme.eligibilitySubmissionData.map(_.partyType).getOrElse(throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Missing party type"))

    SummaryList(
      applicantDetailsSection ++
        businessContactSection ++
        sicAndComplianceSection ++
        returnsSection ++
        tradingDetailsSection ++
        bankAccountSection ++
        flatRateSchemeSection
    )
  }

  def applicantDetailsSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val applicantDetails = vatScheme.applicantDetails.getOrElse(throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Missing applicant details block"))

    val changeTransactorDetailsUrl: String = {
      partyType match {
        case Individual | NETP | Partnership => applicantRoutes.SoleTraderIdentificationController.startJourney().url
        case _ if isEnabled(UseSoleTraderIdentification) => applicantRoutes.SoleTraderIdentificationController.startJourney().url
        case _ => applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney().url
      }
    }

    val companyNumber = optSummaryListRowString(
      s"$sectionId.companyNumber",
      applicantDetails.entity.collect {
        case incorpIdEntity: IncorporatedEntity => incorpIdEntity.companyNumber
      },
      Some(applicantRoutes.IncorpIdController.startJourney().url)
    )

    val ctutr = optSummaryListRowString(
      s"$sectionId.ctutr",
      applicantDetails.entity.flatMap {
        case incorpIdEntity: IncorporatedEntity => incorpIdEntity.ctutr
        case _ => None
      },
      Some(applicantRoutes.IncorpIdController.startJourney().url)
    )

    val sautr = optSummaryListRowString(
      s"$sectionId.sautr",
      applicantDetails.entity.flatMap {
        case soleTrader: SoleTraderIdEntity => soleTrader.sautr
        case business: BusinessIdEntity => business.sautr
        case partnership: PartnershipIdEntity => partnership.sautr
        case _ => None
      },
      partyType match {
        case Partnership => Some(applicantRoutes.PartnershipIdController.startJourney().url)
        case UnincorpAssoc | Trust => Some(applicantRoutes.BusinessIdController.startJourney().url)
        case _ => Some(applicantRoutes.SoleTraderIdentificationController.startJourney().url)
      }
    )

    val trn = optSummaryListRowString(
      s"$sectionId.trn",
      applicantDetails.entity.flatMap {
        case soleTrader: SoleTraderIdEntity => soleTrader.trn
        case _ => None
      },
      Some(applicantRoutes.SoleTraderIdentificationController.startJourney().url)
    )

    val chrn = optSummaryListRowString(
      s"$sectionId.chrn",
      applicantDetails.entity.flatMap {
        case incorporatedEntity: IncorporatedEntity => incorporatedEntity.chrn
        case businessIdEntity: BusinessIdEntity => businessIdEntity.chrn
        case _ => None
      },
      partyType match {
        case CharitableOrg => Some(applicantRoutes.IncorpIdController.startJourney().url)
        case Trust | UnincorpAssoc => Some(applicantRoutes.BusinessIdController.startJourney().url)
        case _ => None
      }
    )

    //TODO add Partnership lead partner identifiers

    val firstName = optSummaryListRowString(
      s"$sectionId.firstName",
      applicantDetails.transactor.map(_.firstName),
      Some(changeTransactorDetailsUrl)
    )

    val lastName = optSummaryListRowString(
      s"$sectionId.lastName",
      applicantDetails.transactor.map(_.lastName),
      Some(changeTransactorDetailsUrl)
    )

    val nino = optSummaryListRowString(
      s"$sectionId.nino",
      applicantDetails.transactor.flatMap(_.nino),
      Some(changeTransactorDetailsUrl)
    )

    val dob = optSummaryListRowString(
      s"$sectionId.dob",
      applicantDetails.transactor.map(_.dateOfBirth.format(presentationFormatter)),
      Some(changeTransactorDetailsUrl)
    )

    val roleInTheBusiness = optSummaryListRowString(
      s"$sectionId.roleInTheBusiness",
      applicantDetails.roleInTheBusiness.collect {
        case Director => "pages.roleInTheBusiness.radio1"
        case CompanySecretary => "pages.roleInTheBusiness.radio2"
      },
      Some(applicantRoutes.CaptureRoleInTheBusinessController.show().url)
    )

    val formerName = optSummaryListRowString(
      s"$sectionId.formerName",
      applicantDetails.formerName.flatMap(_.formerName) match {
        case None => Some(s"$sectionId.noFormerName")
        case formerName => formerName
      },
      Some(applicantRoutes.FormerNameController.show().url)
    )

    val formerNameDate = optSummaryListRowString(
      s"$sectionId.formerNameDate",
      applicantDetails.formerNameDate.map(_.date.format(presentationFormatter)),
      Some(applicantRoutes.FormerNameController.show().url)
    )

    val email = optSummaryListRowString(
      s"$sectionId.email",
      applicantDetails.emailAddress.map(_.email),
      Some(applicantRoutes.CaptureEmailAddressController.show().url)
    )

    val telephone = optSummaryListRowString(
      s"$sectionId.telephone",
      applicantDetails.telephoneNumber.map(_.telephone),
      Some(applicantRoutes.CaptureTelephoneNumberController.show().url)
    )

    val homeAddress = optSummaryListRowSeq(
      s"$sectionId.homeAddress",
      applicantDetails.homeAddress.flatMap(_.address).map(Address.normalisedSeq),
      Some(applicantRoutes.HomeAddressController.redirectToAlf().url)
    )

    val moreThanThreeYears = optSummaryListRowBoolean(
      s"$sectionId.moreThanThreeYears",
      applicantDetails.previousAddress.map(_.yesNo),
      Some(applicantRoutes.PreviousAddressController.show().url)
    )

    val previousAddress = optSummaryListRowSeq(
      s"$sectionId.previousAddress",
      applicantDetails.previousAddress.flatMap(_.address).map(Address.normalisedSeq),
      Some(applicantRoutes.PreviousAddressController.show().url)
    )

    Seq(
      companyNumber,
      ctutr,
      sautr,
      trn,
      chrn,
      firstName,
      lastName,
      nino,
      dob,
      roleInTheBusiness,
      formerName,
      formerNameDate,
      email,
      telephone,
      homeAddress,
      moreThanThreeYears,
      previousAddress
    ).flatten
  }

  def businessContactSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val businessContact: BusinessContact = vatScheme.businessContact.getOrElse(throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Missing business contact block"))

    val businessEmailRow = optSummaryListRowString(
      s"$sectionId.emailBusiness",
      businessContact.companyContactDetails.map(_.email),
      Some(businessContactRoutes.BusinessContactDetailsController.show().url)
    )

    val businessDaytimePhoneNumberRow = optSummaryListRowString(
      s"$sectionId.daytimePhoneBusiness",
      businessContact.companyContactDetails.flatMap(_.phoneNumber),
      Some(businessContactRoutes.BusinessContactDetailsController.show().url)
    )

    val businessMobilePhoneNumberRow = optSummaryListRowString(
      s"$sectionId.mobileBusiness",
      businessContact.companyContactDetails.flatMap(_.mobileNumber),
      Some(businessContactRoutes.BusinessContactDetailsController.show().url)
    )

    val businessWebsiteRow = optSummaryListRowString(
      s"$sectionId.website",
      businessContact.companyContactDetails.flatMap(_.websiteAddress),
      Some(businessContactRoutes.BusinessContactDetailsController.show().url)
    )

    val ppobRow = optSummaryListRowSeq(
      s"$sectionId.ppob",
      businessContact.ppobAddress.map(Address.normalisedSeq),
      Some(businessContactRoutes.PpobAddressController.startJourney().url)
    )

    val contactPreferenceRow = optSummaryListRowString(
      s"$sectionId.contactPreference",
      businessContact.contactPreference.map(_.toString),
      Some(controllers.routes.ContactPreferenceController.showContactPreference().url)
    )

    Seq(
      businessEmailRow,
      businessDaytimePhoneNumberRow,
      businessMobilePhoneNumberRow,
      businessWebsiteRow,
      ppobRow,
      contactPreferenceRow
    ).flatten
  }

  def sicAndComplianceSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val sicAndCompliance: SicAndCompliance = vatScheme.sicAndCompliance.getOrElse(throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Missing sic and compliance block"))

    val companyBusinessDescriptionRow = optSummaryListRowString(
      s"$sectionId.businessDescription",
      sicAndCompliance.description.map(_.description),
      Some(sicAndCompRoutes.BusinessActivityDescriptionController.show().url)
    )

    val mainActivityRow = optSummaryListRowString(
      s"$sectionId.mainSicCode",
      sicAndCompliance.mainBusinessActivity.flatMap(_.mainBusinessActivity.map(_.description)),
      Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity().url)
    )

    val sicCodesRow = optSummaryListRowSeq(
      s"$sectionId.sicCodes",
      sicAndCompliance.businessActivities.map(_.sicCodes.map(_.description)),
      Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity().url)
    )

    val confirmIndustryClassificationCodesRow = optSummaryListRowSeq(
      s"$sectionId.businessActivities",
      sicAndCompliance.businessActivities.map(codes => codes.sicCodes.map(
        sicCode => sicCode.code + " - " + sicCode.description
      )),
      Some(controllers.routes.SicAndComplianceController.returnToICL().url)
    )

    val providingWorkersRow = optSummaryListRowBoolean(
      s"$sectionId.supplyWorkers",
      sicAndCompliance.supplyWorkers.map(_.yesNo),
      Some(sicAndCompRoutes.SupplyWorkersController.show().url)
    )

    val numberOfWorkersRow = optSummaryListRowString(
      s"$sectionId.numberOfWorkers",
      sicAndCompliance.workers.map(_.numberOfWorkers.toString),
      Some(sicAndCompRoutes.WorkersController.show().url)
    )

    val intermediarySupplyRow = optSummaryListRowBoolean(
      s"$sectionId.intermediarySupply",
      sicAndCompliance.intermediarySupply.map(_.yesNo),
      Some(sicAndCompRoutes.SupplyWorkersIntermediaryController.show().url)
    )

    Seq(
      companyBusinessDescriptionRow,
      mainActivityRow,
      sicCodesRow,
      confirmIndustryClassificationCodesRow,
      providingWorkersRow,
      numberOfWorkersRow,
      intermediarySupplyRow
    ).flatten
  }

  def returnsSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val returns: Returns = vatScheme.returns.getOrElse(throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Missing returns block"))
    val mandatoryRegistration: Boolean = vatScheme.eligibilitySubmissionData.map(_.threshold).exists(_.mandatoryRegistration)

    val startDateRow = optSummaryListRowString(
      s"$sectionId.startDate",
      returns.startDate match {
        case Some(date) => Some(date.format(presentationFormatter))
        case None if partyType.equals(NETP) => None
        case None => Some(s"$sectionId.mandatoryStartDate")
      },
      if (mandatoryRegistration) Some(returnsRoutes.ReturnsController.mandatoryStartPage().url)
      else Some(returnsRoutes.ReturnsController.voluntaryStartPage().url)
    )

    val zeroRatedRow = optSummaryListRowString(
      s"$sectionId.zeroRated",
      returns.zeroRatedSupplies.map(Formatters.currency),
      Some(returnsRoutes.ZeroRatedSuppliesController.show().url)
    )

    val expectClaimRefundsRow = optSummaryListRowBoolean(
      s"$sectionId.claimRefunds",
      returns.reclaimVatOnMostReturns,
      Some(returnsRoutes.ClaimRefundsController.show().url)
    )

    val accountingPeriodRow = optSummaryListRowString(
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
      Some(returnsRoutes.ReturnsController.accountPeriodsPage().url)
    )

    val lastMonthOfAccountingYearRow = optSummaryListRowString(
      s"$sectionId.lastMonthOfAccountingYear",
      returns.staggerStart match {
        case Some(period: AnnualStagger) => Some(s"$sectionId.lastMonthOfAccountingYear.${period.toString}")
        case _ => None
      },
      Some(returnsRoutes.LastMonthOfAccountingYearController.show().url)
    )

    val paymentFrequencyRow = optSummaryListRowString(
      s"$sectionId.paymentFrequency",
      returns.annualAccountingDetails.flatMap(_.paymentFrequency).map { paymentFrequency =>
        s"$sectionId.paymentFrequency.${paymentFrequency.toString}"
      },
      Some(returnsRoutes.PaymentFrequencyController.show().url)
    )

    val paymentMethodRow = optSummaryListRowString(
      s"$sectionId.paymentMethod",
      returns.annualAccountingDetails.flatMap(_.paymentMethod).map { paymentMethod =>
        s"$sectionId.paymentMethod.${paymentMethod.toString}"
      },
      Some(returnsRoutes.PaymentMethodController.show().url)
    )

    val sendGoodsOverseas = optSummaryListRowBoolean(
      s"$sectionId.sendGoodsOverseas",
      returns.overseasCompliance.flatMap(_.goodsToOverseas),
      Some(returnsRoutes.SendGoodsOverseasController.show().url)
    )

    val sendGoodsToEu = optSummaryListRowBoolean(
      s"$sectionId.sendGoodsToEu",
      returns.overseasCompliance.flatMap(_.goodsToEu),
      Some(returnsRoutes.SendEUGoodsController.show().url)
    )

    val storingGoods = optSummaryListRowString(
      s"$sectionId.storingGoods",
      returns.overseasCompliance.flatMap(_.storingGoodsForDispatch).map{
        case StoringWithinUk => s"$sectionId.storingGoods.uk"
        case StoringOverseas => s"$sectionId.storingGoods.overseas"
      },
      Some(returnsRoutes.StoringGoodsController.show().url)
    )

    val dispatchFromWarehouse = optSummaryListRowBoolean(
      s"$sectionId.dispatchFromWarehouse",
      returns.overseasCompliance.flatMap(_.usingWarehouse),
      Some(returnsRoutes.DispatchFromWarehouseController.show().url)
    )

    val warehouseNumber = optSummaryListRowString(
      s"$sectionId.warehouseNumber",
      returns.overseasCompliance.flatMap(_.fulfilmentWarehouseNumber),
      Some(returnsRoutes.WarehouseNumberController.show().url)
    )

    val warehouseName = optSummaryListRowString(
      s"$sectionId.warehouseName",
      returns.overseasCompliance.flatMap(_.fulfilmentWarehouseName),
      Some(returnsRoutes.WarehouseNameController.show().url)
    )

    Seq(
      startDateRow,
      zeroRatedRow,
      expectClaimRefundsRow,
      accountingPeriodRow,
      lastMonthOfAccountingYearRow,
      paymentFrequencyRow,
      paymentMethodRow,
      sendGoodsOverseas,
      sendGoodsToEu,
      storingGoods,
      dispatchFromWarehouse,
      warehouseNumber,
      warehouseName
    ).flatten
  }

  def tradingDetailsSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val tradingDetails: TradingDetails = vatScheme.tradingDetails.getOrElse(throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Missing trading details block"))
    val tradingNameOptional: Boolean = Seq(UkCompany, RegSociety, CharitableOrg).contains(partyType)

    val tradingNameRow = optSummaryListRowString(
      if (tradingNameOptional) {
        s"$sectionId.tradingName"
      } else {
        s"$sectionId.mandatoryName"
      },
      tradingDetails.tradingNameView.flatMap(_.tradingName) match {
        case None => Some("app.common.no")
        case optTradingName => optTradingName
      },
      if (tradingNameOptional) {
        Some(controllers.registration.business.routes.TradingNameController.show().url)
      } else {
        Some(controllers.registration.business.routes.MandatoryTradingNameController.show().url)
      }
    )

    val applyForEoriRow = optSummaryListRowBoolean(
      s"$sectionId.applyForEori",
      tradingDetails.euGoods,
      Some(controllers.registration.business.routes.ApplyForEoriController.show().url)
    )

    Seq(
      tradingNameRow,
      applyForEoriRow
    ).flatten
  }

  def bankAccountSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val bankAccount: Option[BankAccount] = vatScheme.bankAccount

    val accountIsProvidedRow = optSummaryListRowBoolean(
      s"$sectionId.companyBankAccount",
      bankAccount.map(_.isProvided),
      Some(controllers.registration.bankdetails.routes.HasBankAccountController.show().url)
    )

    val companyBankAccountDetails = optSummaryListRowSeq(
      s"$sectionId.companyBankAccount.details",
      partyType match {
        case NETP => bankAccount.flatMap(_.overseasDetails.map(OverseasBankDetails.overseasBankSeq))
        case _ => bankAccount.flatMap(_.details.map(BankAccountDetails.bankSeq))
      },
      partyType match {
        case NETP => Some(controllers.registration.bankdetails.routes.OverseasBankAccountController.show().url)
        case _ => Some(controllers.registration.bankdetails.routes.UkBankAccountDetailsController.show().url)
      }
    )

    val noUKBankAccount = optSummaryListRowString(
      s"$sectionId.companyBankAccount.reason",
      bankAccount.flatMap(_.reason).map {
        case BeingSetup => "pages.noUKBankAccount.reason.beingSetup"
        case OverseasAccount => "pages.noUKBankAccount.reason.overseasAccount"
        case NameChange => "pages.noUKBankAccount.reason.nameChange"
      },
      Some(controllers.registration.bankdetails.routes.NoUKBankAccountController.show().url)
    )

    Seq(
      accountIsProvidedRow,
      companyBankAccountDetails,
      noUKBankAccount
    ).flatten
  }

  def flatRateSchemeSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val optFlatRateScheme: Option[FlatRateScheme] = vatScheme.flatRateScheme
    val isLimitedCostTrader: Boolean = optFlatRateScheme.exists(_.limitedCostTrader.contains(true))

    val joinFrsRow = optSummaryListRowBoolean(
      s"$sectionId.joinFrs",
      optFlatRateScheme.flatMap(_.joinFrs),
      Some(controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show().url)
    )

    val costsInclusiveRow = optSummaryListRowBoolean(
      s"$sectionId.costsInclusive",
      optFlatRateScheme.flatMap(_.overBusinessGoods),
      Some(controllers.routes.FlatRateController.annualCostsInclusivePage().url)
    )

    val estimateTotalSalesRow = optSummaryListRowString(
      s"$sectionId.estimateTotalSales",
      optFlatRateScheme.flatMap(_.estimateTotalSales.map("%,d".format(_))).map(sales => s"£$sales"),
      Some(controllers.registration.flatratescheme.routes.EstimateTotalSalesController.estimateTotalSales().url)
    )

    val costsLimitedRow = optSummaryListRowBoolean(
      s"$sectionId.costsLimited",
      optFlatRateScheme.flatMap(_.overBusinessGoodsPercent),
      Some(controllers.routes.FlatRateController.annualCostsLimitedPage().url),
      Seq(optFlatRateScheme.flatMap(_.estimateTotalSales.map(v => flatRateService.applyPercentRoundUp(v))).map("%,d".format(_)).getOrElse("0"))
    )

    val flatRatePercentageRow = optSummaryListRowBoolean(
      s"$sectionId.flatRate",
      optFlatRateScheme.flatMap(_.useThisRate),
      Some(
        if (isLimitedCostTrader) controllers.routes.FlatRateController.registerForFrsPage().url
        else controllers.routes.FlatRateController.yourFlatRatePage().url
      ),
      Seq(
        if (isLimitedCostTrader) FlatRateService.defaultFlatRate.toString
        else optFlatRateScheme.flatMap(_.percent).getOrElse(0.0).toString
      )
    )

    val businessSectorRow = optSummaryListRowString(
      s"$sectionId.businessSector",
      optFlatRateScheme.flatMap(_.categoryOfBusiness.filter(_.nonEmpty).map(frsId => configConnector.getBusinessTypeDetails(frsId)._1)),
      Some(controllers.registration.flatratescheme.routes.ChooseBusinessTypeController.show().url)
    )

    Seq(
      joinFrsRow,
      costsInclusiveRow,
      estimateTotalSalesRow,
      costsLimitedRow,
      flatRatePercentageRow,
      businessSectorRow
    ).flatten
  }
}
