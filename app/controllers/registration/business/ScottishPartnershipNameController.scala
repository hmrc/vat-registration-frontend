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

package controllers.registration.business

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.ScottishPartnershipNameForm
import play.api.mvc.{Action, AnyContent}
import services.SessionService.scottishPartnershipNameKey
import services.{ApplicantDetailsService, SessionProfile, SessionService}
import views.html.ScottishPartnershipName

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ScottishPartnershipNameController @Inject()(val sessionService: SessionService,
                                                  val authConnector: AuthClientConnector,
                                                  val applicantDetailsService: ApplicantDetailsService,
                                                  view: ScottishPartnershipName)
                                                 (implicit appConfig: FrontendAppConfig,
                                                  val executionContext: ExecutionContext,
                                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        sessionService.fetchAndGet[String](scottishPartnershipNameKey).map {
          case Some(companyName) => Ok(view(ScottishPartnershipNameForm().fill(companyName)))
          case None => Ok(view(ScottishPartnershipNameForm()))
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        ScottishPartnershipNameForm.apply().bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          companyName => {
            sessionService.cache[String](scottishPartnershipNameKey, companyName).map(_ =>
              Redirect(controllers.registration.applicant.routes.PartnershipIdController.startPartnerJourney)
            )
          }
        )
  }
}
