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

package controllers.registration.business

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.SoleTraderNameForm
import models.api.{NETP, NonUkNonEstablished}
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile, TradingDetailsService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.soletrader_name

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class MandatoryTradingNameController @Inject()(val keystoreConnector: KeystoreConnector,
                                               val authConnector: AuthConnector,
                                               val applicantDetailsService: ApplicantDetailsService,
                                               val tradingDetailsService: TradingDetailsService,
                                               val vatRegistrationService: VatRegistrationService,
                                               view: soletrader_name
                                              )(implicit val appConfig: FrontendAppConfig,
                                                val executionContext: ExecutionContext,
                                                baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          tradingDetails <- tradingDetailsService.getTradingDetailsViewModel(profile.registrationId)
          form = tradingDetails.tradingNameView.fold(SoleTraderNameForm.form)(tradingName => SoleTraderNameForm.form.fill(tradingName.tradingName.get))
        } yield Ok(view(form))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        SoleTraderNameForm.form.bindFromRequest().fold(
          errors =>
            for {
              _ <- tradingDetailsService.getTradingDetailsViewModel(profile.registrationId)
            } yield BadRequest(view(errors)),
          success => {
            val name = success
            tradingDetailsService.saveTradingName(profile.registrationId, true, Some(name)) flatMap {
              _ =>
                vatRegistrationService.partyType.map {
                  case NETP | NonUkNonEstablished => Redirect(controllers.registration.returns.routes.ZeroRatedSuppliesController.show())
                  case _ => Redirect(controllers.registration.business.routes.ApplyForEoriController.show())
                }
            }
          }
        )
  }
}
