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
import forms.EnterBankAccountDetailsForm
import forms.EnterBankAccountDetailsForm.{form => enterBankAccountDetailsForm}
import models.{BankAccount, BankAccountDetails}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{BankAccountDetailsService, VatRegistrationService}
import views.html.enter_company_bank_account_details

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UkBankAccountDetailsController @Inject()(val authConnector: AuthClientConnector,
                                                val bankAccountDetailsService: BankAccountDetailsService,
                                                val keystoreConnector: KeystoreConnector,
                                                val vatRegistrationService: VatRegistrationService,
                                                view: enter_company_bank_account_details)
                                               (implicit appConfig: FrontendAppConfig,
                                                val executionContext: ExecutionContext,
                                                baseControllerComponents: BaseControllerComponents) extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request => implicit profile =>
      for {
        bankDetails <- bankAccountDetailsService.fetchBankAccountDetails
        filledForm = bankDetails.flatMap(_.details).fold(enterBankAccountDetailsForm)(enterBankAccountDetailsForm.fill)
      } yield Ok(view(filledForm))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request => implicit profile =>
      enterBankAccountDetailsForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        accountDetails =>
          bankAccountDetailsService.saveEnteredBankAccountDetails(accountDetails) map { accountDetailsValid =>
          if (accountDetailsValid) {
            Redirect(controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show)
          } else {
            val invalidDetails = EnterBankAccountDetailsForm.formWithInvalidAccountReputation.fill(accountDetails)
            BadRequest(view(invalidDetails))
          }
        }
      )
  }

}
