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

package controllers.test

import config.{AuthClientConnector, FrontendAppConfig}
import connectors.test.TestVatRegistrationConnector
import connectors.{KeystoreConnector, S4LConnector}
import controllers.BaseController
import forms.test.{TestSetupEligibilityForm, TestSetupForm}
import javax.inject.{Inject, Singleton}
import models._
import models.test.SicStub
import models.view.ApplicantDetails
import models.view.test._
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{S4LService, SessionProfile}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestSetupController @Inject()(mcc: MessagesControllerComponents,
                                    val s4LService: S4LService,
                                    val s4lConnector: S4LConnector,
                                    val authConnector: AuthClientConnector,
                                    val keystoreConnector: KeystoreConnector,
                                    val testVatRegConnector: TestVatRegistrationConnector)
                                   (implicit val appConfig: FrontendAppConfig,
                                    ec: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  val s4LBuilder: TestS4LBuilder.type = TestS4LBuilder

  private val empty = Future.successful(CacheMap("", Map.empty))

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          sicStub <- s4LService.fetchAndGet[SicStub]
          vatSicAndCompliance <- s4LService.fetchAndGet[SicAndCompliance]
          applicantDetails <- s4LService.fetchAndGet[ApplicantDetails]
          businessContact <- s4lConnector.fetchAndGet[BusinessContact](profile.registrationId, "business-contact")
          bankAccount <- s4LService.fetchAndGetNoAux(S4LKey.bankAccountKey)
          flatRateScheme <- s4LService.fetchAndGetNoAux(S4LKey.flatRateScheme)
          returns <- s4LService.fetchAndGetNoAux(S4LKey.returns)
          tradingDetails <- s4LService.fetchAndGet[TradingDetails]

          testSetup = TestSetup(
            VatContactTestSetup(
              email = businessContact.flatMap(_.companyContactDetails).map(_.email),
              daytimePhone = businessContact.flatMap(_.companyContactDetails).flatMap(_.phoneNumber),
              mobile = businessContact.flatMap(_.companyContactDetails).flatMap(_.mobileNumber),
              website = businessContact.flatMap(_.companyContactDetails).flatMap(_.websiteAddress),
              line1 = businessContact.flatMap(_.ppobAddress).map(_.line1),
              line2 = businessContact.flatMap(_.ppobAddress).map(_.line2),
              line3 = businessContact.flatMap(_.ppobAddress).flatMap(_.line3),
              line4 = businessContact.flatMap(_.ppobAddress).flatMap(_.line4),
              postcode = businessContact.flatMap(_.ppobAddress).flatMap(_.postcode),
              country = businessContact.flatMap(_.ppobAddress).flatMap(_.country)
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
              mainBusinessActivityId = vatSicAndCompliance.flatMap(_.mainBusinessActivity).flatMap(_.mainBusinessActivity).map(_.code),
              mainBusinessActivityDescription = vatSicAndCompliance.flatMap(_.mainBusinessActivity).flatMap(_.mainBusinessActivity).map(_.description),
              mainBusinessActivityDisplayDetails = vatSicAndCompliance.flatMap(_.mainBusinessActivity).flatMap(_.mainBusinessActivity).map(_.displayDetails)
            ),
            applicantHomeAddress = ApplicantHomeAddressTestSetup(
              line1 = applicantDetails.flatMap(_.homeAddress).flatMap(_.address).map(_.line1),
              line2 = applicantDetails.flatMap(_.homeAddress).flatMap(_.address).map(_.line2),
              line3 = applicantDetails.flatMap(_.homeAddress).flatMap(_.address).flatMap(_.line3),
              line4 = applicantDetails.flatMap(_.homeAddress).flatMap(_.address).flatMap(_.line4),
              postcode = applicantDetails.flatMap(_.homeAddress).flatMap(_.address).flatMap(_.postcode),
              country = applicantDetails.flatMap(_.homeAddress).flatMap(_.address).flatMap(_.country)),
            applicantPreviousAddress = ApplicantPreviousAddressTestSetup(
              threeYears = applicantDetails.flatMap(_.previousAddress).map(_.yesNo.toString),
              line1 = applicantDetails.flatMap(_.previousAddress).flatMap(_.address).map(_.line1),
              line2 = applicantDetails.flatMap(_.previousAddress).flatMap(_.address).map(_.line2),
              line3 = applicantDetails.flatMap(_.previousAddress).flatMap(_.address).flatMap(_.line3),
              line4 = applicantDetails.flatMap(_.previousAddress).flatMap(_.address).flatMap(_.line4),
              postcode = applicantDetails.flatMap(_.previousAddress).flatMap(_.address).flatMap(_.postcode),
              country = applicantDetails.flatMap(_.previousAddress).flatMap(_.address).flatMap(_.country)),
            applicantDetails = ApplicantDetailsTestSetup(
              email = applicantDetails.flatMap(_.contactDetails).flatMap(_.email),
              mobile = applicantDetails.flatMap(_.contactDetails).flatMap(_.daytimePhone),
              phone = applicantDetails.flatMap(_.contactDetails).flatMap(_.mobile),
              formernameChoice = applicantDetails.flatMap(_.formerName).map(_.yesNo.toString),
              formername = applicantDetails.flatMap(_.formerName).flatMap(_.formerName),
              formernameChangeDay = applicantDetails.flatMap(_.formerNameDate).map(_.date.getDayOfMonth.toString),
              formernameChangeMonth = applicantDetails.flatMap(_.formerNameDate).map(_.date.getMonthValue.toString),
              formernameChangeYear = applicantDetails.flatMap(_.formerNameDate).map(_.date.getYear.toString)
            ),
            flatRateSchemeBlock = flatRateScheme,
            bankAccountBlock = bankAccount,
            returnsBlock = returns,
            tradingDetailsBlock = tradingDetails
          )
          form = TestSetupForm.form.fill(testSetup)
        } yield Ok(views.html.pages.test.test_setup(form))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile => {
        implicit val flatRateKey: S4LKey[FlatRateScheme] = S4LKey.flatRateScheme

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
                _ <- s4LService.save(s4LBuilder.tradingDetailsFromData(data))
                _ <- s4lConnector.save[BusinessContact](profile.registrationId, "business-contact", s4LBuilder.vatContactFromData(data))

                applicantDetails = s4LBuilder.buildApplicantDetailsFromTestData(data)
                _ <- s4LService.save(applicantDetails)

                _ <- data.returnsBlock.fold(empty)(x => s4LService.save(x))
                _ <- data.flatRateSchemeBlock.fold(empty)(x => s4LService.save(x))
                _ <- data.bankAccountBlock.fold(empty)(x => s4LService.save(x))
              } yield Ok("Test setup complete")
            }
          }
        )
      }
  }

  def showEligibility: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        Future.successful(Ok(views.html.pages.test.test_setup_eligibility(TestSetupEligibilityForm.form)))
  }

  def submitEligibility: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        TestSetupEligibilityForm.form.bindFromRequest().fold(
          badForm => {
            Future.successful(BadRequest(views.html.pages.test.test_setup_eligibility(badForm)))
          }, { data: String =>
            testVatRegConnector.updateEligibilityData(Json.parse(data)) map (_ => Ok("Eligibility updated"))
          }
        )
  }
}
