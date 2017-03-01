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
import forms.vatDetails.{CompanyBankAccountForm, VoluntaryRegistrationForm}
import models.ApiModelTransformer
import models.view.{CompanyBankAccount, VoluntaryRegistration}
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future

class CompanyBankAccountController @Inject()(s4LService: S4LService, vatRegistrationService: VatRegistrationService,
                                             ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    s4LService.fetchAndGet[CompanyBankAccount](CacheKeys.CompanyBankAccount.toString) flatMap {
      case Some(viewModel) => Future.successful(viewModel)
      case None => vatRegistrationService.getVatScheme() map ApiModelTransformer[CompanyBankAccount].toViewModel
    } map { viewModel =>
      val form = CompanyBankAccountForm.form.fill(viewModel)
      Ok(views.html.pages.company_bank_account(form))
    }
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    CompanyBankAccountForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.company_bank_account(formWithErrors)))
      }, {

        data: CompanyBankAccount => {
          s4LService.saveForm[CompanyBankAccount](CacheKeys.CompanyBankAccount.toString, data) flatMap { _ =>
            if (CompanyBankAccount.COMPANY_BANK_ACCOUNT_YES == data.yesNo) {
              Future.successful(Redirect(controllers.userJourney.routes.EstimateVatTurnoverController.show()))
            } else {
              Future.successful(Redirect(controllers.userJourney.routes.EstimateVatTurnoverController.show()))
            }
          }
        }
      })
  })

}
