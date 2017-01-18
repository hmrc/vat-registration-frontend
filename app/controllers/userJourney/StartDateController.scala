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
import forms.StartDateForm
import models.StartDateModel
import play.api.mvc._

import scala.concurrent.Future

class StartDateController @Inject()(ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised(implicit user => implicit request => {
    val form = StartDateForm.form.fill(StartDateModel.empty)
    Ok(views.html.pages.start_date(form))
  })

  val submit = authorised.async {
    implicit user =>
      implicit request => {
        StartDateForm.form.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(views.html.pages.start_date(formWithErrors)))
          }, {
            data => {
              val updatedData = data.dateType match {
                case StartDateModel.WHEN_REGISTERED => data.copy(dateType = StartDateModel.WHEN_REGISTERED, day = None, month = None, year = None)
                case StartDateModel.WHEN_TRADING => data.copy(dateType = StartDateModel.WHEN_TRADING, day = None, month = None, year = None)
              }
              //call to service
              Future.successful(Redirect(routes.TaxableTurnoverController.show()))
            }
          }
        )
      }
  }
}
