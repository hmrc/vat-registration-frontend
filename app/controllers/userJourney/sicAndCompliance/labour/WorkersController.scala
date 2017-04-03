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

package controllers.userJourney.sicAndCompliance.labour

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatDetails.sicAndCompliance.cultural.NotForProfitForm
import forms.vatDetails.sicAndCompliance.labour.WorkersForm
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.labour.Workers
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future


class WorkersController @Inject()(ds: CommonPlayDependencies)
                                 (implicit s4LService: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {

  import cats.instances.future._

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[Workers].map { vm =>
      Ok(views.html.pages.sicAndCompliance.labour.workers(WorkersForm.form.fill(vm)))
    }.getOrElse(Ok(views.html.pages.sicAndCompliance.labour.workers(WorkersForm.form)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    WorkersForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.sicAndCompliance.labour.workers(formWithErrors)))
      }, {
        data: Workers => {
          s4LService.saveForm[Workers](data) map { _ =>
            Redirect(controllers.userJourney.vatFinancials.routes.CompanyBankAccountController.show())
          }
        }
      })
  })

}
