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

import connectors.KeystoreConnector
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatLodgingOfficer.FormerNameForm
import models.view.vatLodgingOfficer.FormerNameView
import play.api.data.Form
import play.api.mvc._
import services.{S4LService, SessionProfile, VatRegistrationService}

class FormerNameController @Inject()(ds: CommonPlayDependencies)
                                    (implicit s4LService: S4LService, vatRegistrationService: VatRegistrationService)
  extends VatRegistrationController(ds) with SessionProfile {

  import cats.syntax.flatMap._

  val keystoreConnector: KeystoreConnector = KeystoreConnector

  val form: Form[FormerNameView] = FormerNameForm.form

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          viewModel[FormerNameView]().fold(form)(form.fill)
            .map(f => Ok(views.html.pages.vatLodgingOfficer.former_name(f)))
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          form.bindFromRequest().fold(
            badForm => BadRequest(views.html.pages.vatLodgingOfficer.former_name(badForm)).pure,
            data => data.yesNo.pure.ifM(
              ifTrue = save(data).map(_ => Redirect(controllers.vatLodgingOfficer.routes.FormerNameDateController.show())),
              ifFalse = save(data).map(_ => Redirect(controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.show()))))
        }
  }

}
