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

package controllers.vatapplication

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.ReceiveGoodsNipForm
import models.{ConditionalValue, NIPTurnover}
import play.api.mvc.{Action, AnyContent}
import services.VatApplicationService.TurnoverFromEu
import services._
import views.html.vatapplication.ReceiveGoodsNip

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReceiveGoodsNipController @Inject()(val sessionService: SessionService,
                                          val authConnector: AuthClientConnector,
                                          val applicantDetailsService: ApplicantDetailsService,
                                          val vatApplicationService: VatApplicationService,
                                          val vatRegistrationService: VatRegistrationService,
                                          view: ReceiveGoodsNip)
                                         (implicit appConfig: FrontendAppConfig,
                                          val executionContext: ExecutionContext,
                                          baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication.map { vatApplication =>
          vatApplication.northernIrelandProtocol match {
            case Some(NIPTurnover(_, Some(ConditionalValue(receiveGoods, amount)))) => Ok(view(ReceiveGoodsNipForm.form.fill((receiveGoods, amount))))
            case _ => Ok(view(ReceiveGoodsNipForm.form))
          }
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        ReceiveGoodsNipForm.form.bindFromRequest.fold(
          badForm => Future.successful(BadRequest(view(badForm))),
          successForm => {
            val success = ConditionalValue(successForm._1, successForm._2)

            vatApplicationService.saveVatApplication(TurnoverFromEu(success)).map { _ =>
              Redirect(controllers.vatapplication.routes.ClaimRefundsController.show)
            }
          }
        )
  }
}
