/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.bankdetails

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.BusinessOrPersonalBankAccountForm
import models.bars.BankAccountType
import play.api.mvc.{Action, AnyContent}
import services.{BankAccountDetailsService, SessionService}
import views.html.bankdetails.BusinessOrPersonalBankAccountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessOrPersonalBankAccountController @Inject()(
                                                         val authConnector: AuthClientConnector,
                                                         val bankAccountDetailsService: BankAccountDetailsService,
                                                         val sessionService: SessionService,
                                                         view: BusinessOrPersonalBankAccountView
                                                       )(implicit appConfig: FrontendAppConfig,
                                                         val executionContext: ExecutionContext,
                                                         baseControllerComponents: BaseControllerComponents) extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      bankAccountDetailsService.fetchBankAccountDetails.map { bankDetails =>
        val filledForm = bankDetails.flatMap(_.bankAccountType)
          .fold(BusinessOrPersonalBankAccountForm.form)(BusinessOrPersonalBankAccountForm.form.fill)
        Ok(view(filledForm))
      }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      BusinessOrPersonalBankAccountForm.form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        bankAccountType =>
          bankAccountDetailsService.saveBankAccountType(bankAccountType).map { _ =>
            Redirect(routes.UkBankAccountDetailsController.show)
          }
      )
  }
}