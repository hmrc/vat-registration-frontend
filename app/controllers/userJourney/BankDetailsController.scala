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
import enums.CacheKeys
import forms.vatDetails.{BankDetailsForm, SortCode}
import models.ApiModelTransformer
import models.view.CompanyBankAccountDetails
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future

class BankDetailsController @Inject()(s4LService: S4LService, vatRegistrationService: VatRegistrationService,
                                      ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    s4LService.fetchAndGet[CompanyBankAccountDetails](CacheKeys.CompanyBankAccountDetails.toString) flatMap {
      case Some(viewModel) => Future.successful(viewModel)
      case None => vatRegistrationService.getVatScheme() map ApiModelTransformer[CompanyBankAccountDetails].toViewModel
    } map { viewModel =>
      val form = BankDetailsForm.form.fill(
        BankDetailsForm(
          accountName = viewModel.accountName,
          accountNumber = viewModel.accountNumber,
          sortCode = SortCode.parse(viewModel.sortCode).getOrElse(SortCode("", "", ""))))
      Ok(views.html.pages.bank_account_details(form))
    }
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    BankDetailsForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.bank_account_details(formWithErrors)))
      }, (form: BankDetailsForm) => {
        s4LService.saveForm[CompanyBankAccountDetails](
          CacheKeys.CompanyBankAccountDetails.toString,
          CompanyBankAccountDetails(
            accountName = form.accountName,
            accountNumber = form.accountNumber,
            sortCode = form.sortCode.toString
          )).map(_ => Redirect(controllers.userJourney.routes.SummaryController.show()))
      })
  })

}


