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

import config.AuthClientConnector
import connectors.KeystoreConnector
import deprecated.DeprecatedConstants
import forms.{EuGoodsForm, TradingNameForm}
import javax.inject.Inject
import play.api.i18n.MessagesApi
import services.{SessionProfile, TradingDetailsService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.{eu_goods => EuGoodsPage, trading_name => TradingNamePage}

import scala.concurrent.Future

class TradingDetailsControllerImpl @Inject()(val keystoreConnector: KeystoreConnector,
                                             val authConnector: AuthClientConnector,
                                             val tradingDetailsService: TradingDetailsService,
                                             val messagesApi: MessagesApi) extends TradingDetailsController

trait TradingDetailsController extends BaseController with SessionProfile {

  val tradingDetailsService: TradingDetailsService
  val authConnector: AuthConnector
  val keystoreConnector: KeystoreConnector

  val tradingNamePage = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          tradingDetailsView  <- tradingDetailsService.getTradingDetailsViewModel(profile.registrationId)
          form                =  TradingNameForm.fillWithPrePop(tradingDetailsView.tradingNameView)
        } yield Ok(TradingNamePage(form, DeprecatedConstants.fakeCompanyName))
      }
  }

  val submitTradingName = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
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
  }

  val euGoodsPage = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        tradingDetailsService.getTradingDetailsViewModel(profile.registrationId) map {
          _.euGoods match {
            case Some(goods) => Ok(EuGoodsPage(EuGoodsForm.form.fill(goods)))
            case None => Ok(EuGoodsPage(EuGoodsForm.form))
          }
        }
      }
  }

  val submitEuGoods = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        EuGoodsForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(EuGoodsPage(errors))),
          success => tradingDetailsService.saveEuGoods(profile.registrationId, success) map { _ =>
            Redirect(controllers.routes.ReturnsController.chargeExpectancyPage())
          }
        )
      }
  }
}