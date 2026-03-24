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
import forms.HasCompanyBankAccountForm.{form => hasBankAccountForm}
import models.api.{Individual, NonUkNonEstablished}
import play.api.mvc.{Action, AnyContent}
import services.{BankAccountDetailsService, LockService, SessionService, VatRegistrationService}
import views.html.bankdetails.{CanProvideBankAccountView, HasCompanyBankAccountView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HasBankAccountController @Inject() (val authConnector: AuthClientConnector,
                                          val bankAccountDetailsService: BankAccountDetailsService,
                                          val sessionService: SessionService,
                                          val vatRegistrationService: VatRegistrationService,
                                          val lockService: LockService,

                                          oldView: HasCompanyBankAccountView,
                                          newView: CanProvideBankAccountView)(implicit
    appConfig: FrontendAppConfig,
    val executionContext: ExecutionContext,
    baseControllerComponents: BaseControllerComponents)
    extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    vatRegistrationService.getEligibilitySubmissionData.map(data => (data.partyType, data.fixedEstablishmentInManOrUk)).flatMap {
      case (Individual | NonUkNonEstablished, false) =>
        Future.successful(Redirect(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show))
      case _ =>
        lockService.isBarsLocked(profile.registrationId).flatMap {
          case true => Future.successful(Redirect(controllers.errors.routes.BankDetailsLockoutController.show))
          case false =>
            for {
              bankDetails <- bankAccountDetailsService.getBankAccount
              filledForm = bankDetails.map(_.isProvided).fold(hasBankAccountForm)(hasBankAccountForm.fill)
            } yield
              if (isEnabled(UseNewBarsVerify)) Ok(newView(filledForm))
              else Ok(oldView(filledForm))
        }
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    hasBankAccountForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          if (isEnabled(UseNewBarsVerify)) Future.successful(BadRequest(newView(formWithErrors)))
          else Future.successful(BadRequest(oldView(formWithErrors))),
        hasBankAccount =>
          bankAccountDetailsService.saveHasCompanyBankAccount(hasBankAccount).map { _ =>
            (hasBankAccount, isEnabled(UseNewBarsVerify)) match {
              case (true, false) => Redirect(routes.UkBankAccountDetailsController.show)
              case (true, true)  => Redirect(routes.ChooseAccountTypeController.show)
              case (false, _)    => Redirect(routes.NoUKBankAccountController.show)
            }
          }
      )
  }

}
