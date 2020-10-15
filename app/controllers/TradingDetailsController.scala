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

package controllers

import config.{AuthClientConnector, FrontendAppConfig}
import connectors.KeystoreConnector
import deprecated.DeprecatedConstants
import forms.{EuGoodsForm, TradingNameForm}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{SessionProfile, TradingDetailsService}
import views.html.{eu_goods => EuGoodsPage, trading_name => TradingNamePage}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TradingDetailsController @Inject()(mcc: MessagesControllerComponents,
                                         val keystoreConnector: KeystoreConnector,
                                         val authConnector: AuthClientConnector,
                                         val tradingDetailsService: TradingDetailsService)
                                        (implicit val appConfig: FrontendAppConfig,
                                         ec: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  val tradingNamePage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          tradingDetailsView <- tradingDetailsService.getTradingDetailsViewModel(profile.registrationId)
          form = TradingNameForm.fillWithPrePop(tradingDetailsView.tradingNameView)
        } yield Ok(TradingNamePage(form, DeprecatedConstants.fakeCompanyName))
  }

  val submitTradingName: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        TradingNameForm.form.bindFromRequest.fold(
          errors => {
            Future.successful(BadRequest(TradingNamePage(errors, DeprecatedConstants.fakeCompanyName)))

          },
          success => {
            val (hasName, name) = success
            tradingDetailsService.saveTradingName(profile.registrationId, hasName, name) map {
              _ => Redirect(controllers.routes.TradingDetailsController.euGoodsPage())
            }
          }
        )
  }

  val euGoodsPage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        tradingDetailsService.getTradingDetailsViewModel(profile.registrationId) map {
          _.euGoods match {
            case Some(goods) => Ok(EuGoodsPage(EuGoodsForm.form.fill(goods)))
            case None => Ok(EuGoodsPage(EuGoodsForm.form))
          }
        }
  }

  val submitEuGoods: Action[AnyContent] = isAuthenticatedWithProfile {
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