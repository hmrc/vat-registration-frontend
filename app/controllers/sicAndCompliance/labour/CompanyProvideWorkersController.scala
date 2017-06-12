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

package controllers.sicAndCompliance.labour

import javax.inject.Inject

import controllers.CommonPlayDependencies
import controllers.sicAndCompliance.ComplianceExitController
import forms.sicAndCompliance.labour.CompanyProvideWorkersForm
import models.view.sicAndCompliance.labour.CompanyProvideWorkers
import models.{CulturalCompliancePath, FinancialCompliancePath, S4LVatSicAndCompliance}
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}


class CompanyProvideWorkersController @Inject()(ds: CommonPlayDependencies)
                                               (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends ComplianceExitController(ds, vrs) {

  val form = CompanyProvideWorkersForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[CompanyProvideWorkers]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.sicAndCompliance.labour.company_provide_workers(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.sicAndCompliance.labour.company_provide_workers(badForm)).pure,
      data => for {
          _ <- save(S4LVatSicAndCompliance())
          _ <- save(data)
          _ <- vrs.deleteElements(List(CulturalCompliancePath, FinancialCompliancePath))
          route =
            if (CompanyProvideWorkers.PROVIDE_WORKERS_YES == data.yesNo) {
              controllers.sicAndCompliance.labour.routes.WorkersController.show()
            } else { submitAndExit }
        } yield Redirect(route)))

}
