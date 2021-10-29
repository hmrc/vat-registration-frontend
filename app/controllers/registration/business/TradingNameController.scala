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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.TradingNameForm
import models.api.NonUkNonEstablished
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile, TradingDetailsService, VatRegistrationService}
import uk.gov.hmrc.http.InternalServerException
import views.html.trading_name

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TradingNameController @Inject()(val keystoreConnector: KeystoreConnector,
                                      val authConnector: AuthClientConnector,
                                      val applicantDetailsService: ApplicantDetailsService,
                                      val tradingDetailsService: TradingDetailsService,
                                      val vatRegistrationService: VatRegistrationService,
                                      view: trading_name)
                                     (implicit appConfig: FrontendAppConfig,
                                      val executionContext: ExecutionContext,
                                      baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          companyName <- applicantDetailsService.getCompanyName.map(_.getOrElse(throw new InternalServerException("Missing company name")))
          tradingDetailsView <- tradingDetailsService.getTradingDetailsViewModel(profile.registrationId)
          form = TradingNameForm.fillWithPrePop(tradingDetailsView.tradingNameView)
        } yield Ok(view(form, companyName))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        TradingNameForm.form.bindFromRequest.fold(
          errors =>
            for {
              companyName <- applicantDetailsService.getCompanyName.map(_.getOrElse(throw new InternalServerException("Missing company name")))
              _ <- tradingDetailsService.getTradingDetailsViewModel(profile.registrationId)
            } yield BadRequest(view(errors, companyName)),
          success => {
            val (hasName, name) = success
            tradingDetailsService.saveTradingName(profile.registrationId, hasName, name).flatMap { _ =>
              vatRegistrationService.partyType.map {
                case NonUkNonEstablished => Redirect(controllers.registration.returns.routes.ZeroRatedSuppliesResolverController.resolve())
                case _ => Redirect(controllers.registration.business.routes.ApplyForEoriController.show())
              }
            }
          }
        )
  }

}
