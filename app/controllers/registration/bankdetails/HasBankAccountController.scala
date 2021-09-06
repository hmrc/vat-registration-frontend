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

package controllers.registration.bankdetails

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.HasCompanyBankAccountForm.{form => hasBankAccountForm}
import models.api.NETP
import play.api.mvc.{Action, AnyContent}
import services.{BankAccountDetailsService, VatRegistrationService}
import views.html.has_company_bank_account

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HasBankAccountController @Inject()(val authConnector: AuthClientConnector,
                                         val bankAccountDetailsService: BankAccountDetailsService,
                                         val keystoreConnector: KeystoreConnector,
                                         val vatRegistrationService: VatRegistrationService,
                                         view: has_company_bank_account)
                                        (implicit appConfig: FrontendAppConfig,
                                         val executionContext: ExecutionContext,
                                         baseControllerComponents: BaseControllerComponents) extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request => implicit profile =>
        vatRegistrationService.partyType.flatMap {
          case NETP => Future.successful(Redirect(controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show()))
          case _ => for {
            bankDetails <- bankAccountDetailsService.fetchBankAccountDetails
            filledForm = bankDetails.map(_.isProvided).fold(hasBankAccountForm)(hasBankAccountForm.fill)
          } yield Ok(view(filledForm))
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request => implicit profile =>
      hasBankAccountForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        hasBankAccount =>
          bankAccountDetailsService.saveHasCompanyBankAccount(hasBankAccount) flatMap { _ =>
            vatRegistrationService.partyType.map {
              case NETP =>
                Redirect(routes.OverseasBankAccountController.show())
              case _ if hasBankAccount =>
                Redirect(routes.UkBankAccountDetailsController.show())
              case _ =>
                Redirect(routes.NoUKBankAccountController.show())
            }
          }
      )
  }

}
