/*
 * Copyright 2026 HM Revenue & Customs
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
import forms.RoleInTheBusinessForm
import play.api.mvc.{Action, AnyContent}
import services.ApplicantDetailsService.RoleInTheBusinessAnswer
import services.{ApplicantDetailsService, SessionProfile, SessionService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.applicant.RoleInTheBusiness

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureRoleInTheBusinessController @Inject()(view: RoleInTheBusiness,
                                                   val authConnector: AuthConnector,
                                                   val sessionService: SessionService,
                                                   val applicantDetailsService: ApplicantDetailsService,
                                                   val vatRegistrationService: VatRegistrationService)
                                                  (implicit appConfig: FrontendAppConfig,
                                                   val executionContext: ExecutionContext,
                                                   baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          applicant <- applicantDetailsService.getApplicantDetails
          roleInTheBusinessAnswer = applicant.roleInTheBusiness.map(role => RoleInTheBusinessAnswer(role, applicant.otherRoleInTheBusiness))
          partyType <- vatRegistrationService.getEligibilitySubmissionData.map(_.partyType)
          optName <- applicantDetailsService.getApplicantNameForTransactorFlow
          filledForm = roleInTheBusinessAnswer.fold(RoleInTheBusinessForm(partyType, optName.isDefined))(RoleInTheBusinessForm(partyType, optName.isDefined).fill)
        } yield Ok(view(filledForm, optName, partyType))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          partyType <- vatRegistrationService.getEligibilitySubmissionData.map(_.partyType)
          optName <- applicantDetailsService.getApplicantNameForTransactorFlow
          redirect <- RoleInTheBusinessForm(partyType, optName.isDefined).bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, optName, partyType))),
            roleInTheBusinessAnswer =>
              applicantDetailsService.saveApplicantDetails(roleInTheBusinessAnswer).map { _ =>
                Redirect(routes.FormerNameController.show)
              }
          )
        } yield redirect
  }
}
