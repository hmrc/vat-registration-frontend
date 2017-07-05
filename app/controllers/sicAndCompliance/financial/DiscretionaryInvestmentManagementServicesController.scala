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

import controllers.CommonPlayDependencies
import controllers.sicAndCompliance.ComplianceExitController
import forms.sicAndCompliance.financial.DiscretionaryInvestmentManagementServicesForm
import models.ElementPath
import models.view.sicAndCompliance.financial.DiscretionaryInvestmentManagementServices
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService}


class DiscretionaryInvestmentManagementServicesController @Inject()(ds: CommonPlayDependencies)
                                                                   (implicit s4LService: S4LService, vrs: RegistrationService)
  extends ComplianceExitController(ds) {

  import cats.syntax.flatMap._

  val form: Form[DiscretionaryInvestmentManagementServices] = DiscretionaryInvestmentManagementServicesForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[DiscretionaryInvestmentManagementServices]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.sicAndCompliance.financial.discretionary_investment_management_services(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.sicAndCompliance.financial.discretionary_investment_management_services(badForm)).pure,
      view => save(view).map(_ => view.yesNo).ifM(
        ifTrue = submitAndExit(ElementPath.finCompElementPaths.drop(3)),
        ifFalse = Redirect(controllers.sicAndCompliance.financial.routes.LeaseVehiclesController.show()).pure
      )))

}
