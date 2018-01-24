/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}

import connectors.{KeystoreConnect, S4LConnect}
import controllers.{CommonPlayDependencies, VatRegistrationController}
import features.businessContact.models.BusinessContact
import features.officer.models.view.LodgingOfficer
import features.turnoverEstimates.TurnoverEstimatesService
import forms.test.TestSetupForm
import models.view.test._
import models.{S4LKey, S4LVatFinancials, S4LVatSicAndCompliance, _}
import play.api.libs.json.Format
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService, SessionProfile}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

@Singleton
class TestSetupController @Inject()(implicit val s4LService: S4LService,
                                    val s4lConnector: S4LConnect,
                                    vatRegistrationService: RegistrationService,
                                    s4LBuilder: TestS4LBuilder,
                                    ds: CommonPlayDependencies,
                                    val authConnector: AuthConnector,
                                    val keystoreConnector: KeystoreConnect,
                                    val turnoverService: TurnoverEstimatesService) extends VatRegistrationController(ds) with SessionProfile {

  private val empty = Future.successful(CacheMap("", Map.empty))

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          for {
            vatFinancials       <- s4LService.fetchAndGet[S4LVatFinancials]
            sicStub             <- s4LService.fetchAndGet[SicStub]
            vatSicAndCompliance <- s4LService.fetchAndGet[S4LVatSicAndCompliance]
            businessContact     <- s4lConnector.fetchAndGet[BusinessContact](profile.registrationId, "business-contact")
            lodgingOfficer      <- s4LService.fetchAndGet[LodgingOfficer]
            frs                 <- s4LService.fetchAndGet[S4LFlatRateScheme]
            bankAccount         <- s4LService.fetchAndGetNoAux(S4LKey.bankAccountKey)
            returns             <- s4LService.fetchAndGetNoAux(S4LKey.returns)
            turnoverEstimates   <- turnoverService.fetchTurnoverEstimates
            tradingDetails      <- s4LService.fetchAndGetNoAux(S4LKey.tradingDetails)
            testSetup = TestSetup(
              VatContactTestSetup(
                email        = businessContact.flatMap(_.companyContactDetails).map(_.email),
                daytimePhone = businessContact.flatMap(_.companyContactDetails).flatMap(_.phoneNumber),
                mobile       = businessContact.flatMap(_.companyContactDetails).flatMap(_.mobileNumber),
                website      = businessContact.flatMap(_.companyContactDetails).flatMap(_.websiteAddress),
                line1        = businessContact.flatMap(_.ppobAddress).map(_.line1),
                line2        = businessContact.flatMap(_.ppobAddress).map(_.line2),
                line3        = businessContact.flatMap(_.ppobAddress).flatMap(_.line3),
                line4        = businessContact.flatMap(_.ppobAddress).flatMap(_.line4),
                postcode     = businessContact.flatMap(_.ppobAddress).flatMap(_.postcode),
                country      = businessContact.flatMap(_.ppobAddress).flatMap(_.country)
              ),
              VatFinancialsTestSetup(
                vatFinancials.flatMap(_.zeroRatedTurnover).map(_.yesNo),
                vatFinancials.flatMap(_.zeroRatedTurnoverEstimate).map(_.zeroRatedTurnoverEstimate.toString)
              ),
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
                financialManageAdditionalFunds = vatSicAndCompliance.flatMap(_.manageAdditionalFunds.map(_.yesNo.toString)),
                mainBusinessActivityId = vatSicAndCompliance.flatMap(_.mainBusinessActivity).flatMap(_.mainBusinessActivity).map(_.id),
                mainBusinessActivityDescription = vatSicAndCompliance.flatMap(_.mainBusinessActivity).flatMap(_.mainBusinessActivity).map(_.description),
                mainBusinessActivityDisplayDetails = vatSicAndCompliance.flatMap(_.mainBusinessActivity).flatMap(_.mainBusinessActivity).map(_.displayDetails)
              ),
              officerHomeAddress = OfficerHomeAddressTestSetup(
                line1 = lodgingOfficer.flatMap(_.homeAddress).flatMap(_.address).map(_.line1),
                line2 = lodgingOfficer.flatMap(_.homeAddress).flatMap(_.address).map(_.line2),
                line3 = lodgingOfficer.flatMap(_.homeAddress).flatMap(_.address).flatMap(_.line3),
                line4 = lodgingOfficer.flatMap(_.homeAddress).flatMap(_.address).flatMap(_.line4),
                postcode = lodgingOfficer.flatMap(_.homeAddress).flatMap(_.address).flatMap(_.postcode),
                country = lodgingOfficer.flatMap(_.homeAddress).flatMap(_.address).flatMap(_.country)),
              officerPreviousAddress = OfficerPreviousAddressTestSetup(
                threeYears = lodgingOfficer.flatMap(_.previousAddress).map(_.yesNo.toString),
                line1 = lodgingOfficer.flatMap(_.previousAddress).flatMap(_.address).map(_.line1),
                line2 = lodgingOfficer.flatMap(_.previousAddress).flatMap(_.address).map(_.line2),
                line3 = lodgingOfficer.flatMap(_.previousAddress).flatMap(_.address).flatMap(_.line3),
                line4 = lodgingOfficer.flatMap(_.previousAddress).flatMap(_.address).flatMap(_.line4),
                postcode = lodgingOfficer.flatMap(_.previousAddress).flatMap(_.address).flatMap(_.postcode),
                country = lodgingOfficer.flatMap(_.previousAddress).flatMap(_.address).flatMap(_.country)),
              lodgingOfficer = LodgingOfficerTestSetup(
                dobDay = lodgingOfficer.flatMap(_.securityQuestions).map(_.dob.getDayOfMonth.toString),
                dobMonth = lodgingOfficer.flatMap(_.securityQuestions).map(_.dob.getMonthValue.toString),
                dobYear = lodgingOfficer.flatMap(_.securityQuestions).map(_.dob.getYear.toString),
                nino = lodgingOfficer.flatMap(_.securityQuestions).map(_.nino),
                role = lodgingOfficer.flatMap(_.completionCapacity).flatMap(_.officer).map(_.role),
                firstname = lodgingOfficer.flatMap(_.completionCapacity).flatMap(_.officer).flatMap(_.name.forename),
                othernames = lodgingOfficer.flatMap(_.completionCapacity).flatMap(_.officer).flatMap(_.name.otherForenames),
                surname = lodgingOfficer.flatMap(_.completionCapacity).flatMap(_.officer).map(_.name.surname),
                email = lodgingOfficer.flatMap(_.contactDetails).flatMap(_.email),
                mobile = lodgingOfficer.flatMap(_.contactDetails).flatMap(_.daytimePhone),
                phone = lodgingOfficer.flatMap(_.contactDetails).flatMap(_.mobile),
                formernameChoice = lodgingOfficer.flatMap(_.formerName).map(_.yesNo.toString),
                formername = lodgingOfficer.flatMap(_.formerName).flatMap(_.formerName),
                formernameChangeDay = lodgingOfficer.flatMap(_.formerNameDate).map(_.date.getDayOfMonth.toString),
                formernameChangeMonth = lodgingOfficer.flatMap(_.formerNameDate).map(_.date.getMonthValue.toString),
                formernameChangeYear = lodgingOfficer.flatMap(_.formerNameDate).map(_.date.getYear.toString)
              ),
              vatFlatRateScheme = VatFlatRateSchemeTestSetup(
                joinFrs = frs.flatMap(_.joinFrs).map(_.selection.toString),
                annualCostsInclusive = frs.flatMap(_.annualCostsInclusive).map(_.selection),
                annualCostsLimited = frs.flatMap(_.annualCostsLimited).map(_.selection),
                registerForFrs = frs.flatMap(_.registerForFrs).map(_.selection.toString),
                frsStartDateChoice = frs.flatMap(_.frsStartDate).map(_.dateType),
                frsStartDateDay = frs.flatMap(_.frsStartDate).flatMap(_.date).map(_.getDayOfMonth.toString),
                frsStartDateMonth = frs.flatMap(_.frsStartDate).flatMap(_.date).map(_.getMonthValue.toString),
                frsStartDateYear = frs.flatMap(_.frsStartDate).flatMap(_.date).map(_.getYear.toString)
              ),
              turnoverEstimatesBlock = turnoverEstimates,
              bankAccountBlock = bankAccount,
              returnsBlock = returns,
              tradingDetailsBlock = tradingDetails
            )
            form = TestSetupForm.form.fill(testSetup)
          } yield Ok(views.html.pages.test.test_setup(form))
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          def saveToS4Later[T: Format : S4LKey](userEntered: Option[String], data: TestSetup, f: TestSetup => T): Future[Unit] =
            userEntered.map(_ => s4LService.save(f(data)).map(_ => ())).getOrElse(Future.successful(()))

          def saveToS4L[T: Format: S4LKey](data: T) = s4LService.save(data)

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
                  _ <- s4LService.save(s4LBuilder.vatFinancialsFromData(data))
                  _ <- s4LService.saveNoAux(s4LBuilder.tradingDetailsFromData(data), S4LKey.tradingDetails)
                  _ <- s4lConnector.save[BusinessContact](profile.registrationId, "business-contact", s4LBuilder.vatContactFromData(data))

                  lodgingOfficer = s4LBuilder.buildLodgingOfficerFromTestData(data)
                  _ <- s4LService.save(lodgingOfficer)

                  _ <- s4LService.save(s4LBuilder.vatFrsFromData(data))

                  _ <- data.bankAccountBlock.fold(empty)(saveToS4L)
                  _ <- data.turnoverEstimatesBlock.fold(empty)(t => turnoverService.saveTurnoverEstimates(t).flatMap(_ => empty))
                } yield Ok("Test setup complete")
              }
            })
        }
  }

}
