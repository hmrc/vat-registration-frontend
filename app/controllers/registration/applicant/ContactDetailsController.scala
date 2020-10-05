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

package controllers.registration.applicant

import config.FrontendAppConfig
import connectors.KeystoreConnector
import controllers.BaseController
import forms.ContactDetailsForm
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ApplicantDetailsService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.{ExecutionContext, Future}
import controllers.registration.applicant.{routes => applicantRoutes}

@Singleton
class ContactDetailsController @Inject()(mcc: MessagesControllerComponents,
                                         val authConnector: AuthConnector,
                                         val keystoreConnector: KeystoreConnector,
                                         val applicantDetailsService: ApplicantDetailsService)
                                        (implicit val appConfig: FrontendAppConfig,
                                         ec: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      for {
        applicant <- applicantDetailsService.getApplicantDetails
        filledForm = applicant.contactDetails.fold(ContactDetailsForm.form)(ContactDetailsForm.form.fill)
      } yield Ok(views.html.applicant_contact_details(filledForm))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ContactDetailsForm.form.bindFromRequest().fold(
        badForm =>
          Future.successful(BadRequest(views.html.applicant_contact_details(badForm))),
        data =>
          applicantDetailsService.saveApplicantDetails(data) map {
            _ => Redirect(applicantRoutes.HomeAddressController.redirectToAlf())
          }
      )
  }

}
