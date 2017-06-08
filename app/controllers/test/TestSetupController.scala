/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.test

import java.time.LocalDate
import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.test.TestSetupForm
import models.ModelKeys.REGISTERING_OFFICER_KEY
import models.api._
import models.external.Officer
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.financial._
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.test._
import models.view.vatContact.BusinessContactDetails
import models.view.vatFinancials._
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatFinancials.vatBankAccount.{CompanyBankAccount, CompanyBankAccountDetails}
import models.view.vatLodgingOfficer._
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.vatChoice.{StartDateView, TaxableTurnover, VoluntaryRegistration, VoluntaryRegistrationReason}
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
import models.{S4LKey, S4LVatContact, S4LVatLodgingOfficer, S4LVatSicAndCompliance}
import play.api.libs.json.Format
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, S4LService, VatRegistrationService}

import scala.concurrent.Future

class TestSetupController @Inject()(ds: CommonPlayDependencies)(implicit s4LService: S4LService, vatRegistrationService: VatRegistrationService)
  extends VatRegistrationController(ds) with CommonService {

  def show: Action[AnyContent] = authorised.async(body = implicit user => implicit request => {
    for {
      taxableTurnover <- s4LService.fetchAndGet[TaxableTurnover]()
      voluntaryRegistration <- s4LService.fetchAndGet[VoluntaryRegistration]()
      voluntaryRegistrationReason <- s4LService.fetchAndGet[VoluntaryRegistrationReason]()
      startDate <- s4LService.fetchAndGet[StartDateView]()
      tradingName <- s4LService.fetchAndGet[TradingNameView]()

      euGoods <- s4LService.fetchAndGet[EuGoods]()
      applyEori <- s4LService.fetchAndGet[ApplyEori]()
      companyBankAccount <- s4LService.fetchAndGet[CompanyBankAccount]()
      companyBankAccountDetails <- s4LService.fetchAndGet[CompanyBankAccountDetails]()
      estimateVatTurnover <- s4LService.fetchAndGet[EstimateVatTurnover]()
      zeroRatedSales <- s4LService.fetchAndGet[ZeroRatedSales]()
      estimateZeroRatedSales <- s4LService.fetchAndGet[EstimateZeroRatedSales]()
      vatChargeExpectancy <- s4LService.fetchAndGet[VatChargeExpectancy]()
      vatReturnFrequency <- s4LService.fetchAndGet[VatReturnFrequency]()
      accountingPeriod <- s4LService.fetchAndGet[AccountingPeriod]()

      sicStub <- s4LService.fetchAndGet[SicStub]()

//      businessActivityDescription <- s4LService.fetchAndGet[BusinessActivityDescription]()
//
//      culturalNotForProfit <- s4LService.fetchAndGet[NotForProfit]()
//
//      labourCompanyProvideWorkers <- s4LService.fetchAndGet[CompanyProvideWorkers]()
//      labourWorkers <- s4LService.fetchAndGet[Workers]()
//      labourTemporaryContracts <- s4LService.fetchAndGet[TemporaryContracts]()
//      labourSkilledWorkers <- s4LService.fetchAndGet[SkilledWorkers]()
//
//      adviceOrConsultancy <- s4LService.fetchAndGet[AdviceOrConsultancy]()
//      actAsIntermediary <- s4LService.fetchAndGet[ActAsIntermediary]()
//      chargeFees <- s4LService.fetchAndGet[ChargeFees]()
//      additionalNonSecuritiesWork <- s4LService.fetchAndGet[AdditionalNonSecuritiesWork]()
//      discretionaryInvestment <- s4LService.fetchAndGet[DiscretionaryInvestmentManagementServices]()
//      leaseVehiclesOrEquipment <- s4LService.fetchAndGet[LeaseVehicles]()
//      investmentFundManagement <- s4LService.fetchAndGet[InvestmentFundManagement]()
//      manageAdditionalFunds <- s4LService.fetchAndGet[ManageAdditionalFunds]()

      vatSicAndCompliance <- s4LService.fetchAndGet[S4LVatSicAndCompliance]()

      vatContact <- s4LService.fetchAndGet[S4LVatContact]()
      vatLodgingOfficer <- s4LService.fetchAndGet[S4LVatLodgingOfficer]()

      eligibility <- s4LService.fetchAndGet[VatServiceEligibility]()

      testSetup = TestSetup(
        VatChoiceTestSetup(
          taxableTurnover.map(_.yesNo),
          voluntaryRegistration.map(_.yesNo),
          voluntaryRegistrationReason.map(_.reason),
          startDate.map(_.dateType),
          startDate.flatMap(_.date).map(_.getDayOfMonth.toString),
          startDate.flatMap(_.date).map(_.getMonthValue.toString),
          startDate.flatMap(_.date).map(_.getYear.toString)
        ),
        VatTradingDetailsTestSetup(
          tradingName.map(_.yesNo),
          tradingName.flatMap(_.tradingName),
          euGoods.map(_.yesNo),
          applyEori.map(_.yesNo.toString)),
        VatContactTestSetup(
          email = vatContact.flatMap(_.businessContactDetails).map(_.email),
          daytimePhone = vatContact.flatMap(_.businessContactDetails).flatMap(_.daytimePhone),
          mobile = vatContact.flatMap(_.businessContactDetails).flatMap(_.mobile),
          website = vatContact.flatMap(_.businessContactDetails).flatMap(_.website)
        ),
        VatFinancialsTestSetup(
          companyBankAccount.map(_.yesNo),
          companyBankAccountDetails.map(_.accountName),
          companyBankAccountDetails.map(_.accountNumber),
          companyBankAccountDetails.map(_.sortCode),
          estimateVatTurnover.map(_.vatTurnoverEstimate.toString),
          zeroRatedSales.map(_.yesNo),
          estimateZeroRatedSales.map(_.zeroRatedTurnoverEstimate.toString),
          vatChargeExpectancy.map(_.yesNo),
          vatReturnFrequency.map(_.frequencyType),
          accountingPeriod.map(_.accountingPeriod)),
        SicAndComplianceTestSetup(
          businessActivityDescription = vatSicAndCompliance.flatMap(_.description.map(_.description)),
          sicCode1 = sicStub.map(_.sicCode1.getOrElse("")),
          sicCode2 = sicStub.map(_.sicCode2.getOrElse("")),
          sicCode3 = sicStub.map(_.sicCode3.getOrElse("")),
          sicCode4 = sicStub.map(_.sicCode4.getOrElse("")),
          culturalNotForProfit = vatSicAndCompliance.flatMap(_.notForProfit.map(_.yesNo)),
          labourCompanyProvideWorkers = vatSicAndCompliance.flatMap(_.companyProvideWorkers.map(_.yesNo)),
          labourWorkers = vatSicAndCompliance.flatMap(_.workers.map(_.numberOfWorkers.toString)),
          labourTemporaryContracts = vatSicAndCompliance.flatMap(_.temporaryContracts.map(_.yesNo)),
          labourSkilledWorkers = vatSicAndCompliance.flatMap(_.skilledWorkers.map(_.yesNo)),
          financialAdviceOrConsultancy = vatSicAndCompliance.flatMap(_.adviceOrConsultancy.map(_.yesNo.toString)),
          financialActAsIntermediary = vatSicAndCompliance.flatMap(_.actAsIntermediary.map(_.yesNo.toString)),
          financialChargeFees = vatSicAndCompliance.flatMap(_.chargeFees.map(_.yesNo.toString)),
          financialAdditionalNonSecuritiesWork = vatSicAndCompliance.flatMap(_.additionalNonSecuritiesWork.map(_.yesNo.toString)),
          financialDiscretionaryInvestment = vatSicAndCompliance.flatMap(_.discretionaryInvestmentManagementServices.map(_.yesNo.toString)),
          financialLeaseVehiclesOrEquipment = vatSicAndCompliance.flatMap(_.leaseVehicles.map(_.yesNo.toString)),
          financialInvestmentFundManagement = vatSicAndCompliance.flatMap(_.investmentFundManagement.map(_.yesNo.toString)),
          financialManageAdditionalFunds = vatSicAndCompliance.flatMap(_.manageAdditionalFunds.map(_.yesNo.toString))),
        VatServiceEligibilityTestSetup(
          haveNino = eligibility.map(_.haveNino.getOrElse("").toString),
          doingBusinessAbroad = eligibility.map(_.doingBusinessAbroad.getOrElse("").toString),
          doAnyApplyToYou = eligibility.map(_.doAnyApplyToYou.getOrElse("").toString),
          applyingForAnyOf = eligibility.map(_.applyingForAnyOf.getOrElse("").toString),
          companyWillDoAnyOf = eligibility.map(_.companyWillDoAnyOf.getOrElse("").toString)),
        officerHomeAddress = OfficerHomeAddressTestSetup(
          line1 = vatLodgingOfficer.flatMap(_.officerHomeAddress).flatMap(_.address).map(_.line1),
          line2 = vatLodgingOfficer.flatMap(_.officerHomeAddress).flatMap(_.address).map(_.line2),
          line3 = vatLodgingOfficer.flatMap(_.officerHomeAddress).flatMap(_.address).flatMap(_.line3),
          line4 = vatLodgingOfficer.flatMap(_.officerHomeAddress).flatMap(_.address).flatMap(_.line4),
          postcode = vatLodgingOfficer.flatMap(_.officerHomeAddress).flatMap(_.address).flatMap(_.postcode),
          country = vatLodgingOfficer.flatMap(_.officerHomeAddress).flatMap(_.address).flatMap(_.country)),
        officerPreviousAddress = OfficerPreviousAddressTestSetup(
          threeYears = vatLodgingOfficer.flatMap(_.previousAddress).map(_.yesNo.toString),
          line1 = vatLodgingOfficer.flatMap(_.previousAddress).flatMap(_.address).map(_.line1),
          line2 = vatLodgingOfficer.flatMap(_.previousAddress).flatMap(_.address).map(_.line2),
          line3 = vatLodgingOfficer.flatMap(_.previousAddress).flatMap(_.address).flatMap(_.line3),
          line4 = vatLodgingOfficer.flatMap(_.previousAddress).flatMap(_.address).flatMap(_.line4),
          postcode = vatLodgingOfficer.flatMap(_.previousAddress).flatMap(_.address).flatMap(_.postcode),
          country = vatLodgingOfficer.flatMap(_.previousAddress).flatMap(_.address).flatMap(_.country)),
        vatLodgingOfficer = VatLodgingOfficerTestSetup(
          dobDay = vatLodgingOfficer.flatMap(_.officerDateOfBirth).map(_.dob.getDayOfMonth.toString),
          dobMonth = vatLodgingOfficer.flatMap(_.officerDateOfBirth).map(_.dob.getMonthValue.toString),
          dobYear = vatLodgingOfficer.flatMap(_.officerDateOfBirth).map(_.dob.getYear.toString),
          nino = vatLodgingOfficer.flatMap(_.officerNino).map(_.nino),
          role = vatLodgingOfficer.flatMap(_.completionCapacity).flatMap(_.completionCapacity).map(_.role),
          firstname = vatLodgingOfficer.flatMap(_.completionCapacity).flatMap(_.completionCapacity).flatMap(_.name.forename),
          othernames = vatLodgingOfficer.flatMap(_.completionCapacity).flatMap(_.completionCapacity).flatMap(_.name.otherForenames),
          surname = vatLodgingOfficer.flatMap(_.completionCapacity).flatMap(_.completionCapacity).map(_.name.surname),
          email = vatLodgingOfficer.flatMap(_.officerContactDetails).flatMap(_.email),
          mobile = vatLodgingOfficer.flatMap(_.officerContactDetails).flatMap(_.daytimePhone),
          phone = vatLodgingOfficer.flatMap(_.officerContactDetails).flatMap(_.mobile),
          formernameChoice = vatLodgingOfficer.flatMap(_.formerName).map(_.yesNo.toString),
          formername = vatLodgingOfficer.flatMap(_.formerName).flatMap(_.formerName)
        )
      )
      form = TestSetupForm.form.fill(testSetup)
    } yield Ok(views.html.pages.test.test_setup(form))
  })


  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {

    def saveStartDate(data: TestSetup) = {
      s4LService.save[StartDateView](data.vatChoice.startDateChoice match {
        case None => StartDateView()
        case Some("SPECIFIC_DATE") => StartDateView(dateType = "SPECIFIC_DATE", date = Some(LocalDate.of(
          data.vatChoice.startDateYear.map(_.toInt).get,
          data.vatChoice.startDateMonth.map(_.toInt).get,
          data.vatChoice.startDateDay.map(_.toInt).get
        )))
        case Some("BUSINESS_START_DATE") => StartDateView(dateType = "BUSINESS_START_DATE", ctActiveDate = Some(LocalDate.of(
          data.vatChoice.startDateYear.map(_.toInt).get,
          data.vatChoice.startDateMonth.map(_.toInt).get,
          data.vatChoice.startDateDay.map(_.toInt).get
        )))
        case Some(t) => StartDateView(t, None)
      })
    }

    def saveToS4Later[T: Format : S4LKey](userEntered: Option[String], data: TestSetup, f: TestSetup => T): Future[Unit] =
      userEntered.map(_ => s4LService.save(f(data)).map(_ => ())).getOrElse(Future.successful(()))

    TestSetupForm.form.bindFromRequest().fold(
      badForm => {
        Future.successful(BadRequest(views.html.pages.test.test_setup(badForm)))
      }, {
        data: TestSetup => {
          for {
            _ <- saveStartDate(data)
            _ <- saveToS4Later(data.vatChoice.taxableTurnoverChoice, data, { x => TaxableTurnover(x.vatChoice.taxableTurnoverChoice.get) })
            _ <- saveToS4Later(data.vatChoice.voluntaryChoice, data, { x => VoluntaryRegistration(x.vatChoice.voluntaryChoice.get) })
            _ <- saveToS4Later(data.vatChoice.voluntaryRegistrationReason, data, { x => VoluntaryRegistrationReason(x.vatChoice.voluntaryRegistrationReason.get) })
            _ <- saveToS4Later(data.vatTradingDetails.tradingNameChoice, data, { x => TradingNameView(x.vatTradingDetails.tradingNameChoice.get, data.vatTradingDetails.tradingName) })
            _ <- saveToS4Later(data.vatTradingDetails.euGoods, data, { x => EuGoods(x.vatTradingDetails.euGoods.get) })
            _ <- saveToS4Later(data.vatTradingDetails.applyEori, data, { x => ApplyEori(x.vatTradingDetails.applyEori.get.toBoolean) })
            _ <- saveToS4Later(data.vatFinancials.companyBankAccountChoice, data, { x => CompanyBankAccount(x.vatFinancials.companyBankAccountChoice.get) })
            _ <- saveToS4Later(data.vatFinancials.companyBankAccountName, data, {
              x =>
                CompanyBankAccountDetails(x.vatFinancials.companyBankAccountName.get,
                  x.vatFinancials.companyBankAccountNumber.get, x.vatFinancials.sortCode.get)
            })
            _ <- saveToS4Later(data.vatFinancials.estimateVatTurnover, data, { x => EstimateVatTurnover(x.vatFinancials.estimateVatTurnover.get.toLong) })
            _ <- saveToS4Later(data.vatFinancials.zeroRatedSalesChoice, data, { x => ZeroRatedSales(x.vatFinancials.zeroRatedSalesChoice.get) })
            _ <- saveToS4Later(data.vatFinancials.zeroRatedTurnoverEstimate, data, { x => EstimateZeroRatedSales(x.vatFinancials.zeroRatedTurnoverEstimate.get.toLong) })
            _ <- saveToS4Later(data.vatFinancials.vatChargeExpectancyChoice, data, { x => VatChargeExpectancy(x.vatFinancials.vatChargeExpectancyChoice.get) })
            _ <- saveToS4Later(data.vatFinancials.vatReturnFrequency, data, { x => VatReturnFrequency(x.vatFinancials.vatReturnFrequency.get) })
            _ <- saveToS4Later(data.vatFinancials.accountingPeriod, data, { x => AccountingPeriod(x.vatFinancials.accountingPeriod.get) })

            _ <- saveToS4Later(data.sicAndCompliance.sicCode1, data, { x =>
              SicStub(Some(x.sicAndCompliance.sicCode1.getOrElse("")),
                Some(x.sicAndCompliance.sicCode2.getOrElse("")),
                Some(x.sicAndCompliance.sicCode3.getOrElse("")),
                Some(x.sicAndCompliance.sicCode4.getOrElse("")))
            })

            _ <- s4LService.save(vatSicAndComplianceFromData(data))

            _ <- saveToS4Later(data.vatServiceEligibility.haveNino, data, { x =>
              VatServiceEligibility(x.vatServiceEligibility.haveNino.map(_.toBoolean),
                x.vatServiceEligibility.doingBusinessAbroad.map(_.toBoolean),
                x.vatServiceEligibility.doAnyApplyToYou.map(_.toBoolean),
                x.vatServiceEligibility.applyingForAnyOf.map(_.toBoolean),
                x.vatServiceEligibility.companyWillDoAnyOf.map(_.toBoolean))
            })

            _ <- s4LService.save(vatContactFromData(data))

            vatLodgingOfficer = vatLodgingOfficerFromData(data)
            _ <- s4LService.save(vatLodgingOfficerFromData(data))

            // Keystore hack for Officer DOB page
            officer = vatLodgingOfficer.completionCapacity.
                flatMap(ccv => ccv.completionCapacity.
                  map(cc => Officer(cc.name, cc.role, None)))
            _ <- keystoreConnector.cache(REGISTERING_OFFICER_KEY, officer.getOrElse(Officer.empty))

          } yield Ok("Test setup complete")
        }
      })
  })

  private def vatContactFromData(data: TestSetup): S4LVatContact = {
    val businessContactDetails = data.vatContact.email.map(_ =>
              BusinessContactDetails(data.vatContact.email.get,
                                      data.vatContact.daytimePhone,
                                      data.vatContact.mobile,
                                      data.vatContact.website))

    S4LVatContact(businessContactDetails = businessContactDetails)
  }

  private def vatSicAndComplianceFromData(data: TestSetup): S4LVatSicAndCompliance = {
    val base = data.sicAndCompliance
    val compliance: S4LVatSicAndCompliance =
      (base.culturalNotForProfit, base.labourCompanyProvideWorkers, base.financialAdviceOrConsultancy) match {
        case (None, None, None) => S4LVatSicAndCompliance()
        case (Some(_), None, None) => S4LVatSicAndCompliance(
          notForProfit = Some(NotForProfit(base.culturalNotForProfit.get)))
        case(None, Some(_), None) => S4LVatSicAndCompliance(
          companyProvideWorkers = Some(CompanyProvideWorkers(base.labourCompanyProvideWorkers.get)),
          workers = Some(Workers(base.labourWorkers.get.toInt)),
          temporaryContracts = Some(TemporaryContracts(base.labourTemporaryContracts.get)),
          skilledWorkers = Some(SkilledWorkers(base.labourSkilledWorkers.get)))
        case(None, None, Some(_)) => S4LVatSicAndCompliance(
          adviceOrConsultancy = Some(AdviceOrConsultancy(base.financialAdviceOrConsultancy.get.toBoolean)),
          actAsIntermediary = Some(ActAsIntermediary(base.financialActAsIntermediary.get.toBoolean)),
          chargeFees = Some(ChargeFees(base.financialChargeFees.get.toBoolean)),
          leaseVehicles = Some(LeaseVehicles(base.financialLeaseVehiclesOrEquipment.get.toBoolean)),
          additionalNonSecuritiesWork = Some(AdditionalNonSecuritiesWork(base.financialAdditionalNonSecuritiesWork.get.toBoolean)),
          discretionaryInvestmentManagementServices = Some(DiscretionaryInvestmentManagementServices(base.financialDiscretionaryInvestment.get.toBoolean)),
          investmentFundManagement = Some(InvestmentFundManagement(base.financialInvestmentFundManagement.get.toBoolean)),
          manageAdditionalFunds = Some(ManageAdditionalFunds(base.financialManageAdditionalFunds.get.toBoolean)))
      }

    compliance.copy(description = base.businessActivityDescription.map(BusinessActivityDescription(_)))
  }

  private def vatLodgingOfficerFromData(data: TestSetup): S4LVatLodgingOfficer = {
    val homeAddress: Option[ScrsAddress] = data.officerHomeAddress.line1.map(_ =>
      ScrsAddress(
        line1 = data.officerHomeAddress.line1.getOrElse(""),
        line2 = data.officerHomeAddress.line2.getOrElse(""),
        line3 = data.officerHomeAddress.line3,
        line4 = data.officerHomeAddress.line4,
        postcode = data.officerHomeAddress.postcode,
        country = data.officerHomeAddress.country))
    val threeYears: Option[String] = data.officerPreviousAddress.threeYears
    val previousAddress: Option[ScrsAddress] = data.officerPreviousAddress.line1.map(_ =>
      ScrsAddress(
        line1 = data.officerPreviousAddress.line1.getOrElse(""),
        line2 = data.officerPreviousAddress.line2.getOrElse(""),
        line3 = data.officerPreviousAddress.line3,
        line4 = data.officerPreviousAddress.line4,
        postcode = data.officerPreviousAddress.postcode,
        country = data.officerPreviousAddress.country))
    val dob: Option[LocalDate] = data.vatLodgingOfficer.dobDay.map(_ =>
      LocalDate.of(
        data.vatLodgingOfficer.dobYear.getOrElse("1900").toInt,
        data.vatLodgingOfficer.dobMonth.getOrElse("1").toInt,
        data.vatLodgingOfficer.dobDay.getOrElse("1").toInt))
    val nino = data.vatLodgingOfficer.nino
    val completionCapacity = data.vatLodgingOfficer.role.map(_ => {
      CompletionCapacity(
        name = Name(data.vatLodgingOfficer.firstname,
        data.vatLodgingOfficer.othernames,
        data.vatLodgingOfficer.surname.getOrElse("")),
        role = data.vatLodgingOfficer.role.getOrElse(""))
    })
    val contactDetails = data.vatLodgingOfficer.email.map(_ =>
      OfficerContactDetails(
        email = data.vatLodgingOfficer.email,
        mobile = data.vatLodgingOfficer.mobile,
        tel = data.vatLodgingOfficer.phone))
    val formerName = data.vatLodgingOfficer.formernameChoice.map(_ =>
      FormerName(
        selection = data.vatLodgingOfficer.formernameChoice.map(_.toBoolean).getOrElse(false),
        formerName = data.vatLodgingOfficer.formername))
    S4LVatLodgingOfficer(
      previousAddress = threeYears.map(t => PreviousAddressView(t.toBoolean, previousAddress)),
      officerHomeAddress = homeAddress.map(a => OfficerHomeAddressView(a.id, Some(a))),
      officerDateOfBirth = dob.map(OfficerDateOfBirthView(_, completionCapacity.map(_.name))),
      officerNino = nino.map(OfficerNinoView(_)),
      completionCapacity = completionCapacity.map(CompletionCapacityView(_)),
      officerContactDetails = contactDetails.map(OfficerContactDetailsView(_)),
      formerName = formerName.map(FormerNameView(_))
    )
  }

}