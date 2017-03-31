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

package controllers.userJourney.vatChoice

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatDetails.vatTradingDetails.StartDateFormFactory
import models.view.vatTradingDetails.StartDateView
import play.api.data.Form
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future

class StartDateController @Inject()(startDateFormFactory: StartDateFormFactory, ds: CommonPlayDependencies)
                                   (implicit s4LService: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {

  import cats.instances.future._

  val form: Form[StartDateView] = startDateFormFactory.form()

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[StartDateView].fold(form)(form.fill).map(f => Ok(views.html.pages.start_date(f)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    startDateFormFactory.form().bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(views.html.pages.start_date(formWithErrors))),
      (data: StartDateView) => s4LService.saveForm(data).map { _ =>
        Redirect(controllers.userJourney.vatTradingDetails.routes.TradingNameController.show())
      }
    )
  })
}
