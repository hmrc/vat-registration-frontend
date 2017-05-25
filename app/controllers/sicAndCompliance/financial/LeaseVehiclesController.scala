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
import forms.sicAndCompliance.financial.LeaseVehiclesForm
import models.ElementPath
import models.view.sicAndCompliance.financial.LeaseVehicles
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService}

import scala.concurrent.Future


class LeaseVehiclesController @Inject()(ds: CommonPlayDependencies)
                                       (implicit s4LService: S4LService, vrs: RegistrationService) extends VatRegistrationController(ds) {

  import cats.instances.future._

  val form: Form[LeaseVehicles] = LeaseVehiclesForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel2[LeaseVehicles].fold(form)(form.fill)
      .map(f => Ok(views.html.pages.sicAndCompliance.financial.lease_vehicles(f)))
  )

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.sicAndCompliance.financial.lease_vehicles(formWithErrors)))
      }, {
        data: LeaseVehicles => {
          s4LService.save[LeaseVehicles](data) flatMap { _ =>
            if (!data.yesNo) {
              Future.successful(Redirect(controllers.sicAndCompliance.financial.routes.InvestmentFundManagementController.show()))
            } else {
              vrs.deleteElements(ElementPath.finCompElementPaths.drop(4)).map { _ =>
                Redirect(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show()) }
            }
          }
        }
      }
    )
  )

}

