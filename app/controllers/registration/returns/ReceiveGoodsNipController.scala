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

package controllers.registration.returns

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.ReceiveGoodsNipForm
import models.api.NETP
import models.{ConditionalValue, NIPCompliance}
import play.api.mvc.{Action, AnyContent}
import services._
import views.html.returns.ReceiveGoodsNip

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReceiveGoodsNipController @Inject()(val keystoreConnector: KeystoreConnector,
                                          val authConnector: AuthClientConnector,
                                          val applicantDetailsService: ApplicantDetailsService,
                                          val returnsService: ReturnsService,
                                          val vatRegistrationService: VatRegistrationService,
                                          view: ReceiveGoodsNip)
                                         (implicit appConfig: FrontendAppConfig,
                                          val executionContext: ExecutionContext,
                                          baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        returnsService.getReturns.map { returns =>
          returns.northernIrelandProtocol match {
            case Some(NIPCompliance(_, Some(ConditionalValue(receiveGoods, amount)))) => Ok(view(ReceiveGoodsNipForm.form.fill(receiveGoods, amount)))
            case _ => Ok(view(ReceiveGoodsNipForm.form))
          }
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        ReceiveGoodsNipForm.form.bindFromRequest.fold(
          badForm => Future.successful(BadRequest(view(badForm))),
          successForm => {
            val (receiveGoods, amount) = successForm
            for {
              returns <- returnsService.getReturns
              updatedReturns = returns.copy(
                northernIrelandProtocol = Some(NIPCompliance(returns.northernIrelandProtocol.flatMap(_.goodsToEU), Some(ConditionalValue(receiveGoods,amount)))
                )
              )
              _ <- returnsService.submitReturns(updatedReturns)
              v <- returnsService.isVoluntary
              pt <- vatRegistrationService.partyType
            } yield {
              (v, pt) match {
                case (_, NETP) => Redirect(controllers.registration.returns.routes.ReturnsController.returnsFrequencyPage())
                case (true, _) => Redirect(controllers.registration.returns.routes.ReturnsController.voluntaryStartPage())
                case (false, _) => Redirect(controllers.registration.returns.routes.ReturnsController.mandatoryStartPage())
              }
            }
          }
        )
  }
}