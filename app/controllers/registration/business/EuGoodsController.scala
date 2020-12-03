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

package controllers.registration.business

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.EuGoodsForm
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile, TradingDetailsService}
import views.html.{eu_goods => EuGoodsPage}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EuGoodsController @Inject()(val keystoreConnector: KeystoreConnector,
                                  val authConnector: AuthClientConnector,
                                  val applicantDetailsService: ApplicantDetailsService,
                                  val tradingDetailsService: TradingDetailsService)
                                 (implicit appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        tradingDetailsService.getTradingDetailsViewModel(profile.registrationId) map {
          _.euGoods match {
            case Some(goods) => Ok(EuGoodsPage(EuGoodsForm.form.fill(goods)))
            case None => Ok(EuGoodsPage(EuGoodsForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        EuGoodsForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(EuGoodsPage(errors))),
          success => tradingDetailsService.saveEuGoods(profile.registrationId, success) map { _ =>
            Redirect(controllers.routes.ZeroRatedSuppliesController.show())
          }
        )
  }
}