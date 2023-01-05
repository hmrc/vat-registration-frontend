/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.applicant

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.TelephoneNumberForm
import play.api.mvc.{Action, AnyContent}
import services.ApplicantDetailsService.TelephoneNumber
import services.{ApplicantDetailsService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.applicant.capture_telephone_number

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureTelephoneNumberController @Inject()(view: capture_telephone_number,
                                                 val authConnector: AuthConnector,
                                                 val sessionService: SessionService,
                                                 applicantDetailsService: ApplicantDetailsService
                                                )(implicit appConfig: FrontendAppConfig,
                                                  val executionContext: ExecutionContext,
                                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          applicant <- applicantDetailsService.getApplicantDetails
          filledForm = applicant.contact.tel.fold(TelephoneNumberForm.form)(TelephoneNumberForm.form.fill)
          name <- applicantDetailsService.getApplicantNameForTransactorFlow
        } yield Ok(view(routes.CaptureTelephoneNumberController.submit, filledForm, name))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        TelephoneNumberForm.form.bindFromRequest().fold(
          formWithErrors =>
            applicantDetailsService.getApplicantNameForTransactorFlow.map { name =>
              BadRequest(view(routes.CaptureTelephoneNumberController.submit, formWithErrors, name))
            },
          telephone =>
            applicantDetailsService.saveApplicantDetails(TelephoneNumber(telephone)).map { _ =>
              Redirect(controllers.routes.TaskListController.show)
            }
        )
  }

}
