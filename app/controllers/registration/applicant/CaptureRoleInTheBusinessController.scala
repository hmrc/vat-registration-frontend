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
import forms.RoleInTheBusinessForm

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile, SessionService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.role_in_the_business

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureRoleInTheBusinessController @Inject()(view: role_in_the_business,
                                                   val authConnector: AuthConnector,
                                                   val sessionService: SessionService,
                                                   val applicantDetailsService: ApplicantDetailsService,
                                                   val vatRegistrationService: VatRegistrationService)
                                                  (implicit appConfig: FrontendAppConfig,
                                                   val executionContext: ExecutionContext,
                                                   baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          applicant <- applicantDetailsService.getApplicantDetails
          filledForm = applicant.roleInTheBusiness.fold(RoleInTheBusinessForm())(RoleInTheBusinessForm().fill)
          name <- applicantDetailsService.getTransactorApplicantName
        } yield Ok(view(filledForm, name))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        RoleInTheBusinessForm().bindFromRequest().fold(
          formWithErrors =>
            applicantDetailsService.getTransactorApplicantName.map { name =>
              BadRequest(view(formWithErrors, name))
            },
          roleInTheBusiness =>
            applicantDetailsService.saveApplicantDetails(roleInTheBusiness).map { _ =>
              Redirect(routes.FormerNameController.show)
            }
        )
  }
}
