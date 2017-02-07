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

import controllers.{CommonPlayDependencies, VatRegistrationController}
import enums.CacheKeys
import forms.vatDetails.VoluntaryRegistrationForm
import models.view.VoluntaryRegistration
import play.api.mvc._
import services.S4LService

import scala.concurrent.Future

  class VoluntaryRegistrationController @Inject()(s4LService: S4LService, ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    s4LService.fetchAndGet[VoluntaryRegistration](CacheKeys.VoluntaryRegistration.toString) map { date =>
      val form = VoluntaryRegistrationForm.form.fill(date.getOrElse(VoluntaryRegistration.empty))
      Ok(views.html.pages.voluntary_registration(form))
    }
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    VoluntaryRegistrationForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.voluntary_registration(formWithErrors)))
      }, {

        data: VoluntaryRegistration => {
          s4LService.saveForm[VoluntaryRegistration](CacheKeys.VoluntaryRegistration.toString, data) map { _ =>
              if (VoluntaryRegistration.REGISTER_YES == data.yesNo) {
                Redirect(controllers.userJourney.routes.StartDateController.show())
              } else {
                Redirect(controllers.userJourney.routes.StartDateController.show())
              }
          }
        }
      })
  })

}
