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

import javax.inject.Inject

import connectors.{KeystoreConnect, S4LConnect}
import controllers.VatRegistrationControllerNoAux
import features.businessContact.models.BusinessContact
import features.officer.models.view.LodgingOfficer
import features.sicAndCompliance.models.SicAndCompliance
import features.sicAndCompliance.models.test.SicStub
import features.turnoverEstimates.TurnoverEstimatesService
import forms.test.TestSetupForm
import models.view.test._
import models.{S4LKey, S4LVatFinancials}
import play.api.i18n.MessagesApi
import play.api.libs.json.Format
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, SessionProfile}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class TestSetupControllerImpl @Inject()(implicit val s4LService: S4LService,
                                        val s4lConnector: S4LConnect,
                                        val authConnector: AuthConnector,
                                        val keystoreConnector: KeystoreConnect,
                                        val turnoverService: TurnoverEstimatesService,
                                        implicit val messagesApi: MessagesApi) extends TestSetupController

trait TestSetupController extends VatRegistrationControllerNoAux with SessionProfile {
  val s4LBuilder = TestS4LBuilder

  val s4LService: S4LService
  val s4lConnector: S4LConnect
  val turnoverService: TurnoverEstimatesService

  private val empty = Future.successful(CacheMap("", Map.empty))

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          for {
            vatFinancials <- s4LService.fetchAndGet[S4LVatFinancials]
            sicStub <- s4LService.fetchAndGet[SicStub]
            vatSicAndCompliance <- s4LService.fetchAndGet[SicAndCompliance]
            lodgingOfficer <- s4LService.fetchAndGet[LodgingOfficer]
            businessContact     <- s4lConnector.fetchAndGet[BusinessContact](profile.registrationId, "business-contact")
            bankAccount       <- s4LService.fetchAndGetNoAux(S4LKey.bankAccountKey)
            flatRateScheme    <- s4LService.fetchAndGetNoAux(S4LKey.flatRateScheme)
            returns           <- s4LService.fetchAndGetNoAux(S4LKey.returns)
            turnoverEstimates <- turnoverService.fetchTurnoverEstimates
            tradingDetails    <- s4LService.fetchAndGetNoAux(S4LKey.tradingDetails)

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
                labourCompanyProvideWorkers = vatSicAndCompliance.flatMap(_.companyProvideWorkers.map(_.yesNo)),
                labourWorkers = vatSicAndCompliance.flatMap(_.workers.map(_.numberOfWorkers.toString)),
                labourTemporaryContracts = vatSicAndCompliance.flatMap(_.temporaryContracts.map(_.yesNo)),
                labourSkilledWorkers = vatSicAndCompliance.flatMap(_.skilledWorkers.map(_.yesNo)),
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
              flatRateSchemeBlock = flatRateScheme,
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
          implicit val flatRateKey = S4LKey.flatRateScheme

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
                  _ <- s4LService.save(s4LBuilder.vatFinancialsFromData(data))
                  _ <- s4LService.saveNoAux(s4LBuilder.tradingDetailsFromData(data), S4LKey.tradingDetails)
                  _ <- s4lConnector.save[BusinessContact](profile.registrationId, "business-contact", s4LBuilder.vatContactFromData(data))

                  lodgingOfficer = s4LBuilder.buildLodgingOfficerFromTestData(data)
                  _ <- s4LService.save(lodgingOfficer)

                  _ <- data.flatRateSchemeBlock.fold(empty)(x => s4LService.save(x))
                  _ <- data.bankAccountBlock.fold(empty)(x => s4LService.save(x))
                  _ <- data.turnoverEstimatesBlock.fold(empty)(t => turnoverService.saveTurnoverEstimates(t).flatMap(_ => empty))
                } yield Ok("Test setup complete")
              }
            })
        }
  }
}
