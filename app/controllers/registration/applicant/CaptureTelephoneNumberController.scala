/*
 * Copyright 2021 HM Revenue & Customs
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

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.TelephoneNumberForm
import javax.inject.{Inject, Singleton}
import models.TelephoneNumber
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.capture_telephone_number

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureTelephoneNumberController @Inject()(view: capture_telephone_number,
                                                 val authConnector: AuthConnector,
                                                 val keystoreConnector: KeystoreConnector,
                                                 applicantDetailsService: ApplicantDetailsService
                                                )(implicit appConfig: FrontendAppConfig,
                                                  val executionContext: ExecutionContext,
                                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          applicant <- applicantDetailsService.getApplicantDetails
          filledForm = applicant.telephoneNumber.fold(TelephoneNumberForm.form)(tn => TelephoneNumberForm.form.fill(tn.telephone))
        } yield
          Ok(view(routes.CaptureTelephoneNumberController.submit(), filledForm))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        TelephoneNumberForm.form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(routes.CaptureTelephoneNumberController.submit(), formWithErrors))),
          telephone =>
            applicantDetailsService.saveApplicantDetails(TelephoneNumber(telephone)).map {
              _ => Redirect(controllers.registration.business.routes.PpobAddressController.startJourney())
            }
        )
  }

}
