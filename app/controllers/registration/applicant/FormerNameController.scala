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
import forms.FormerNameForm
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.former_name

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FormerNameController @Inject()(val authConnector: AuthConnector,
                                     val keystoreConnector: KeystoreConnector,
                                     val applicantDetailsService: ApplicantDetailsService,
                                     formerNamePage: former_name
                                    )(implicit appConfig: FrontendAppConfig,
                                      val executionContext: ExecutionContext,
                                      baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          applicant <- applicantDetailsService.getApplicantDetails
          filledForm = applicant.formerName.fold(FormerNameForm.form)(FormerNameForm.form.fill)
        } yield
          Ok(formerNamePage(filledForm))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        FormerNameForm.form.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(formerNamePage(badForm))),
          data => applicantDetailsService.saveApplicantDetails(data) map { _ =>
            if (data.yesNo) {
              Redirect(applicantRoutes.FormerNameDateController.show())
            }
            else {
              Redirect(applicantRoutes.HomeAddressController.redirectToAlf())
            }
          }
        )
  }

}
