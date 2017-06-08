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
import models.view.vatFinancials._
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatFinancials.vatBankAccount.{CompanyBankAccount, CompanyBankAccountDetails}
import models.{S4LKey, S4LTradingDetails, S4LVatContact, S4LVatLodgingOfficer}
import play.api.libs.json.Format
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, S4LService, VatRegistrationService}

import scala.concurrent.Future

class TestSetupController @Inject()(ds: CommonPlayDependencies)(implicit s4LService: S4LService,
                                                                vatRegistrationService: VatRegistrationService,
                                                                s4LBuilder: TestS4LBuilder)
  extends VatRegistrationController(ds) with CommonService {

  def show: Action[AnyContent] = authorised.async(body = implicit user => implicit request => {
    for {

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

      tradingDetails <- s4LService.fetchAndGet[S4LTradingDetails]()
      vatContact <- s4LService.fetchAndGet[S4LVatContact]()
      vatLodgingOfficer <- s4LService.fetchAndGet[S4LVatLodgingOfficer]()

      eligibility <- s4LService.fetchAndGet[VatServiceEligibility]()

      testSetup = TestSetup(
        VatChoiceTestSetup(
          taxableTurnoverChoice = tradingDetails.flatMap(_.taxableTurnover).map(_.yesNo),
          voluntaryChoice = tradingDetails.flatMap(_.voluntaryRegistration).map(_.yesNo),
          voluntaryRegistrationReason = tradingDetails.flatMap(_.voluntaryRegistrationReason).map(_.reason),
          startDateChoice = tradingDetails.flatMap(_.startDate).map(_.dateType),
          startDateDay = tradingDetails.flatMap(_.startDate).flatMap(_.date).map(_.getDayOfMonth.toString),
          startDateMonth = tradingDetails.flatMap(_.startDate).flatMap(_.date).map(_.getMonthValue.toString),
          startDateYear = tradingDetails.flatMap(_.startDate).flatMap(_.date).map(_.getYear.toString)
        ),
        VatTradingDetailsTestSetup(
          tradingNameChoice = tradingDetails.flatMap(_.tradingName).map(_.yesNo),
          tradingName = tradingDetails.flatMap(_.tradingName).flatMap(_.tradingName),
          euGoods = tradingDetails.flatMap(_.euGoods).map(_.yesNo),
          applyEori = tradingDetails.flatMap(_.applyEori).map(_.yesNo.toString)
        ),
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
    def saveToS4Later[T: Format : S4LKey](userEntered: Option[String], data: TestSetup, f: TestSetup => T): Future[Unit] =
      userEntered.map(_ => s4LService.save(f(data)).map(_ => ())).getOrElse(Future.successful(()))

    TestSetupForm.form.bindFromRequest().fold(
      badForm => {
        Future.successful(BadRequest(views.html.pages.test.test_setup(badForm)))
      }, {
        data: TestSetup => {
          for {
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

            tradingDetails = s4LBuilder.tradingDetailsFromData(data)
            _ <- s4LService.save(tradingDetails)

            vatContact = s4LBuilder.vatContactFromData(data)
            _ <- s4LService.save(vatContact)

            vatLodgingOfficer = s4LBuilder.vatLodgingOfficerFromData(data)
            _ <- s4LService.save(vatLodgingOfficer)

            // KeyStore hack
            officer = vatLodgingOfficer.completionCapacity.
              flatMap(ccv => ccv.completionCapacity.
                map(cc => Officer(cc.name, cc.role, None)))
            _ <- keystoreConnector.cache(REGISTERING_OFFICER_KEY, officer.getOrElse(Officer.empty))

          } yield Ok("Test setup complete")
        }
      })
  })


}