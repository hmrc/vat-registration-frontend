/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.userJourney

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatDetails.CompanyBankAccountForm
import models.view.CompanyBankAccount
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future

class CompanyBankAccountController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4l: S4LService, vatRegService: VatRegistrationService)
  extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[CompanyBankAccount](CompanyBankAccount()) map { vm =>
      Ok(views.html.pages.company_bank_account(CompanyBankAccountForm.form.fill(vm)))
    }
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    CompanyBankAccountForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.company_bank_account(formWithErrors)))
      }, {
        data: CompanyBankAccount => {
          s4l.saveForm[CompanyBankAccount](data) map { _ =>
            if (CompanyBankAccount.COMPANY_BANK_ACCOUNT_YES == data.yesNo) {
              Redirect(controllers.userJourney.routes.BankDetailsController.show())
            } else {
              Redirect(controllers.userJourney.routes.EstimateVatTurnoverController.show())
            }
          }
        }
      })
  })

}
