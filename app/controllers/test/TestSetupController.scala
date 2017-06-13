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
import models.{S4LKey, S4LVatContact, S4LVatFinancials, S4LVatLodgingOfficer, S4LVatSicAndCompliance, _}
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

      vatFinancials <- s4LService.fetchAndGet[S4LVatFinancials]()
      sicStub <- s4LService.fetchAndGet[SicStub]()
      vatSicAndCompliance <- s4LService.fetchAndGet[S4LVatSicAndCompliance]()
      tradingDetails <- s4LService.fetchAndGet[S4LTradingDetails]()
      vatContact <- s4LService.fetchAndGet[S4LVatContact]()
      vatLodgingOfficer <- s4LService.fetchAndGet[S4LVatLodgingOfficer]()
      eligibility <- s4LService.fetchAndGet[S4LVatEligibility]()
      ppob <- s4LService.fetchAndGet[S4LPpob]()
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
        VatPpobSetup(
          line1 = ppob.flatMap(_.address).flatMap(_.address).map(_.line1),
          line2 = ppob.flatMap(_.address).flatMap(_.address).map(_.line2),
          line3 = ppob.flatMap(_.address).flatMap(_.address).flatMap(_.line3),
          line4 = ppob.flatMap(_.address).flatMap(_.address).flatMap(_.line4),
          postcode = ppob.flatMap(_.address).flatMap(_.address).flatMap(_.postcode),
          country = ppob.flatMap(_.address).flatMap(_.address).flatMap(_.country)),
        VatContactTestSetup(
          email = vatContact.flatMap(_.businessContactDetails).map(_.email),
          daytimePhone = vatContact.flatMap(_.businessContactDetails).flatMap(_.daytimePhone),
          mobile = vatContact.flatMap(_.businessContactDetails).flatMap(_.mobile),
          website = vatContact.flatMap(_.businessContactDetails).flatMap(_.website)
        ),
        VatFinancialsTestSetup(
          vatFinancials.flatMap(_.companyBankAccount).map(_.yesNo),
          vatFinancials.flatMap(_.companyBankAccountDetails).map(_.accountName),
          vatFinancials.flatMap(_.companyBankAccountDetails).map(_.accountNumber),
          vatFinancials.flatMap(_.companyBankAccountDetails).map(_.sortCode),
          vatFinancials.flatMap(_.estimateVatTurnover).map(_.vatTurnoverEstimate.toString),
          vatFinancials.flatMap(_.zeroRatedTurnover).map(_.yesNo),
          vatFinancials.flatMap(_.zeroRatedTurnoverEstimate).map(_.zeroRatedTurnoverEstimate.toString),
          vatFinancials.flatMap(_.vatChargeExpectancy).map(_.yesNo),
          vatFinancials.flatMap(_.vatReturnFrequency).map(_.frequencyType),
          vatFinancials.flatMap(_.accountingPeriod).map(_.accountingPeriod)),
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
          haveNino = eligibility.flatMap(_.vatEligibility).map(_.haveNino.getOrElse("").toString),
          doingBusinessAbroad = eligibility.flatMap(_.vatEligibility).map(_.doingBusinessAbroad.getOrElse("").toString),
          doAnyApplyToYou = eligibility.flatMap(_.vatEligibility).map(_.doAnyApplyToYou.getOrElse("").toString),
          applyingForAnyOf = eligibility.flatMap(_.vatEligibility).map(_.applyingForAnyOf.getOrElse("").toString),
          companyWillDoAnyOf = eligibility.flatMap(_.vatEligibility).map(_.companyWillDoAnyOf.getOrElse("").toString)),
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
            _ <- saveToS4Later(data.sicAndCompliance.sicCode1, data, { x =>
              SicStub(Some(x.sicAndCompliance.sicCode1.getOrElse("")),
                Some(x.sicAndCompliance.sicCode2.getOrElse("")),
                Some(x.sicAndCompliance.sicCode3.getOrElse("")),
                Some(x.sicAndCompliance.sicCode4.getOrElse("")))
            })

            _ <- s4LService.save(s4LBuilder.vatSicAndComplianceFromData(data))

            _ <- saveToS4Later(data.vatServiceEligibility.haveNino, data, { x =>
              S4LVatEligibility(Some(VatServiceEligibility(x.vatServiceEligibility.haveNino.map(_.toBoolean),
                x.vatServiceEligibility.doingBusinessAbroad.map(_.toBoolean),
                x.vatServiceEligibility.doAnyApplyToYou.map(_.toBoolean),
                x.vatServiceEligibility.applyingForAnyOf.map(_.toBoolean),
                x.vatServiceEligibility.companyWillDoAnyOf.map(_.toBoolean))))
            })

            _ <- s4LService.save(s4LBuilder.vatFinancialsFromData(data))
            _ <- s4LService.save(s4LBuilder.tradingDetailsFromData(data))
            _ <- s4LService.save(s4LBuilder.vatContactFromData(data))
            _ <- s4LService.save(s4LBuilder.vatPpobFormData(data))

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

}