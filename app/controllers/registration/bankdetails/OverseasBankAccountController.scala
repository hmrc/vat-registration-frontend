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

package controllers.registration.bankdetails

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.OverseasBankAccountForm
import play.api.mvc.{Action, AnyContent}
import services.{BankAccountDetailsService, SessionProfile, SessionService}
import views.html.overseas_bank_account

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OverseasBankAccountController @Inject()(overseasBankAccountView: overseas_bank_account,
                                              val authConnector: AuthClientConnector,
                                              val sessionService: SessionService,
                                              val bankAccountDetailsService: BankAccountDetailsService)
                                             (implicit appConfig: FrontendAppConfig,
                                              val executionContext: ExecutionContext,
                                              baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          optBankDetails <- bankAccountDetailsService.fetchBankAccountDetails
          optOverseasDetails = optBankDetails.flatMap(_.overseasDetails)
          form = optOverseasDetails.fold(OverseasBankAccountForm.form)(details => OverseasBankAccountForm.form.fill(details))
        } yield Ok(overseasBankAccountView(form))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        OverseasBankAccountForm.form.bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(overseasBankAccountView(formWithErrors))),
          accountDetails => bankAccountDetailsService.saveEnteredOverseasBankAccountDetails(accountDetails).flatMap(_ =>
            Future.successful(Redirect(controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show)))
        )
  }

}
