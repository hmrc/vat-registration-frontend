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
import forms.sicAndCompliance.financial.AdviceOrConsultancyForm
import models.view.sicAndCompliance.financial.AdviceOrConsultancy
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService}


class AdviceOrConsultancyController @Inject()(ds: CommonPlayDependencies)
                                             (implicit s4LService: S4LService, vrs: RegistrationService)
  extends VatRegistrationController(ds) {

  val form: Form[AdviceOrConsultancy] = AdviceOrConsultancyForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[AdviceOrConsultancy]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.sicAndCompliance.financial.advice_or_consultancy(f)))
  )

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.sicAndCompliance.financial.advice_or_consultancy(badForm)).pure,
      data => save(data).map(_ => // TODO delete any existing non-financial compliance questions - i.e labour and cultural
        Redirect(controllers.sicAndCompliance.financial.routes.ActAsIntermediaryController.show()))
    )
  )

}


