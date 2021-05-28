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
import controllers.registration.applicant.{routes => applicantRoutes}
import forms.FormerNameDateForm
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.ExecutionContext

@Singleton
class FormerNameDateController @Inject()(val authConnector: AuthConnector,
                                         val keystoreConnector: KeystoreConnector,
                                         val applicantDetailsService: ApplicantDetailsService)
                                        (implicit appConfig: FrontendAppConfig,
                                         val executionContext: ExecutionContext,
                                         baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          applicant <- applicantDetailsService.getApplicantDetails
          dob = applicant.transactor.map(_.dateOfBirth).getOrElse(throw new IllegalStateException("Missing date of birth"))
          formerName = applicant.formerName.flatMap(_.formerName).getOrElse(throw new IllegalStateException("Missing applicant former name"))
          filledForm = applicant.formerNameDate.fold(FormerNameDateForm.form(dob))(FormerNameDateForm.form(dob).fill)
        } yield Ok(views.html.former_name_date(filledForm, formerName))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        applicantDetailsService.getApplicantDetails flatMap {
          applicantDetails =>
            val dob = applicantDetails.transactor.map(_.dateOfBirth).getOrElse(throw new IllegalStateException("Missing date of birth"))
            FormerNameDateForm.form(dob).bindFromRequest().fold(
              badForm => for {
                applicant <- applicantDetailsService.getApplicantDetails
                formerName = applicant.formerName.flatMap(_.formerName).getOrElse(throw new IllegalStateException("Missing applicant former name"))
              } yield BadRequest(views.html.former_name_date(badForm, formerName)),
              data => applicantDetailsService.saveApplicantDetails(data) map {
                _ => Redirect(applicantRoutes.HomeAddressController.redirectToAlf())
              }
            )
        }
  }

}
