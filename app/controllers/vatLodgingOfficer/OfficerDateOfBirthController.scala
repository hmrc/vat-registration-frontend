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

import cats.data.OptionT
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatLodgingOfficer.OfficerDateOfBirthForm
import models.ModelKeys._
import models.external.Officer
import models.view.vatLodgingOfficer.OfficerDateOfBirthView
import play.api.mvc._
import services.{CommonService, S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

class OfficerDateOfBirthController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4l: S4LService,
                                             vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with CommonService {

  val form = OfficerDateOfBirthForm.form

  private def fetchOfficer()(implicit headerCarrier: HeaderCarrier) =
    OptionT(keystoreConnector.fetchAndGet[Officer](REGISTERING_OFFICER_KEY))

  def show: Action[AnyContent] = authorised.async(body = implicit user => implicit request =>
    for {
      officer <- fetchOfficer().getOrElse(Officer.empty) // TODO: getOrElse(Officer.empty) ??
      res <- viewModel[OfficerDateOfBirthView]().
          fold(officer.dateOfBirth.fold(form)(dob =>
            form.fill(OfficerDateOfBirthView(dob)))) {
        view =>
          view.officerName match {
            case Some(name) if name == officer.name => form.fill(view)
            case _ => officer.dateOfBirth.fold(form)(dob => form.fill(OfficerDateOfBirthView(dob)))
          }
      }
    } yield Ok(views.html.pages.vatLodgingOfficer.officer_dob(res)))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatLodgingOfficer.officer_dob(badForm)).pure,
      data  => for {
        officer <- fetchOfficer().getOrElse(Officer.empty)
        _ <- save(data.copy(officerName = Some(officer.name)))
      } yield Redirect(controllers.vatLodgingOfficer.routes.OfficerNinoController.show())))

}
