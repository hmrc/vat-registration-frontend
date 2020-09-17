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
import forms.FormerNameDateForm
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ApplicantDetailsService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.ExecutionContext
import controllers.registration.applicant.{routes => applicantRoutes}

@Singleton
class FormerNameDateController @Inject()(mcc: MessagesControllerComponents,
                                         val authConnector: AuthConnector,
                                         val keystoreConnector: KeystoreConnector,
                                         val applicantDetailsService: ApplicantDetailsService)
                                        (implicit val appConfig: FrontendAppConfig,
                                         ec: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          applicant <- applicantDetailsService.getApplicantDetails
          formerName = applicant.formerName.flatMap(_.formerName).getOrElse(throw new IllegalStateException("Missing applicant former name"))
          filledForm = applicant.formerNameDate.fold(FormerNameDateForm.form)(FormerNameDateForm.form.fill)
        } yield Ok(views.html.former_name_date(filledForm, formerName))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        applicantDetailsService.getApplicantDetails flatMap { applicant =>
          FormerNameDateForm.form.bindFromRequest().fold(
            badForm => for {
              applicant <- applicantDetailsService.getApplicantDetails
              formerName = applicant.formerName.flatMap(_.formerName).getOrElse(throw new IllegalStateException("Missing applicant former name"))
            } yield BadRequest(views.html.former_name_date(badForm, formerName)),
            data => applicantDetailsService.saveApplicantDetails(data) map {
              _ => Redirect(applicantRoutes.ContactDetailsController.show())
            }
          )
        }
  }

}
