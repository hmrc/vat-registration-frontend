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
import forms.sicAndCompliance.financial.ManageAdditionalFundsForm
import models.view.sicAndCompliance.financial.ManageAdditionalFunds
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService}


class ManageAdditionalFundsController @Inject()(ds: CommonPlayDependencies)
                                               (implicit s4LService: S4LService, vrs: RegistrationService)
  extends ComplianceExitController(ds) {

  val form = ManageAdditionalFundsForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[ManageAdditionalFunds]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.sicAndCompliance.financial.manage_additional_funds(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.sicAndCompliance.financial.manage_additional_funds(badForm)).pure,
<<<<<<< HEAD
      data => save(data).flatMap(_ => submitAndExit(List()))))
=======
      view => save(view).flatMap(_ => submitAndExit(List())).map(Redirect)))
>>>>>>> c6ab0cccd57edee74c840c657fad849bb312383c

}


