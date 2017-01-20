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

package controllers.userJourney

import javax.inject.Inject

import auth.VatTaxRegime
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.StartDateForm
import models.StartDateModel
import play.api.mvc._

import scala.concurrent.Future

class StartDateController @Inject()(ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised(implicit user => implicit request => {
    val form = StartDateForm.form.fill(StartDateModel.empty)
    Ok(views.html.pages.start_date(form))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    StartDateForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.start_date(formWithErrors)))
      }, {
        _ => {
          Future.successful(Redirect(controllers.userJourney.routes.TaxableTurnoverController.show()))
        }
      })
  })
}
