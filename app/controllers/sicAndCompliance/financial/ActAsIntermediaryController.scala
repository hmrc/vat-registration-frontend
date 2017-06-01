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
import models.ElementPath
import models.view.sicAndCompliance.financial.ActAsIntermediary
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService}


class ActAsIntermediaryController @Inject()(ds: CommonPlayDependencies)
                                           (implicit s4LService: S4LService, vrs: RegistrationService)
  extends VatRegistrationController(ds) {

  import cats.syntax.flatMap._

  val form: Form[ActAsIntermediary] = ActAsIntermediaryForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel2[ActAsIntermediary].fold(form)(form.fill)
      .map(f => Ok(views.html.pages.sicAndCompliance.financial.act_as_intermediary(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.sicAndCompliance.financial.act_as_intermediary(badForm)).pure,
      data => s4LService.save(data).map(_ => data.yesNo).ifM(
        vrs.deleteElements(ElementPath.finCompElementPaths).map(_ =>
          controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show()),
        controllers.sicAndCompliance.financial.routes.ChargeFeesController.show().pure
      ).map(Redirect)))

}


