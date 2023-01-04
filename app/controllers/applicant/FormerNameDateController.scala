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
import forms.FormerNameDateForm
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.applicant.former_name_date

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FormerNameDateController @Inject()(val authConnector: AuthConnector,
                                         val sessionService: SessionService,
                                         val applicantDetailsService: ApplicantDetailsService,
                                         formerNameDatePage: former_name_date
                                        )(implicit appConfig: FrontendAppConfig,
                                          val executionContext: ExecutionContext,
                                          baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          applicant <- applicantDetailsService.getApplicantDetails
          dob = applicant.personalDetails.flatMap(_.dateOfBirth).getOrElse(throw new IllegalStateException("Missing date of birth"))
          formerName = applicant.changeOfName.name.getOrElse(throw new IllegalStateException("Missing applicant former name"))
          filledForm = applicant.changeOfName.change.fold(FormerNameDateForm.form(dob))(FormerNameDateForm.form(dob).fill)
          name <- applicantDetailsService.getApplicantNameForTransactorFlow
        } yield Ok(formerNameDatePage(filledForm, formerName.asLabel, name))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        applicantDetailsService.getApplicantDetails flatMap {
          applicantDetails =>
            val dob = applicantDetails.personalDetails.flatMap(_.dateOfBirth).getOrElse(throw new IllegalStateException("Missing date of birth"))
            FormerNameDateForm.form(dob).bindFromRequest().fold(
              badForm => for {
                name <- applicantDetailsService.getApplicantNameForTransactorFlow
                formerName = applicantDetails.changeOfName.name.getOrElse(throw new IllegalStateException("Missing applicant former name"))
              } yield BadRequest(formerNameDatePage(badForm, formerName.asLabel, name)),
              data => {
                applicantDetailsService.saveApplicantDetails(data) flatMap { _ =>
                  Future.successful(Redirect(controllers.routes.TaskListController.show))
                }
              }
            )
        }
  }

}
