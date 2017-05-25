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

package controllers.vatTradingDetails.vatChoice

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatTradingDetails.vatChoice.VoluntaryRegistrationForm
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

class VoluntaryRegistrationController @Inject()(ds: CommonPlayDependencies)
                                               (implicit s4l: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) {

  import cats.instances.future._
  import cats.syntax.applicative._
  import cats.syntax.flatMap._

  val form = VoluntaryRegistrationForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel2[VoluntaryRegistration].fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatTradingDetails.vatChoice.voluntary_registration(f)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    VoluntaryRegistrationForm.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.pages.vatTradingDetails.vatChoice.voluntary_registration(formWithErrors)).pure
      }, {
        data: VoluntaryRegistration => {
          (VoluntaryRegistration.REGISTER_YES == data.yesNo).pure.ifM(
            s4l.save[VoluntaryRegistration](data)
              .map(_ => controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationReasonController.show())
            ,
            s4l.clear().flatMap(_ => vrs.deleteVatScheme())
              .map(_ => controllers.routes.WelcomeController.show())
          ).map(Redirect)
        }
      }
    )
  })

}
