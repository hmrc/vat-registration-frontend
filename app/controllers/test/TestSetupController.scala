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
import models.view.test._
import models.view.vatFinancials._
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatFinancials.vatBankAccount.{CompanyBankAccount, CompanyBankAccountDetails}
import models._
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

      sicStub <- s4LService.fetchAndGet[SicStub]()

      vatSicAndCompliance <- s4LService.fetchAndGet[S4LVatSicAndCompliance]()

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

            _ <- saveToS4Later(data.sicAndCompliance.sicCode1, data, { x =>
              SicStub(Some(x.sicAndCompliance.sicCode1.getOrElse("")),
                Some(x.sicAndCompliance.sicCode2.getOrElse("")),
                Some(x.sicAndCompliance.sicCode3.getOrElse("")),
                Some(x.sicAndCompliance.sicCode4.getOrElse("")))
            })

            _ <- s4LService.save(s4LBuilder.vatSicAndComplianceFromData(data))

            _ <- saveToS4Later(data.vatServiceEligibility.haveNino, data, { x =>
              VatServiceEligibility(x.vatServiceEligibility.haveNino.map(_.toBoolean),
                x.vatServiceEligibility.doingBusinessAbroad.map(_.toBoolean),
                x.vatServiceEligibility.doAnyApplyToYou.map(_.toBoolean),
                x.vatServiceEligibility.applyingForAnyOf.map(_.toBoolean),
                x.vatServiceEligibility.companyWillDoAnyOf.map(_.toBoolean))
            })

            _ <- s4LService.save(s4LBuilder.tradingDetailsFromData(data))
            _ <- s4LService.save(s4LBuilder.vatContactFromData(data))

            vatLodgingOfficer = s4LBuilder.vatLodgingOfficerFromData(data)
            _ <- s4LService.save(vatLodgingOfficer)

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
        case (Some(_), None, None) => S4LVatSicAndCompliance(
          notForProfit = Some(NotForProfit(base.culturalNotForProfit.get)))
        case(None, Some(_), None) => S4LVatSicAndCompliance(
          companyProvideWorkers = base.labourCompanyProvideWorkers.flatMap(x => Some(CompanyProvideWorkers(x))),
          workers = base.labourWorkers.flatMap(x => Some(Workers(x.toInt))),
          temporaryContracts = base.labourTemporaryContracts.flatMap(x => Some(TemporaryContracts(x))),
          skilledWorkers = base.labourSkilledWorkers.flatMap(x => Some(SkilledWorkers(x))))
        case(None, None, Some(_)) => S4LVatSicAndCompliance(
          adviceOrConsultancy = base.financialAdviceOrConsultancy.flatMap(x => Some(AdviceOrConsultancy(x.toBoolean))),
          actAsIntermediary = base.financialActAsIntermediary.flatMap(x => Some(ActAsIntermediary(x.toBoolean))),
          chargeFees = base.financialChargeFees.flatMap(x => Some(ChargeFees(x.toBoolean))),
          leaseVehicles = base.financialLeaseVehiclesOrEquipment.flatMap(x => Some(LeaseVehicles(x.toBoolean))),
          additionalNonSecuritiesWork = base.financialAdditionalNonSecuritiesWork.flatMap(x => Some(AdditionalNonSecuritiesWork(x.toBoolean))),
          discretionaryInvestmentManagementServices =
            base.financialDiscretionaryInvestment.flatMap(x => Some(DiscretionaryInvestmentManagementServices(x.toBoolean))),
          investmentFundManagement = base.financialInvestmentFundManagement.flatMap(x => Some(InvestmentFundManagement(x.toBoolean))),
          manageAdditionalFunds = base.financialManageAdditionalFunds.flatMap(x => Some(ManageAdditionalFunds(x.toBoolean))))
        case (_, _, _) => S4LVatSicAndCompliance()
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