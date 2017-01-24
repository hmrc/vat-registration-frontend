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
import forms.vatDetails.StartDateForm
import models.view.StartDate
import play.api.mvc._
import services.S4LService

import scala.concurrent.Future

class StartDateController @Inject()(s4LService: S4LService, ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    s4LService.fetchAndGet[StartDate](CacheKeys.StartDate.toString) map { date =>
      val form = StartDateForm.form.fill(date.getOrElse(StartDate.empty))
      Ok(views.html.pages.start_date(form))
    }
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    StartDateForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.start_date(formWithErrors)))
      }, {
        data: StartDate => {
          // Save to S4L
          s4LService.saveForm[StartDate](CacheKeys.StartDate.toString, data) map { _ =>
            Redirect(controllers.userJourney.routes.SummaryController.show())
          }
        }
      })
  })

}
