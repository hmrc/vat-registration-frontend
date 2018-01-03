/*
 * Copyright 2018 HM Revenue & Customs
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

package features.bankAccountDetails

import javax.inject.Inject

import config.FrontendAuthConnector
import connectors.KeystoreConnect
import controllers.VatRegistrationControllerNoAux
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{BankAccountDetailsService, SessionProfile}
import forms.{EnterBankAccountDetailsForm, HasCompanyBankAccountForm}
import models.{BankAccount, BankAccountDetails}
import play.api.data.Form

import scala.concurrent.Future

class BankAccountDetailsControllerImpl @Inject()(val messagesApi: MessagesApi,
                                                 val authConnector: FrontendAuthConnector,
                                                 val bankAccountDetailsService: BankAccountDetailsService,
                                                 val keystoreConnector: KeystoreConnect
                                                ) extends BankAccountDetailsController {
}

trait BankAccountDetailsController extends VatRegistrationControllerNoAux with SessionProfile {

  val bankAccountDetailsService: BankAccountDetailsService

  private val hasCompanyBankAccountForm: Form[Boolean] = HasCompanyBankAccountForm.form
  private val enterBankAccountDetailsForm: Form[BankAccountDetails] = EnterBankAccountDetailsForm.form

  val showHasCompanyBankAccountView: Action[AnyContent] = authorisedWithCurrentProfile {
    implicit user =>
      implicit request =>
        implicit profile =>
          ivPassedCheck {
            bankAccountDetailsService.fetchBankAccountDetails map { details =>
              val form: Form[Boolean] = details match {
                case Some(BankAccount(hasBankAccount, _)) => hasCompanyBankAccountForm.fill(hasBankAccount)
                case None                                 => hasCompanyBankAccountForm
              }
              Ok(views.html.has_company_bank_account(form))
            }
          }
  }

  val submitHasCompanyBankAccount: Action[AnyContent] = authorisedWithCurrentProfile {
    implicit user => implicit request => implicit profile =>
      ivPassedCheck {
        hasCompanyBankAccountForm.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.has_company_bank_account(errors))),
          hasBankAccount => bankAccountDetailsService.saveHasCompanyBankAccount(hasBankAccount) map { _ =>
            if(hasBankAccount){
              Redirect(routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails())
            } else {
              Redirect(controllers.frs.routes.JoinFrsController.show())
            }
          }
        )
      }
  }

  val showEnterCompanyBankAccountDetails: Action[AnyContent] = authorisedWithCurrentProfile {
    implicit user => implicit request => implicit profile =>
      ivPassedCheck{
        bankAccountDetailsService.fetchBankAccountDetails map { account =>
          val form: Form[BankAccountDetails] = account match {
            case Some(BankAccount(_, Some(details))) => enterBankAccountDetailsForm.fill(details)
            case _                                   => enterBankAccountDetailsForm
          }
          Ok(views.html.enter_company_bank_account_details(form))
        }
      }
  }

  val submitEnterCompanyBankAccountDetails: Action[AnyContent] = authorisedWithCurrentProfile {
    implicit user => implicit request => implicit profile =>
      ivPassedCheck{
        enterBankAccountDetailsForm.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.enter_company_bank_account_details(errors))),
          accountDetails => bankAccountDetailsService.saveEnteredBankAccountDetails(accountDetails) map { accountDetailsValid =>
            if(accountDetailsValid){
              Redirect(controllers.frs.routes.JoinFrsController.show())
            } else {
              val invalidDetails = EnterBankAccountDetailsForm.formWithInvalidAccountReputation.fill(accountDetails)
              Ok(views.html.enter_company_bank_account_details(invalidDetails))
            }
          }
        )
      }
  }
}