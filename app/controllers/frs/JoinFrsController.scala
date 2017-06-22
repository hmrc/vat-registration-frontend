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

package controllers.frs

import javax.inject.Inject

import cats.syntax.FlatMapSyntax
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
import models.view.frs.JoinFrsView
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}


class JoinFrsController @Inject()(ds: CommonPlayDependencies, formFactory: YesOrNoFormFactory)
                                 (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with FlatMapSyntax {

  val form = formFactory.form("joinFrs")("frs.join")

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[JoinFrsView]().map(vm => YesOrNoAnswer(vm.selection)).fold(form)(form.fill)
      .map(f => Ok(views.html.pages.frs.frs_join(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.frs.frs_join(badForm)).pure,
      goodForm => save(JoinFrsView(goodForm.answer)).map(_ =>
        Redirect(if (goodForm.answer) {
          controllers.frs.routes.AnnualCostsInclusiveController.show()
        } else {
          controllers.routes.SummaryController.show()
        }))))

}
