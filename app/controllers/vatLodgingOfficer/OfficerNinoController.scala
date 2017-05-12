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

package controllers.vatLodgingOfficer

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatLodgingOfficer.OfficerNinoForm
import models.view.vatLodgingOfficer.OfficerNinoView
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, S4LService, VatRegistrationService}

class OfficerNinoController @Inject()(ds: CommonPlayDependencies)
                                     (implicit s4l: S4LService,
                                      vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with CommonService {

  import cats.instances.future._
  import cats.syntax.applicative._
  import cats.syntax.flatMap._

  val form = OfficerNinoForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    for {
      res <- viewModel[OfficerNinoView].fold(form)(form.fill)
    } yield Ok(views.html.pages.vatLodgingOfficer.officer_nino(res))

  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    form.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.pages.vatLodgingOfficer.officer_nino(formWithErrors)).pure,
      data => {
        viewModel[VoluntaryRegistration]
          .map(_ == VoluntaryRegistration.yes).getOrElse(true).ifM(
          s4l.saveForm[OfficerNinoView](data) map { _ =>
            controllers.vatTradingDetails.vatChoice.routes.StartDateController.show()
          },
          s4l.saveForm[OfficerNinoView](data) map { _ =>
            controllers.vatTradingDetails.vatChoice.routes.MandatoryStartDateController.show()
          }
        ).map(Redirect)
      }
    )
  })

}
