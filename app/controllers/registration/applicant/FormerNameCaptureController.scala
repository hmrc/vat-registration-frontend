/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.BaseController
import controllers.registration.applicant.{routes => applicantRoutes}
import forms.FormerNameCaptureForm
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.FormerNameCapture

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FormerNameCaptureController @Inject()(val authConnector: AuthConnector,
                                            val sessionService: SessionService,
                                            val applicantDetailsService: ApplicantDetailsService,
                                            formerNameCapturePage: FormerNameCapture
                                    )(implicit appConfig: FrontendAppConfig,
                                      val executionContext: ExecutionContext,
                                      baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          applicant <- applicantDetailsService.getApplicantDetails
          filledForm = applicant.formerName.fold(FormerNameCaptureForm.form)(FormerNameCaptureForm.form.fill)
          name <- applicantDetailsService.getTransactorApplicantName
        } yield Ok(formerNameCapturePage(filledForm, name))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        FormerNameCaptureForm.form.bindFromRequest().fold(
          badForm =>
            applicantDetailsService.getTransactorApplicantName.map { name =>
              BadRequest(formerNameCapturePage(badForm, name))
            },
          data => applicantDetailsService.saveApplicantDetails(data) flatMap { _ =>
            Future.successful(Redirect(applicantRoutes.FormerNameDateController.show))
          }
        )
  }

}
