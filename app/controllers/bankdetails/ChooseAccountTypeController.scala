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
import featuretoggle.FeatureSwitch.UseNewBarsVerify
import featuretoggle.FeatureToggleSupport.isEnabled
import forms.ChooseAccountTypeForm
import play.api.mvc.{Action, AnyContent}
import services.{BankAccountDetailsService, LockService, SessionService}
import views.html.bankdetails.ChooseAccountTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChooseAccountTypeController @Inject() (val authConnector: AuthClientConnector,
                                             val bankAccountDetailsService: BankAccountDetailsService,
                                             val sessionService: SessionService,
                                             val lockService: LockService,
                                             view: ChooseAccountTypeView)(implicit
    appConfig: FrontendAppConfig,
    val executionContext: ExecutionContext,
    baseControllerComponents: BaseControllerComponents)
    extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    if (isEnabled(UseNewBarsVerify)) {
      lockService.isBarsLocked(profile.registrationId).flatMap {
        case true => Future.successful(Redirect(controllers.errors.routes.BankDetailsLockoutController.show))
        case false =>
          bankAccountDetailsService.getBankAccount.map { bankDetails =>
            val filledForm = bankDetails
              .flatMap(_.bankAccountType)
              .fold(ChooseAccountTypeForm.form)(ChooseAccountTypeForm.form.fill)
            Ok(view(filledForm))
          }
      }
    } else {
      Future.successful(Redirect(routes.HasBankAccountController.show))
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    ChooseAccountTypeForm.form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        bankAccountType =>
          bankAccountDetailsService.saveBankAccountType(bankAccountType).map { _ =>
            Redirect(routes.UkBankAccountDetailsController.show)
          }
      )
  }

}
