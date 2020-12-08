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

package controllers.registration.returns

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.{BaseController, routes => baseRoutes}
import forms.ChargeExpectancyForm
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent}
import services.{ReturnsService, SessionProfile}
import views.html.claim_refunds_view

import scala.concurrent.{ExecutionContext, Future}

class ClaimRefundsController @Inject()(val keystoreConnector: KeystoreConnector,
                                       val authConnector: AuthClientConnector,
                                       val returnsService: ReturnsService,
                                       val claimRefundsView: claim_refunds_view)
                                      (implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        returnsService.getReturns map { returns =>
          returns.reclaimVatOnMostReturns match {
            case Some(chargeExpectancy) => Ok(claimRefundsView(ChargeExpectancyForm.form.fill(chargeExpectancy)))
            case None => Ok(claimRefundsView(ChargeExpectancyForm.form))
          }
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        ChargeExpectancyForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(claimRefundsView(errors))),
          success => {
            for {
              _ <- returnsService.saveReclaimVATOnMostReturns(success)
              isVoluntary <- returnsService.getThreshold
            } yield {
              if (isVoluntary) {
                Redirect(baseRoutes.ReturnsController.voluntaryStartPage())
              }
              else {
                Redirect(baseRoutes.ReturnsController.mandatoryStartPage())
              }
            }
          }
        )
  }

}
