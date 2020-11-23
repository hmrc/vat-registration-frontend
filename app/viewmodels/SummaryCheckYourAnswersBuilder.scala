/*
 * Copyright 2020 HM Revenue & Customs
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
import models._
import models.api.{Address, Threshold, VatScheme}
import models.view.{ApplicantDetails, SummaryRow, SummarySection}
import org.apache.commons.lang3.StringUtils

case class SummaryCheckYourAnswersBuilder(scheme: VatScheme,
                                          vatApplicantDetails: ApplicantDetails,
                                          calculatedOnEstimatedSales: Option[Long],
                                          businessType: Option[String],
                                          turnoverEstimates: Option[TurnoverEstimates],
                                          threshold: Option[Threshold],
                                          returnsBlock: Option[Returns]) extends SummarySectionBuilder {

  override val sectionId: String = "directorDetails"

  val joinFrsContainsTrue: Boolean = scheme.flatRateScheme.flatMap(_.joinFrs).contains(true)
  val isflatRatePercentYes: Boolean = scheme.flatRateScheme.flatMap(_.useThisRate).contains(true)
  val isBusinessGoodsYes: Boolean = joinFrsContainsTrue && scheme.flatRateScheme.flatMap(_.overBusinessGoods).contains(true)

  private val thresholdBlock = threshold.getOrElse(throw new IllegalStateException("Missing threshold block to show summary"))
  private val voluntaryRegistration = !thresholdBlock.mandatoryRegistration

  def startDateRow: SummaryRow = SummaryRow(
    s"$sectionId.startDate",
    returnsBlock.flatMap(_.start).flatMap(_.date) match {
      case Some(date) => date.format(presentationFormatter)
      case _ => s"pages.summary.$sectionId.mandatoryStartDate"
    },
    if (voluntaryRegistration) Some(controllers.routes.ReturnsController.voluntaryStartPage()) else None
  )

  val tradingNameRow: SummaryRow = SummaryRow(
    s"$sectionId.tradingName",
    scheme.tradingDetails.flatMap(_.tradingNameView).flatMap(_.tradingName).getOrElse("app.common.no"),
    Some(controllers.registration.business.routes.TradingNameController.show())
  )


  val firstName: SummaryRow = SummaryRow(
    s"$sectionId.firstName",
    vatApplicantDetails.transactorDetails.map(_.firstName).getOrElse(""),
    Some(applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney())
  )

  val lastName: SummaryRow = SummaryRow(
    s"$sectionId.lastName",
    vatApplicantDetails.transactorDetails.map(_.lastName).getOrElse(""),
    Some(applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney())
  )

  val nino: SummaryRow = SummaryRow(
    s"$sectionId.nino",
    vatApplicantDetails.transactorDetails.map(_.nino).getOrElse(""),
    Some(applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney())
  )

  val dob: SummaryRow = SummaryRow(
    s"$sectionId.dob",
    vatApplicantDetails.transactorDetails.map(_.dateOfBirth.format(presentationFormatter)).getOrElse(""),
    Some(applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney())
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
    Some(controllers.routes.ZeroRatedSuppliesController.show())
  )

  val expectClaimRefundsRow: SummaryRow = yesNoRow(
    "claimRefunds",
    scheme.returns.flatMap(_.reclaimVatOnMostReturns),
    controllers.registration.returns.routes.ClaimRefundsController.show()
  )

  val accountingPeriodRow: SummaryRow = SummaryRow(
    s"$sectionId.accountingPeriod",
    (scheme.returns.flatMap(_.frequency), scheme.returns.flatMap(_.staggerStart)) match {
      case (Some(Frequency.monthly), _) => s"pages.summary.$sectionId.accountingPeriod.monthly"
      case (Some(Frequency.quarterly), Some(period)) =>
        s"pages.summary.$sectionId.accountingPeriod.${period.substring(0, 3)}"
      case _ => ""
    },
    Some(controllers.routes.ReturnsController.accountPeriodsPage())
  )

  val sicAndComp = scheme.sicAndCompliance.fold(SicAndCompliance())(a => a)

  val companyBusinessDescriptionRow: SummaryRow = SummaryRow(
    s"$sectionId.businessDescription",
    sicAndComp.description.fold("app.common.no")(desc =>
      if (desc.description.isEmpty) "app.common.no" else desc.description),
    Some(controllers.routes.SicAndComplianceController.showBusinessActivityDescription())
  )

  val mainActivityRow: SummaryRow = SummaryRow.mandatory(
    s"$sectionId.mainSicCode",
    (for {
      sicAndCompliance <- scheme.sicAndCompliance
      activity <- sicAndCompliance.mainBusinessActivity.flatMap(_.mainBusinessActivity.map(_.description))
    } yield activity),
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
        activities <- sicAndCompliance.otherBusinessActivities.map(_.sicCodes.map(_.description))
      } yield activities).getOrElse(List.empty)

      (otherActivities ++ mainActivity).distinct
    },
    Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity())
  )

  val confirmIndustryClassificationCodesRow: SummaryRow = SummaryRow(
    s"$sectionId.otherBusinessActivities",
    sicAndComp.otherBusinessActivities.fold(Seq("app.common.no"))(codes => codes.sicCodes.map(
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


  val companyNumber: SummaryRow = SummaryRow(
    s"$sectionId.companyNumber",
    vatApplicantDetails.incorporationDetails.map(_.companyNumber).getOrElse(""),
    Some(applicantRoutes.IncorpIdController.startIncorpIdJourney())
  )
  val ctutr: SummaryRow = SummaryRow(
    s"$sectionId.ctutr",
    vatApplicantDetails.incorporationDetails.map(_.ctutr).getOrElse(""),
    Some(applicantRoutes.IncorpIdController.startIncorpIdJourney())
  )
  val buySellEuGoodsRow: SummaryRow = yesNoRow(
    "euGoods",
    scheme.tradingDetails.flatMap(_.euGoods),
    controllers.registration.business.routes.EuGoodsController.show()
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

  val flatRatePercentageRow: SummaryRow = SummaryRow(
    s"$sectionId.flatRate",
    if (scheme.flatRateScheme.flatMap(_.useThisRate).contains(true)) "app.common.yes" else "app.common.no",
    scheme.flatRateScheme.flatMap(_.categoryOfBusiness).collect {
      case s if s.nonEmpty => controllers.routes.FlatRateController.yourFlatRatePage()
      case _ => controllers.routes.FlatRateController.registerForFrsPage()
    },
    Seq(scheme.flatRateScheme.flatMap(_.percent).getOrElse(0.0).toString)
  )

  val businessSectorRow: SummaryRow = SummaryRow(
    s"$sectionId.businessSector",
    businessType.getOrElse(""),
    Some(controllers.routes.FlatRateController.businessType())
  )

  val providingWorkersRow: SummaryRow = yesNoRow(
    "providesWorkers",
    scheme.sicAndCompliance.flatMap(_.companyProvideWorkers).flatMap(v => CompanyProvideWorkers.toBool(v.yesNo)),
    controllers.routes.LabourComplianceController.showProvideWorkers()
  )

  val numberOfWorkersRow: SummaryRow = SummaryRow(
    s"$sectionId.numberOfWorkers",
    scheme.sicAndCompliance.flatMap(_.workers).fold("")(_.numberOfWorkers.toString),
    Some(controllers.routes.LabourComplianceController.showWorkers())
  )

  val temporaryContractsRow: SummaryRow = yesNoRow(
    "workersOnTemporaryContracts",
    scheme.sicAndCompliance.flatMap(_.temporaryContracts).flatMap(v => TemporaryContracts.toBool(v.yesNo)),
    controllers.routes.LabourComplianceController.showTemporaryContracts()
  )

  val skilledWorkersRow: SummaryRow = yesNoRow(
    "providesSkilledWorkers",
    scheme.sicAndCompliance.flatMap(_.skilledWorkers).flatMap(v => SkilledWorkers.toBool(v.yesNo)),
    controllers.routes.LabourComplianceController.showSkilledWorkers()
  )


  val businessEmailRow: SummaryRow = SummaryRow(
    s"$sectionId.emailBusiness",
    scheme.businessContact.fold("")(_.companyContactDetails.get.email),
    Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails())
  )

  val businessDaytimePhoneNumberRow: SummaryRow = SummaryRow(
    s"$sectionId.daytimePhoneBusiness",
    scheme.businessContact.fold("")(_.companyContactDetails.get.phoneNumber.getOrElse("")),
    Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails())
  )

  val businessMobilePhoneNumberRow: SummaryRow = SummaryRow(
    s"$sectionId.mobileBusiness",
    scheme.businessContact.fold("")(_.companyContactDetails.get.mobileNumber.getOrElse("")),
    Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails())
  )


  val businessWebsiteRow: SummaryRow = SummaryRow(
    s"$sectionId.website",
    scheme.businessContact.fold("")(_.companyContactDetails.get.websiteAddress.getOrElse("")),
    Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails())
  )

  val ppobRow: SummaryRow = SummaryRow(
    s"$sectionId.ppob",
    scheme.businessContact.map(bc => Address.normalisedSeq(bc.ppobAddress.get)).getOrElse(Seq()),
    Some(controllers.routes.BusinessContactDetailsController.ppobRedirectToAlf())
  )

  val contactPreferenceRow: SummaryRow = SummaryRow.mandatory(
    s"$sectionId.contactPreference",
    scheme.businessContact.flatMap(_.contactPreference.map(_.toString)),
    Some(controllers.routes.ContactPreferenceController.showContactPreference())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (companyNumber, vatApplicantDetails.transactorDetails.map(_.firstName).isDefined),
      (ctutr, vatApplicantDetails.transactorDetails.map(_.lastName).isDefined),
      (firstName, vatApplicantDetails.transactorDetails.map(_.firstName).isDefined),
      (lastName, vatApplicantDetails.transactorDetails.map(_.lastName).isDefined),
      (nino, vatApplicantDetails.transactorDetails.map(_.nino).isDefined),
      (dob, vatApplicantDetails.transactorDetails.map(_.dateOfBirth).isDefined),
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
      (buySellEuGoodsRow, scheme.tradingDetails.flatMap(_.euGoods).isDefined),
      (joinFrsRow, scheme.flatRateScheme.flatMap(_.joinFrs).isDefined),
      (costsInclusiveRow, joinFrsContainsTrue && scheme.flatRateScheme.flatMap(_.overBusinessGoods).isDefined),
      (estimateTotalSalesRow, isBusinessGoodsYes && scheme.flatRateScheme.flatMap(_.estimateTotalSales).isDefined),
      (costsLimitedRow, isBusinessGoodsYes && scheme.flatRateScheme.flatMap(_.overBusinessGoodsPercent).isDefined),
      (businessSectorRow, joinFrsContainsTrue && scheme.flatRateScheme.flatMap(_.categoryOfBusiness).exists(StringUtils.isNotBlank)),
      (flatRatePercentageRow, joinFrsContainsTrue && scheme.flatRateScheme.flatMap(_.useThisRate).isDefined),
      (providingWorkersRow, scheme.sicAndCompliance.flatMap(_.companyProvideWorkers).isDefined),
      (numberOfWorkersRow, scheme.sicAndCompliance.flatMap(_.workers).isDefined),
      (temporaryContractsRow, scheme.sicAndCompliance.flatMap(_.temporaryContracts).isDefined),
      (skilledWorkersRow, scheme.sicAndCompliance.flatMap(_.skilledWorkers).isDefined),
      (tradingNameRow, true),
      (accountIsProvidedRow, true),
      (companyBankAccountDetails, scheme.bankAccount.flatMap(_.details).isDefined)
    )
  )

}
