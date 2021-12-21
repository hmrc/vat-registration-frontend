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

package controllers.registration.business

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.ShortOrgNameForm
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, TradingDetailsService}
import views.html.ShortOrgName

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ShortOrgNameController @Inject()(val sessionService: SessionService,
                                       val authConnector: AuthClientConnector,
                                       val tradingDetailsService: TradingDetailsService,
                                       view: ShortOrgName)
                                      (implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        tradingDetailsService.getTradingDetailsViewModel(profile.registrationId).map { tradingDetails =>
          val prepoppedForm = tradingDetails.shortOrgName.fold(ShortOrgNameForm())(ShortOrgNameForm().fill(_))

          Ok(view(prepoppedForm))
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        ShortOrgNameForm().bindFromRequest.fold(
          errors =>
            Future.successful(BadRequest(view(errors))),
          success => {
            for {
              _ <- tradingDetailsService.saveShortOrgName(success)
            } yield {
              Redirect(routes.TradingNameController.show)
            }
          }
        )
  }

}
