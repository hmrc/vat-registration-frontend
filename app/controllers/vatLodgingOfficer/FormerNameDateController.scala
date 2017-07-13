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

import cats.syntax.FlatMapSyntax
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatLodgingOfficer.FormerNameDateForm
import models.view.vatLodgingOfficer.{FormerNameDateView, FormerNameView}
import play.api.data.Form
import play.api.mvc._
import services.{CommonService, S4LService, VatRegistrationService}
class FormerNameDateController @Inject()(ds: CommonPlayDependencies)
                                        (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with FlatMapSyntax with CommonService {

  val form: Form[FormerNameDateView] = FormerNameDateForm.form

   def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>

    for {
      formerName <- viewModel[FormerNameView]().fold("")(view => view.formerName.getOrElse(""))
      res <- viewModel[FormerNameDateView]().fold(form)(form.fill)
    } yield Ok(views.html.pages.vatLodgingOfficer.former_name_date(res, formerName))
  )


  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm =>
        for {
          formerName <- viewModel[FormerNameView]().fold("")(view => view.formerName.getOrElse(""))
          res <- viewModel[FormerNameDateView]().fold(form)(form.fill)
        } yield BadRequest(views.html.pages.vatLodgingOfficer.former_name_date(badForm, formerName))
      ,
      data => save(data) map (_ => Redirect(controllers.vatLodgingOfficer.routes.OfficerDateOfBirthController.show()))))

}
