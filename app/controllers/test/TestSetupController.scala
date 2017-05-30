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
import models.api.{Name, ScrsAddress, VatServiceEligibility}
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
import models.{S4LKey, S4LVatLodgingOfficer}
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
      businessContactDetails <- s4LService.fetchAndGet[BusinessContactDetails]()
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
      businessActivityDescription <- s4LService.fetchAndGet[BusinessActivityDescription]()
      sicStub <- s4LService.fetchAndGet[SicStub]()

      culturalNotForProfit <- s4LService.fetchAndGet[NotForProfit]()

      labourCompanyProvideWorkers <- s4LService.fetchAndGet[CompanyProvideWorkers]()
      labourWorkers <- s4LService.fetchAndGet[Workers]()
      labourTemporaryContracts <- s4LService.fetchAndGet[TemporaryContracts]()
      labourSkilledWorkers <- s4LService.fetchAndGet[SkilledWorkers]()

      adviceOrConsultancy <- s4LService.fetchAndGet[AdviceOrConsultancy]()
      actAsIntermediary <- s4LService.fetchAndGet[ActAsIntermediary]()
      chargeFees <- s4LService.fetchAndGet[ChargeFees]()
      additionalNonSecuritiesWork <- s4LService.fetchAndGet[AdditionalNonSecuritiesWork]()
      discretionaryInvestment <- s4LService.fetchAndGet[DiscretionaryInvestmentManagementServices]()
      leaseVehiclesOrEquipment <- s4LService.fetchAndGet[LeaseVehicles]()
      investmentFundManagement <- s4LService.fetchAndGet[InvestmentFundManagement]()
      manageAdditionalFunds <- s4LService.fetchAndGet[ManageAdditionalFunds]()

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
          businessContactDetails.map(_.email),
          businessContactDetails.flatMap(_.daytimePhone),
          businessContactDetails.flatMap(_.mobile),
          businessContactDetails.flatMap(_.website)
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
          businessActivityDescription = businessActivityDescription.map(_.description),
          sicCode1 = sicStub.map(_.sicCode1.getOrElse("")),
          sicCode2 = sicStub.map(_.sicCode2.getOrElse("")),
          sicCode3 = sicStub.map(_.sicCode3.getOrElse("")),
          sicCode4 = sicStub.map(_.sicCode4.getOrElse("")),
          culturalNotForProfit = culturalNotForProfit.map(_.yesNo),
          labourCompanyProvideWorkers = labourCompanyProvideWorkers.map(_.yesNo),
          labourWorkers = labourWorkers.map(_.numberOfWorkers.toString),
          labourTemporaryContracts = labourTemporaryContracts.map(_.yesNo),
          labourSkilledWorkers = labourSkilledWorkers.map(_.yesNo),
          financialAdviceOrConsultancy = adviceOrConsultancy.map(_.yesNo.toString),
          financialActAsIntermediary = actAsIntermediary.map(_.yesNo.toString),
          financialChargeFees = chargeFees.map(_.yesNo.toString),
          financialAdditionalNonSecuritiesWork = additionalNonSecuritiesWork.map(_.yesNo.toString),
          financialDiscretionaryInvestment = discretionaryInvestment.map(_.yesNo.toString),
          financialLeaseVehiclesOrEquipment = leaseVehiclesOrEquipment.map(_.yesNo.toString),
          financialInvestmentFundManagement = investmentFundManagement.map(_.yesNo.toString),
          financialManageAdditionalFunds = manageAdditionalFunds.map(_.yesNo.toString)),
        VatServiceEligibilityTestSetup(
          haveNino = eligibility.map(_.haveNino.getOrElse("").toString),
          doingBusinessAbroad = eligibility.map(_.doingBusinessAbroad.getOrElse("").toString),
          doAnyApplyToYou = eligibility.map(_.doAnyApplyToYou.getOrElse("").toString),
          applyingForAnyOf = eligibility.map(_.applyingForAnyOf.getOrElse("").toString),
          companyWillDoAnyOf = eligibility.map(_.companyWillDoAnyOf.getOrElse("").toString)),
        officerHomeAddress = OfficerHomeAddressTestSetup(
          line1 = vatLodgingOfficer.flatMap(vlo => vlo.officerHomeAddress).flatMap(oha => oha.address).map(a => a.line1),
          line2 = vatLodgingOfficer.flatMap(vlo => vlo.officerHomeAddress).flatMap(oha => oha.address).map(a => a.line2),
          line3 = vatLodgingOfficer.flatMap(vlo => vlo.officerHomeAddress).flatMap(oha => oha.address).flatMap(a => a.line3),
          line4 = vatLodgingOfficer.flatMap(vlo => vlo.officerHomeAddress).flatMap(oha => oha.address).flatMap(a => a.line4),
          postcode = vatLodgingOfficer.flatMap(vlo => vlo.officerHomeAddress).flatMap(oha => oha.address).flatMap(a => a.postcode),
          country = vatLodgingOfficer.flatMap(vlo => vlo.officerHomeAddress).flatMap(oha => oha.address).flatMap(a => a.country)),
        vatLodgingOfficer = VatLodgingOfficerTestSetup(
          vatLodgingOfficer.flatMap(vlo => vlo.officerDateOfBirth).map(odob => odob.dob.getDayOfMonth.toString),
          vatLodgingOfficer.flatMap(vlo => vlo.officerDateOfBirth).map(odob => odob.dob.getMonthValue.toString),
          vatLodgingOfficer.flatMap(vlo => vlo.officerDateOfBirth).map(odob => odob.dob.getYear.toString),
          vatLodgingOfficer.flatMap(vlo => vlo.officerNino).map(onino => onino.nino),
          vatLodgingOfficer.flatMap(vlo => vlo.completionCapacity).flatMap(ccv => ccv.officer).map(o => o.role),
          vatLodgingOfficer.flatMap(vlo => vlo.completionCapacity).flatMap(ccv => ccv.officer).flatMap(o => o.name.forename),
          vatLodgingOfficer.flatMap(vlo => vlo.completionCapacity).flatMap(ccv => ccv.officer).flatMap(o => o.name.otherForenames),
          vatLodgingOfficer.flatMap(vlo => vlo.completionCapacity).flatMap(ccv => ccv.officer).map(o => o.name.surname)
        )
      )
      form = TestSetupForm.form.fill(testSetup)
    } yield Ok(views.html.pages.test.test_setup(form))
  })


  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    // TODO Special case
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
            _ <- saveToS4Later(data.vatContact.email, data, { x => BusinessContactDetails(x.vatContact.email.get, x.vatContact.daytimePhone, x.vatContact.mobile, x.vatContact.website) })
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
            _ <- saveToS4Later(data.sicAndCompliance.businessActivityDescription, data, { x => BusinessActivityDescription(x.sicAndCompliance.businessActivityDescription.get) })
            _ <- saveToS4Later(data.sicAndCompliance.sicCode1, data, { x =>
              SicStub(Some(x.sicAndCompliance.sicCode1.getOrElse("")),
                Some(x.sicAndCompliance.sicCode2.getOrElse("")),
                Some(x.sicAndCompliance.sicCode3.getOrElse("")),
                Some(x.sicAndCompliance.sicCode4.getOrElse("")))
            })
            _ <- saveToS4Later(data.sicAndCompliance.culturalNotForProfit, data, { x => NotForProfit(x.sicAndCompliance.culturalNotForProfit.get) })

            _ <- saveToS4Later(data.sicAndCompliance.labourCompanyProvideWorkers, data, { x => CompanyProvideWorkers(x.sicAndCompliance.labourCompanyProvideWorkers.get) })
            _ <- saveToS4Later(data.sicAndCompliance.labourWorkers, data, { x => Workers(x.sicAndCompliance.labourWorkers.get.toInt) })
            _ <- saveToS4Later(data.sicAndCompliance.labourTemporaryContracts, data, { x => TemporaryContracts(x.sicAndCompliance.labourTemporaryContracts.get) })
            _ <- saveToS4Later(data.sicAndCompliance.labourSkilledWorkers, data, { x => SkilledWorkers(x.sicAndCompliance.labourSkilledWorkers.get) })

            _ <- saveToS4Later(data.sicAndCompliance.financialAdviceOrConsultancy, data, { x => AdviceOrConsultancy(x.sicAndCompliance.financialAdviceOrConsultancy.get.toBoolean) })
            _ <- saveToS4Later(data.sicAndCompliance.financialActAsIntermediary, data, { x => ActAsIntermediary(x.sicAndCompliance.financialActAsIntermediary.get.toBoolean) })
            _ <- saveToS4Later(data.sicAndCompliance.financialChargeFees, data, { x => ChargeFees(x.sicAndCompliance.financialChargeFees.get.toBoolean) })
            _ <- saveToS4Later(data.sicAndCompliance.financialAdditionalNonSecuritiesWork, data, { x => AdditionalNonSecuritiesWork(x.sicAndCompliance.financialAdditionalNonSecuritiesWork.get.toBoolean) })
            _ <- saveToS4Later(data.sicAndCompliance.financialDiscretionaryInvestment, data, { x => DiscretionaryInvestmentManagementServices(x.sicAndCompliance.financialDiscretionaryInvestment.get.toBoolean) })
            _ <- saveToS4Later(data.sicAndCompliance.financialLeaseVehiclesOrEquipment, data, { x => LeaseVehicles(x.sicAndCompliance.financialLeaseVehiclesOrEquipment.get.toBoolean) })
            _ <- saveToS4Later(data.sicAndCompliance.financialInvestmentFundManagement, data, { x => InvestmentFundManagement(x.sicAndCompliance.financialInvestmentFundManagement.get.toBoolean) })
            _ <- saveToS4Later(data.sicAndCompliance.financialManageAdditionalFunds, data, { x => ManageAdditionalFunds(x.sicAndCompliance.financialManageAdditionalFunds.get.toBoolean) })

            _ <- saveToS4Later(data.vatServiceEligibility.haveNino, data, { x =>
              VatServiceEligibility(x.vatServiceEligibility.haveNino.map(_.toBoolean),
                x.vatServiceEligibility.doingBusinessAbroad.map(_.toBoolean),
                x.vatServiceEligibility.doAnyApplyToYou.map(_.toBoolean),
                x.vatServiceEligibility.applyingForAnyOf.map(_.toBoolean),
                x.vatServiceEligibility.companyWillDoAnyOf.map(_.toBoolean))
            })

            _ <- s4LService.save(vatLodingingOfficerFromData(data))

          } yield Ok("Test setup complete")
        }
      })
  })

  private def vatLodingingOfficerFromData(data: TestSetup): S4LVatLodgingOfficer = {
    val address = ScrsAddress(
      line1 = data.officerHomeAddress.line1.getOrElse(""),
      line2 = data.officerHomeAddress.line2.getOrElse(""),
      line3 = data.officerHomeAddress.line3,
      line4 = data.officerHomeAddress.line4,
      postcode = data.officerHomeAddress.postcode,
      country = data.officerHomeAddress.country)

    val dateOfBirth = LocalDate.of(
      data.vatLodgingOfficer.dobYear.getOrElse("1900").toInt,
      data.vatLodgingOfficer.dobMonth.getOrElse("1").toInt,
      data.vatLodgingOfficer.dobDay.getOrElse("1").toInt
    )

    val officer = Officer(name = Name(
      forename = data.vatLodgingOfficer.firstname,
      otherForenames = data.vatLodgingOfficer.othernames,
      surname = data.vatLodgingOfficer.surname.getOrElse("")),
      role = data.vatLodgingOfficer.role.getOrElse("")
    )

    S4LVatLodgingOfficer(
      officerHomeAddress = Some(OfficerHomeAddressView(address.id, Some(address))),
      officerDateOfBirth = Some(OfficerDateOfBirthView(dateOfBirth)),
      officerNino = Some(OfficerNinoView(data.vatLodgingOfficer.nino.getOrElse(""))),
      completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
      officerContactDetails = None
    )
  }

}