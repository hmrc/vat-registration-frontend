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
import forms.vatDetails.StartDateForm
import models.view.StartDate
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future

class StartDateController @Inject()(ds: CommonPlayDependencies)
                                   (implicit s4LService: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[StartDate]() map { vm =>
      Ok(views.html.pages.start_date(StartDateForm.form.fill(vm)))
    }
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    StartDateForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.start_date(formWithErrors)))
      }, {
        data: StartDate => {
          s4LService.saveForm[StartDate](data) flatMap { _ =>
            if (StartDate.SPECIFIC_DATE != data.dateType) {
              s4LService.saveForm[StartDate](StartDate())
                .map { _ => Redirect(controllers.userJourney.routes.TradingNameController.show()) }
            } else {
              Future.successful(Redirect(controllers.userJourney.routes.TradingNameController.show()))
            }
          }
        }
      })
  })
}
