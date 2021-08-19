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
import models.api.NETP
import models.{BankAccount, BankAccountDetails}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{BankAccountDetailsService, SessionProfile, VatRegistrationService}
import views.html.{enter_company_bank_account_details, has_company_bank_account}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BankAccountDetailsController @Inject()(val authConnector: AuthClientConnector,
                                             val bankAccountDetailsService: BankAccountDetailsService,
                                             val keystoreConnector: KeystoreConnector,
                                             val vatRegistrationService: VatRegistrationService,
                                             bankAccountPage: enter_company_bank_account_details,
                                             hasBankAccountPage: has_company_bank_account)
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
            case Some(BankAccount(hasBankAccount, _, _)) => hasCompanyBankAccountForm.fill(hasBankAccount)
            case None => hasCompanyBankAccountForm
          }
          Ok(hasBankAccountPage(form))
        }
  }

  val submitHasCompanyBankAccount: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        hasCompanyBankAccountForm.bindFromRequest.fold(
          errors => Future.successful(BadRequest(hasBankAccountPage(errors))),
          hasBankAccount => bankAccountDetailsService.saveHasCompanyBankAccount(hasBankAccount) map { _ =>
            vatRegistrationService.partyType.flatMap {
              case partyType@(NETP) => Future.successful(Redirect(routes.OverseasBankAccountController.showOverseasBankAccountView()))
              case partyType@(anything else) => Future.successful(Redirect(routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails()))
              case _ => Future.successful(Redirect(routes.NoUKBankAccountController.showNoUKBankAccountView()))
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
          Ok(bankAccountPage(form))
        }
  }

  val submitEnterCompanyBankAccountDetails: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        enterBankAccountDetailsForm.bindFromRequest.fold(
          errors => Future.successful(BadRequest(bankAccountPage(errors))),
          accountDetails => bankAccountDetailsService.saveEnteredBankAccountDetails(accountDetails) map { accountDetailsValid =>
            if (accountDetailsValid) {
              Redirect(controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show())
            } else {
              val invalidDetails = EnterBankAccountDetailsForm.formWithInvalidAccountReputation.fill(accountDetails)
              Ok(bankAccountPage(invalidDetails))
            }
          }
        )
  }
}
