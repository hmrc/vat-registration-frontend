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

package controllers.vatFinancials.vatBankAccount

import javax.inject.Inject

import cats.Show
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatFinancials.vatBankAccount.{CompanyBankAccountDetailsForm, SortCode}
import models.view.vatFinancials.vatBankAccount.CompanyBankAccountDetails
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

class CompanyBankAccountDetailsController @Inject()(ds: CommonPlayDependencies)
                                                   (implicit s4l: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) {

  val form = CompanyBankAccountDetailsForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[CompanyBankAccountDetails]().map(vm =>
      CompanyBankAccountDetailsForm(
        accountName = vm.accountName.trim,
        accountNumber = "",
        sortCode = SortCode.parse(vm.sortCode).getOrElse(SortCode("", "", ""))))
      .fold(form)(form.fill)
      .map(frm => Ok(views.html.pages.vatFinancials.vatBankAccount.company_bank_account_details(frm))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatFinancials.vatBankAccount.company_bank_account_details(badForm)).pure,
      view => save(
        CompanyBankAccountDetails(
          accountName = view.accountName.trim,
          accountNumber = view.accountNumber,
          sortCode = Show[SortCode].show(view.sortCode)))
        .map(_ => Redirect(controllers.vatFinancials.routes.EstimateVatTurnoverController.show()))))

}
