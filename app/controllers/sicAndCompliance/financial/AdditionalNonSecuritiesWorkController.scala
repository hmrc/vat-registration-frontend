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
import forms.sicAndCompliance.financial.AdditionalNonSecuritiesWorkForm
import models.ElementPath
import models.view.sicAndCompliance.financial.AdditionalNonSecuritiesWork
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService}


class AdditionalNonSecuritiesWorkController @Inject()(ds: CommonPlayDependencies)
                                                     (implicit s4LService: S4LService, vrs: RegistrationService)
  extends ComplianceExitController(ds) {

  val form = AdditionalNonSecuritiesWorkForm.form

  import cats.syntax.flatMap._

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[AdditionalNonSecuritiesWork]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.sicAndCompliance.financial.additional_non_securities_work(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.sicAndCompliance.financial.additional_non_securities_work(badForm)).pure,
      view => save(view).map(_ => view.yesNo).ifM(
        ifTrue = submitAndExit(ElementPath.finCompElementPaths.drop(2)),
        ifFalse = Redirect(controllers.sicAndCompliance.financial.routes.DiscretionaryInvestmentManagementServicesController.show()).pure
      )))

}
