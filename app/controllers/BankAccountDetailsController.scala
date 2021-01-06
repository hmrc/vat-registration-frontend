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

package controllers

import _root_.connectors.KeystoreConnector
import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import forms.{EnterBankAccountDetailsForm, HasCompanyBankAccountForm}
import javax.inject.{Inject, Singleton}
import models.{BankAccount, BankAccountDetails}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{BankAccountDetailsService, SessionProfile}
import controllers.NoUKBankAccountController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BankAccountDetailsController @Inject()(val authConnector: AuthClientConnector,
                                             val bankAccountDetailsService: BankAccountDetailsService,
                                             val keystoreConnector: KeystoreConnector)
                                            (implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  private val hasCompanyBankAccountForm: Form[Boolean] = HasCompanyBankAccountForm.form
  private val enterBankAccountDetailsForm: Form[BankAccountDetails] = EnterBankAccountDetailsForm.form

  val showHasCompanyBankAccountView: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        bankAccountDetailsService.fetchBankAccountDetails map { details =>
          val form: Form[Boolean] = details match {
            case Some(BankAccount(hasBankAccount, _, None)) => hasCompanyBankAccountForm.fill(hasBankAccount)
            case None => hasCompanyBankAccountForm
          }
          Ok(views.html.has_company_bank_account(form))
        }
  }

  val submitHasCompanyBankAccount: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        hasCompanyBankAccountForm.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.has_company_bank_account(errors))),
          hasBankAccount => bankAccountDetailsService.saveHasCompanyBankAccount(hasBankAccount) map { _ =>
            if (hasBankAccount) {
              Redirect(routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails())
            } else {
              Redirect(routes.NoUKBankAccountController.showNoUKBankAccountView())
            }
          }
        )
  }

  val showEnterCompanyBankAccountDetails: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        bankAccountDetailsService.fetchBankAccountDetails map { account =>
          val form: Form[BankAccountDetails] = account match {
            case Some(BankAccount(_, Some(details), None)) => enterBankAccountDetailsForm.fill(details)
            case _ => enterBankAccountDetailsForm
          }
          Ok(views.html.enter_company_bank_account_details(form))
        }
  }

  val submitEnterCompanyBankAccountDetails: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        enterBankAccountDetailsForm.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.enter_company_bank_account_details(errors))),
          accountDetails => bankAccountDetailsService.saveEnteredBankAccountDetails(accountDetails) map { accountDetailsValid =>
            if (accountDetailsValid) {
              Redirect(controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show())
            } else {
              val invalidDetails = EnterBankAccountDetailsForm.formWithInvalidAccountReputation.fill(accountDetails)
              Ok(views.html.enter_company_bank_account_details(invalidDetails))
            }
          }
        )
  }
}
