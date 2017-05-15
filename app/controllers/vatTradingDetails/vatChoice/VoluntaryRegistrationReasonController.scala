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
import forms.vatTradingDetails.vatChoice.VoluntaryRegistrationReasonForm
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason
import play.api.mvc._
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future

class VoluntaryRegistrationReasonController @Inject()(ds: CommonPlayDependencies)
                                                     (implicit s4l: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) {

  import cats.instances.future._
  import cats.syntax.applicative._
  import cats.syntax.flatMap._

  val form = VoluntaryRegistrationReasonForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[VoluntaryRegistrationReason].fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatTradingDetails.vatChoice.voluntary_registration_reason(f)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    form.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(views.html.pages.vatTradingDetails.vatChoice.voluntary_registration_reason(formWithErrors))),
      (data: VoluntaryRegistrationReason) =>
        (VoluntaryRegistrationReason.NEITHER == data.reason).pure.ifM(
          s4l.clear().flatMap(_ => vrs.deleteVatScheme())
            .map(_ => controllers.routes.WelcomeController.show())
          ,
          s4l.saveForm[VoluntaryRegistrationReason](data)
            .map(_ => controllers.vatLodgingOfficer.routes.OfficerDateOfBirthController.show())
        ).map(Redirect)
    )
  })

}
