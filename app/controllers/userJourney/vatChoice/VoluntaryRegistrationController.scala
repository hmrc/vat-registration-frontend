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
import forms.vatDetails.vatChoice.VoluntaryRegistrationForm
import models.view.vatChoice.VoluntaryRegistration
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future

class VoluntaryRegistrationController @Inject()(ds: CommonPlayDependencies)
                                               (implicit s4LService: S4LService, vatRegistrationService: VatRegistrationService)
  extends VatRegistrationController(ds) {

  import cats.instances.future._

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[VoluntaryRegistration].map { vm =>
      Ok(views.html.pages.voluntary_registration(VoluntaryRegistrationForm.form.fill(vm)))
    }.getOrElse(Ok(views.html.pages.voluntary_registration(VoluntaryRegistrationForm.form)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    VoluntaryRegistrationForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.voluntary_registration(formWithErrors)))
      }, {

        data: VoluntaryRegistration => {
          s4LService.saveForm[VoluntaryRegistration](data) flatMap { _ =>
            if (VoluntaryRegistration.REGISTER_YES == data.yesNo) {
              Future.successful(Redirect(controllers.userJourney.vatChoice.routes.StartDateController.show()))
            } else {
              for {
                _ <- s4LService.clear()
                _ <- vatRegistrationService.deleteVatScheme()
              } yield Redirect(controllers.userJourney.routes.WelcomeController.show())
            }
          }
        }
      })
  })

}
