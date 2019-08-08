/*
 * Copyright 2019 HM Revenue & Customs
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
import features.tradingDetails.TradingDetailsService
import features.tradingDetails.views.html.{eu_goods => EuGoodsPage, trading_name => TradingNamePage}
import forms.{EuGoodsForm, TradingNameForm}
import javax.inject.Inject
import play.api.i18n.MessagesApi
import services.{IncorporationInformationService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

class TradingDetailsControllerImpl @Inject()(val keystoreConnector: KeystoreConnector,
                                             val authConnector: AuthClientConnector,
                                             val tradingDetailsService: TradingDetailsService,
                                             val messagesApi: MessagesApi,
                                             val incorpInfoService: IncorporationInformationService) extends TradingDetailsController

trait TradingDetailsController extends BaseController with SessionProfile {

  val tradingDetailsService: TradingDetailsService
  val authConnector: AuthConnector
  val keystoreConnector: KeystoreConnector
  val incorpInfoService: IncorporationInformationService

  val tradingNamePage = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          companyName         <- incorpInfoService.getCompanyName(profile.registrationId, profile.transactionId)
          tradingDetailsView  <- tradingDetailsService.getTradingDetailsViewModel(profile.registrationId)
          prepopTradingName   <- tradingDetailsService.getTradingNamePrepop(profile.registrationId, tradingDetailsView.tradingNameView)
          form                =  TradingNameForm.fillWithPrePop(prepopTradingName, tradingDetailsView.tradingNameView)
        } yield Ok(TradingNamePage(form, companyName))
      }
  }

  val submitTradingName = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        TradingNameForm.form.bindFromRequest.fold(
          errors => {
            incorpInfoService.getCompanyName(profile.registrationId, profile.transactionId) map { companyName =>
              BadRequest(TradingNamePage(errors, companyName))
            }
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
            Redirect(features.returns.controllers.routes.ReturnsController.chargeExpectancyPage())
          }
        )
      }
  }
}