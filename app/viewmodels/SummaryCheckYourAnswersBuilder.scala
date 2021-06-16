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

import controllers.registration.applicant.{routes => applicantRoutes}
import featureswitch.core.config.{FeatureSwitching, UseSoleTraderIdentification}
import models._
import models.api.returns._
import models.api.{Address, Individual, Threshold, VatScheme}
import models.external.incorporatedentityid.{LimitedCompany, SoleTrader}
import models.view.{SummaryRow, SummarySection}
import org.apache.commons.lang3.StringUtils
import play.api.mvc.Call
import services.FlatRateService

case class SummaryCheckYourAnswersBuilder(scheme: VatScheme,
                                          vatApplicantDetails: ApplicantDetails,
                                          calculatedOnEstimatedSales: Option[Long],
                                          businessType: Option[String],
                                          turnoverEstimates: Option[TurnoverEstimates],
                                          threshold: Option[Threshold],
                                          returnsBlock: Option[Returns]) extends SummarySectionBuilder with FeatureSwitching {

  override val sectionId: String = "directorDetails"

  val joinFrsContainsTrue: Boolean = scheme.flatRateScheme.flatMap(_.joinFrs).contains(true)
  val isflatRatePercentYes: Boolean = scheme.flatRateScheme.flatMap(_.useThisRate).contains(true)
  val isBusinessGoodsYes: Boolean = joinFrsContainsTrue && scheme.flatRateScheme.flatMap(_.overBusinessGoods).contains(true)
  val isAAS: Boolean = scheme.returns.flatMap(_.returnsFrequency).contains(Annual)

  val thresholdBlock: Threshold = threshold.getOrElse(throw new IllegalStateException("Missing threshold block to show summary"))
  val voluntaryRegistration: Boolean = !thresholdBlock.mandatoryRegistration
  val isSoleTrader: Boolean = scheme.eligibilitySubmissionData.exists(_.partyType.equals(Individual))
  val isLimitedCostTrader: Boolean = scheme.flatRateScheme.exists(_.limitedCostTrader.contains(true))

  val changeTransactorDetailsUrl: Call = if (isEnabled(UseSoleTraderIdentification)) {
    applicantRoutes.SoleTraderIdentificationController.startJourney()
  }
  else {
    applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney()
  }

  def startDateRow: SummaryRow = SummaryRow(
    s"$sectionId.startDate",
    returnsBlock.flatMap(_.startDate) match {
      case Some(date) => date.format(presentationFormatter)
      case _ => s"pages.summary.$sectionId.mandatoryStartDate"
    },
    if (voluntaryRegistration) Some(controllers.registration.returns.routes.ReturnsController.voluntaryStartPage()) else None
  )

  val tradingNameRow: SummaryRow = SummaryRow(
    s"$sectionId.tradingName",
    scheme.tradingDetails.flatMap(_.tradingNameView).flatMap(_.tradingName).getOrElse("app.common.no"),
    Some(controllers.registration.business.routes.TradingNameController.show())
  )


  val firstName: SummaryRow = SummaryRow(
    s"$sectionId.firstName",
    vatApplicantDetails.transactor.map(_.firstName).getOrElse(""),
    Some(changeTransactorDetailsUrl)
  )

  val lastName: SummaryRow = SummaryRow(
    s"$sectionId.lastName",
    vatApplicantDetails.transactor.map(_.lastName).getOrElse(""),
    Some(changeTransactorDetailsUrl)
  )

  val nino: SummaryRow = SummaryRow(
    s"$sectionId.nino",
    vatApplicantDetails.transactor.map(_.nino).getOrElse(""),
    Some(changeTransactorDetailsUrl)
  )

  val dob: SummaryRow = SummaryRow(
    s"$sectionId.dob",
    vatApplicantDetails.transactor.map(_.dateOfBirth.format(presentationFormatter)).getOrElse(""),
    Some(changeTransactorDetailsUrl)
  )

  val sautr: SummaryRow = SummaryRow(
    s"$sectionId.sautr",
    vatApplicantDetails.entity.collect {
      case soleTrader: SoleTrader => soleTrader.sautr
    }.getOrElse(""),
    Some(changeTransactorDetailsUrl)
  )

  val roleInTheBusiness: SummaryRow = SummaryRow(
    s"$sectionId.roleInTheBusiness",
    vatApplicantDetails.roleInTheBusiness.map {
      case Director => "pages.roleInTheBusiness.radio1"
      case CompanySecretary => "pages.roleInTheBusiness.radio2"
    }.getOrElse(""),
    Some(applicantRoutes.CaptureRoleInTheBusinessController.show())
  )

  val formerName: SummaryRow = SummaryRow(
    s"$sectionId.formerName",
    vatApplicantDetails.formerName.flatMap(_.formerName).getOrElse(s"pages.summary.$sectionId.noFormerName"),
    Some(applicantRoutes.FormerNameController.show())
  )

  val formerNameDate: SummaryRow = SummaryRow(
    s"$sectionId.formerNameDate",
    vatApplicantDetails.formerNameDate.map(_.date.format(presentationFormatter)).getOrElse(""),
    Some(applicantRoutes.FormerNameController.show())
  )

  val email: SummaryRow = SummaryRow(
    s"$sectionId.email",
    vatApplicantDetails.emailAddress.map(_.email).getOrElse(""),
    Some(applicantRoutes.CaptureEmailAddressController.show())
  )

  val telephone: SummaryRow = SummaryRow(
    s"$sectionId.telephone",
    vatApplicantDetails.telephoneNumber.map(_.telephone).getOrElse(""),
    Some(applicantRoutes.CaptureTelephoneNumberController.show())
  )

  val homeAddress: SummaryRow = SummaryRow(
    s"$sectionId.homeAddress",
    vatApplicantDetails.homeAddress.flatMap(_.address).map(Address.normalisedSeq).getOrElse(Seq.empty),
    Some(applicantRoutes.HomeAddressController.redirectToAlf())
  )

  val moreThanThreeYears: SummaryRow = yesNoRow(
    "moreThanThreeYears",
    vatApplicantDetails.previousAddress.map(_.yesNo),
    applicantRoutes.PreviousAddressController.show()
  )

  val previousAddress: SummaryRow = SummaryRow(
    s"$sectionId.previousAddress",
    vatApplicantDetails.previousAddress.flatMap(_.address).map(Address.normalisedSeq).getOrElse(Seq.empty),
    Some(applicantRoutes.PreviousAddressController.show())
  )

  val zeroRatedRow: SummaryRow = SummaryRow.mandatory(
    s"$sectionId.zeroRated",
    scheme.returns.flatMap(_.zeroRatedSupplies.map(Formatters.currency)),
    Some(controllers.registration.returns.routes.ZeroRatedSuppliesController.show())
  )

  val expectClaimRefundsRow: SummaryRow = yesNoRow(
    "claimRefunds",
    scheme.returns.flatMap(_.reclaimVatOnMostReturns),
    controllers.registration.returns.routes.ClaimRefundsController.show()
  )

  val accountingPeriodRow: SummaryRow = SummaryRow(
    s"$sectionId.accountingPeriod",
    (scheme.returns.flatMap(_.returnsFrequency), scheme.returns.flatMap(_.staggerStart)) match {
      case (Some(Monthly), _) => s"pages.summary.$sectionId.accountingPeriod.monthly"
      case (Some(Quarterly), Some(period)) =>
        s"pages.summary.$sectionId.accountingPeriod.${period.toString.substring(0, 3).toLowerCase()}"
      case (Some(Annual), _) =>
        s"pages.summary.$sectionId.accountingPeriod.annual"
      case _ => ""
    },
    Some(controllers.registration.returns.routes.ReturnsController.accountPeriodsPage())
  )

  val lastMonthOfAccountingYearRow: SummaryRow = SummaryRow(
    s"$sectionId.lastMonthOfAccountingYear",
    scheme.returns.flatMap(_.staggerStart) match {
      case Some(period: AnnualStagger) => s"pages.summary.$sectionId.lastMonthOfAccountingYear.${period.toString}"
      case _ => ""
    },
    Some(controllers.registration.returns.routes.LastMonthOfAccountingYearController.show())
  )

  val paymentFrequencyRow: SummaryRow = SummaryRow(
    s"$sectionId.paymentFrequency",
    scheme.returns.flatMap(_.annualAccountingDetails.map(_.paymentFrequency)) match {
      case Some(Some(paymentFrequency)) => s"pages.summary.$sectionId.paymentFrequency.${paymentFrequency.toString}"
      case _ => ""
    },
    Some(controllers.registration.returns.routes.PaymentFrequencyController.show())
  )

  val paymentMethodRow: SummaryRow = SummaryRow(
    s"$sectionId.paymentMethod",
    scheme.returns.flatMap(_.annualAccountingDetails.map(_.paymentMethod)) match {
      case Some(Some(paymentMethod)) => s"pages.summary.$sectionId.paymentMethod.${paymentMethod.toString}"
      case _ => ""
    },
    Some(controllers.registration.returns.routes.PaymentMethodController.show())
  )

  val sicAndComp: SicAndCompliance = scheme.sicAndCompliance.fold(SicAndCompliance())(a => a)

  val companyBusinessDescriptionRow: SummaryRow = SummaryRow(
    s"$sectionId.businessDescription",
    sicAndComp.description.fold("app.common.no")(desc =>
      if (desc.description.isEmpty) "app.common.no" else desc.description),
    Some(controllers.registration.sicandcompliance.routes.BusinessActivityDescriptionController.show())
  )

  val mainActivityRow: SummaryRow = SummaryRow.mandatory(
    s"$sectionId.mainSicCode",
    for {
      sicAndCompliance <- scheme.sicAndCompliance
      activity <- sicAndCompliance.mainBusinessActivity.flatMap(_.mainBusinessActivity.map(_.description))
    } yield activity,
    Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity())
  )

  val sicCodesRow: SummaryRow = SummaryRow(
    s"$sectionId.sicCodes", {
      val mainActivity = (for {
        sicAndCompliance <- scheme.sicAndCompliance
        activity <- sicAndCompliance.mainBusinessActivity.flatMap(_.mainBusinessActivity.map(_.description))
      } yield activity).toList

      val otherActivities = (for {
        sicAndCompliance <- scheme.sicAndCompliance
        activities <- sicAndCompliance.businessActivities.map(_.sicCodes.map(_.description))
      } yield activities).getOrElse(List.empty)

      (otherActivities ++ mainActivity).distinct
    },
    Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity())
  )

  val confirmIndustryClassificationCodesRow: SummaryRow = SummaryRow(
    s"$sectionId.businessActivities",
    sicAndComp.businessActivities.fold(Seq("app.common.no"))(codes => codes.sicCodes.map(
      sicCode => sicCode.code + " - " + sicCode.description
    )),
    Some(controllers.routes.SicAndComplianceController.returnToICL())
  )

  val accountIsProvidedRow: SummaryRow = yesNoRow(
    "companyBankAccount",
    scheme.bankAccount.map(_.isProvided),
    controllers.routes.BankAccountDetailsController.showHasCompanyBankAccountView()
  )

  val companyBankAccountDetails: SummaryRow = SummaryRow(
    s"$sectionId.companyBankAccount.details",
    scheme.bankAccount.flatMap(_.details).map(BankAccountDetails.bankSeq).getOrElse(Seq.empty),
    Some(controllers.routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails())
  )

  val noUKBankAccount: SummaryRow = SummaryRow(
    s"$sectionId.companyBankAccount.reason",
    scheme.bankAccount.flatMap(_.reason.map {
      case BeingSetup => "pages.noUKBankAccount.reason.beingSetup"
      case OverseasAccount => "pages.noUKBankAccount.reason.overseasAccount"
      case NameChange => "pages.noUKBankAccount.reason.nameChange"
    }).getOrElse(""),
    Some(controllers.routes.NoUKBankAccountController.showNoUKBankAccountView())
  )

  val companyNumber: SummaryRow = SummaryRow(
    s"$sectionId.companyNumber",
    vatApplicantDetails.entity.collect {
      case soleTrader: LimitedCompany => soleTrader.companyName
    }.getOrElse(""),
    Some(applicantRoutes.IncorpIdController.startIncorpIdJourney())
  )

  val ctutr: SummaryRow = SummaryRow(
    s"$sectionId.ctutr",
    vatApplicantDetails.entity.collect {
      case soleTrader: LimitedCompany => soleTrader.ctutr
    }.getOrElse(""),
    Some(applicantRoutes.IncorpIdController.startIncorpIdJourney())
  )

  val applyForEoriRow: SummaryRow = yesNoRow(
    "applyForEori",
    scheme.tradingDetails.flatMap(_.euGoods),
    controllers.registration.business.routes.ApplyForEoriController.show()
  )

  val joinFrsRow: SummaryRow = yesNoRow(
    "joinFrs",
    scheme.flatRateScheme.flatMap(_.joinFrs),
    controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show()
  )

  val costsInclusiveRow: SummaryRow = yesNoRow(
    "costsInclusive",
    scheme.flatRateScheme.flatMap(_.overBusinessGoods),
    controllers.routes.FlatRateController.annualCostsInclusivePage()
  )

  val estimateTotalSalesRow: SummaryRow = SummaryRow(
    s"$sectionId.estimateTotalSales",
    s"£${scheme.flatRateScheme.flatMap(_.estimateTotalSales.map("%,d".format(_))).getOrElse("0")}",
    Some(controllers.routes.FlatRateController.estimateTotalSales())
  )

  val costsLimitedRow: SummaryRow = SummaryRow(
    s"$sectionId.costsLimited",
    if (scheme.flatRateScheme.flatMap(_.overBusinessGoodsPercent).contains(true)) "app.common.yes" else "app.common.no",
    Some(controllers.routes.FlatRateController.annualCostsLimitedPage()),
    Seq(calculatedOnEstimatedSales.map("%,d".format(_)).getOrElse("0"))
  )

  val flatRatePercentageRow: SummaryRow = {
    SummaryRow(
      s"$sectionId.flatRate",
      if (scheme.flatRateScheme.flatMap(_.useThisRate).contains(true)) "app.common.yes" else "app.common.no",
      Some(if (isLimitedCostTrader) controllers.routes.FlatRateController.registerForFrsPage()
      else controllers.routes.FlatRateController.yourFlatRatePage()),
      Seq(if (isLimitedCostTrader) FlatRateService.defaultFlatRate.toString else scheme.flatRateScheme.flatMap(_.percent).getOrElse(0.0).toString)
    )
  }


  val businessSectorRow: SummaryRow = SummaryRow(
    s"$sectionId.businessSector",
    businessType.getOrElse(""),
    Some(controllers.routes.FlatRateController.businessType())
  )

  val providingWorkersRow: SummaryRow = yesNoRow(
    "supplyWorkers",
    scheme.sicAndCompliance.flatMap(_.supplyWorkers).map(_.yesNo),
    controllers.registration.sicandcompliance.routes.SupplyWorkersController.show()
  )

  val numberOfWorkersRow: SummaryRow = SummaryRow(
    s"$sectionId.numberOfWorkers",
    scheme.sicAndCompliance.flatMap(_.workers).fold("")(_.numberOfWorkers.toString),
    Some(controllers.registration.sicandcompliance.routes.WorkersController.show())
  )

  val intermediarySupplyRow: SummaryRow = yesNoRow(
    "intermediarySupply",
    scheme.sicAndCompliance.flatMap(_.intermediarySupply).map(_.yesNo),
    controllers.registration.sicandcompliance.routes.SupplyWorkersIntermediaryController.show()
  )

  val businessEmailRow: SummaryRow = SummaryRow(
    s"$sectionId.emailBusiness",
    scheme.businessContact.fold("")(_.companyContactDetails.get.email),
    Some(controllers.registration.business.routes.BusinessContactDetailsController.show())
  )

  val businessDaytimePhoneNumberRow: SummaryRow = SummaryRow(
    s"$sectionId.daytimePhoneBusiness",
    scheme.businessContact.fold("")(_.companyContactDetails.get.phoneNumber.getOrElse("")),
    Some(controllers.registration.business.routes.BusinessContactDetailsController.show())
  )

  val businessMobilePhoneNumberRow: SummaryRow = SummaryRow(
    s"$sectionId.mobileBusiness",
    scheme.businessContact.fold("")(_.companyContactDetails.get.mobileNumber.getOrElse("")),
    Some(controllers.registration.business.routes.BusinessContactDetailsController.show())
  )

  val businessWebsiteRow: SummaryRow = SummaryRow(
    s"$sectionId.website",
    scheme.businessContact.fold("")(_.companyContactDetails.get.websiteAddress.getOrElse("")),
    Some(controllers.registration.business.routes.BusinessContactDetailsController.show())
  )

  val ppobRow: SummaryRow = SummaryRow(
    s"$sectionId.ppob",
    scheme.businessContact.map(bc => Address.normalisedSeq(bc.ppobAddress.get)).getOrElse(Seq()),
    Some(controllers.registration.business.routes.PpobAddressController.startJourney())
  )

  val contactPreferenceRow: SummaryRow = SummaryRow.mandatory(
    s"$sectionId.contactPreference",
    scheme.businessContact.flatMap(_.contactPreference.map(_.toString)),
    Some(controllers.routes.ContactPreferenceController.showContactPreference())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (companyNumber, !isSoleTrader),
      (ctutr, !isSoleTrader),
      (sautr, isSoleTrader),
      (firstName, vatApplicantDetails.transactor.map(_.firstName).isDefined),
      (lastName, vatApplicantDetails.transactor.map(_.lastName).isDefined),
      (nino, vatApplicantDetails.transactor.map(_.nino).isDefined),
      (dob, vatApplicantDetails.transactor.map(_.dateOfBirth).isDefined),
      (roleInTheBusiness, true),
      (formerName, true),
      (formerNameDate, vatApplicantDetails.formerName.exists(_.yesNo)),
      (homeAddress, vatApplicantDetails.homeAddress.exists(_.address.isDefined)),
      (moreThanThreeYears, true),
      (previousAddress, vatApplicantDetails.previousAddress.exists(_.address.isDefined)),
      (email, vatApplicantDetails.emailAddress.map(_.email).isDefined),
      (telephone, vatApplicantDetails.telephoneNumber.map(_.telephone).isDefined),
      (businessEmailRow, true),
      (businessDaytimePhoneNumberRow, scheme.businessContact.exists(_.companyContactDetails.exists(_.phoneNumber.isDefined))),
      (businessMobilePhoneNumberRow, scheme.businessContact.exists(_.companyContactDetails.exists(_.mobileNumber.isDefined))),
      (businessWebsiteRow, scheme.businessContact.exists(_.companyContactDetails.exists(_.websiteAddress.isDefined))),
      (ppobRow, true),
      (contactPreferenceRow, scheme.businessContact.flatMap(_.contactPreference).isDefined),
      (companyBusinessDescriptionRow, true),
      (confirmIndustryClassificationCodesRow, true),
      (sicCodesRow, true),
      (mainActivityRow, true),
      (zeroRatedRow, scheme.returns.flatMap(_.zeroRatedSupplies).isDefined),
      (expectClaimRefundsRow, scheme.returns.flatMap(_.reclaimVatOnMostReturns).isDefined),
      (startDateRow, isflatRatePercentYes && scheme.flatRateScheme.flatMap(_.frsStart).isDefined),
      (accountingPeriodRow, true),
      (lastMonthOfAccountingYearRow, isAAS),
      (paymentFrequencyRow, isAAS),
      (paymentMethodRow, isAAS),
      (applyForEoriRow, scheme.tradingDetails.flatMap(_.euGoods).isDefined),
      (joinFrsRow, scheme.flatRateScheme.flatMap(_.joinFrs).isDefined),
      (costsInclusiveRow, joinFrsContainsTrue && scheme.flatRateScheme.flatMap(_.overBusinessGoods).isDefined),
      (estimateTotalSalesRow, isBusinessGoodsYes && scheme.flatRateScheme.flatMap(_.estimateTotalSales).isDefined),
      (costsLimitedRow, isBusinessGoodsYes && scheme.flatRateScheme.flatMap(_.overBusinessGoodsPercent).isDefined),
      (businessSectorRow, joinFrsContainsTrue && scheme.flatRateScheme.flatMap(_.categoryOfBusiness).exists(StringUtils.isNotBlank) && !isLimitedCostTrader),
      (flatRatePercentageRow, joinFrsContainsTrue && scheme.flatRateScheme.flatMap(_.useThisRate).isDefined),
      (providingWorkersRow, scheme.sicAndCompliance.flatMap(_.supplyWorkers).isDefined),
      (numberOfWorkersRow, scheme.sicAndCompliance.flatMap(_.workers).isDefined),
      (intermediarySupplyRow, scheme.sicAndCompliance.flatMap(_.intermediarySupply).isDefined),
      (tradingNameRow, true),
      (accountIsProvidedRow, true),
      (companyBankAccountDetails, scheme.bankAccount.exists(_.isProvided)),
      (noUKBankAccount, !scheme.bankAccount.exists(_.isProvided))
    )
  )

}
