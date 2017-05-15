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
import forms.vatLodgingOfficer.OfficerDateOfBirthForm
import models.view.vatLodgingOfficer.OfficerDateOfBirthView
import play.api.mvc._
import services.{CommonService, S4LService, VatRegistrationService}

class OfficerDateOfBirthController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4l: S4LService,
                                             vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with CommonService {

  import cats.instances.future._
  import cats.syntax.applicative._

  val form = OfficerDateOfBirthForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    for {
      res <- viewModel[OfficerDateOfBirthView].fold(form)(form.fill)
    } yield Ok(views.html.pages.vatLodgingOfficer.officer_dob(res))

  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    form.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.pages.vatLodgingOfficer.officer_dob(formWithErrors)).pure,
      data => {
        s4l.saveForm[OfficerDateOfBirthView](data) map { _ =>
          Redirect(controllers.vatLodgingOfficer.routes.OfficerNinoController.show())
        }
      }
    )
  })

}
