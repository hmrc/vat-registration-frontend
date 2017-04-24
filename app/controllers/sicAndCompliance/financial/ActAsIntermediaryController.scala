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

package controllers.sicAndCompliance.financial

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.sicAndCompliance.financial.ActAsIntermediaryForm
import models.{ElementPath, FinChargeFeesPath}
import models.view.sicAndCompliance.financial.ActAsIntermediary
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService}

import scala.concurrent.Future


class ActAsIntermediaryController @Inject()(ds: CommonPlayDependencies)
                                           (implicit s4LService: S4LService, vrs: RegistrationService) extends VatRegistrationController(ds) {
  import cats.instances.future._

  val form: Form[ActAsIntermediary] = ActAsIntermediaryForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[ActAsIntermediary].fold(form)(form.fill)
      .map(f => Ok(views.html.pages.sicAndCompliance.financial.act_as_intermediary(f)))
  )

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    ActAsIntermediaryForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.sicAndCompliance.financial.act_as_intermediary(formWithErrors)))
      }, {
        data: ActAsIntermediary => {
          s4LService.saveForm[ActAsIntermediary](data) flatMap {  _ =>
            if(!data.yesNo) {
              Future.successful(Redirect(controllers.sicAndCompliance.financial.routes.ChargeFeesController.show()))
            }else{
              vrs.deleteElements(ElementPath.finCompElementPaths).map { _ =>
                Redirect(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show()) }
            }
          }
        }
      })
  )

}


