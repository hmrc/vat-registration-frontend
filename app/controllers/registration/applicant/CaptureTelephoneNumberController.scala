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
import forms.TelephoneNumberForm
import javax.inject.{Inject, Singleton}
import models.TelephoneNumber
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ApplicantDetailsService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.capture_telephone_number

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureTelephoneNumberController @Inject()(view: capture_telephone_number,
                                                 mcc: MessagesControllerComponents,
                                                 val authConnector: AuthConnector,
                                                 val keystoreConnector: KeystoreConnector,
                                                 applicantDetailsService: ApplicantDetailsService
                                                )(implicit val appConfig: FrontendAppConfig,
                                                  val executionContext: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        Future.successful(
          Ok(view(routes.CaptureTelephoneNumberController.submit(), TelephoneNumberForm.form))
        )
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
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
