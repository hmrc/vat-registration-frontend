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
import forms.LandAndPropertyForm
import play.api.mvc.{Action, AnyContent}
import services.{LandAndPropertyAnswer, SessionProfile, SessionService, SicAndComplianceService}
import views.html.LandAndProperty

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LandAndPropertyController @Inject()(val sessionService: SessionService,
                                          val authConnector: AuthClientConnector,
                                          sicAndComplianceService: SicAndComplianceService,
                                          view: LandAndProperty)
                                         (implicit appConfig: FrontendAppConfig,
                                          val executionContext: ExecutionContext,
                                          baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        sicAndComplianceService.getSicAndCompliance.map {
          _.hasLandAndProperty match {
            case Some(answer) => Ok(view(LandAndPropertyForm.form.fill(answer)))
            case None => Ok(view(LandAndPropertyForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        LandAndPropertyForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(view(errors))),
          success => sicAndComplianceService.updateSicAndCompliance(LandAndPropertyAnswer(success)).map { _ =>
            Redirect(routes.BusinessActivityDescriptionController.show)
          }
        )
  }
}