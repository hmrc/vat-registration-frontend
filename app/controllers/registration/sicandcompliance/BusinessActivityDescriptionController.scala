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

package controllers.registration.sicandcompliance

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.BusinessActivityDescriptionForm

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, SicAndComplianceService}
import views.html.business_activity_description

import scala.concurrent.{ExecutionContext, Future}

class BusinessActivityDescriptionController @Inject()(val authConnector: AuthClientConnector,
                                                      val sessionService: SessionService,
                                                      val sicAndCompService: SicAndComplianceService,
                                                      view: business_activity_description)
                                                     (implicit appConfig: FrontendAppConfig,
                                                      val executionContext: ExecutionContext,
                                                      baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          sicCompliance <- sicAndCompService.getSicAndCompliance
          formFilled = sicCompliance.description.fold(BusinessActivityDescriptionForm.form)(BusinessActivityDescriptionForm.form.fill)
        } yield Ok(view(formFilled))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        BusinessActivityDescriptionForm.form.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(view(badForm))),
          data => sicAndCompService.updateSicAndCompliance(data).map {
            _ => Redirect(controllers.registration.sicandcompliance.routes.SicAndComplianceController.showSicHalt)
          }
        )
  }

}
