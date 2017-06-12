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
import forms.sicAndCompliance.labour.WorkersForm
import models.view.sicAndCompliance.labour.Workers
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}


class WorkersController @Inject()(ds: CommonPlayDependencies)
                                 (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends ComplianceExitController(ds, vrs) {

  import cats.syntax.flatMap._

  val form = WorkersForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[Workers]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.sicAndCompliance.labour.workers(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.sicAndCompliance.labour.workers(badForm)).pure,
      goodForm => save(goodForm).map(_ => goodForm.numberOfWorkers >= 8).ifM(
        ifTrue = controllers.sicAndCompliance.labour.routes.TemporaryContractsController.show().pure,
        ifFalse = submitAndExit.pure).map(Redirect)))

}
