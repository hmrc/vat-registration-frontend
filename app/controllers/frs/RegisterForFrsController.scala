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
import forms.genericForms.YesOrNoFormFactory
import models.view.frs.RegisterForFrsView
import models.{S4LFlatRateScheme, VatFrsStartDate, VatFrsWhenToJoin}
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}


class RegisterForFrsController @Inject()(ds: CommonPlayDependencies, formFactory: YesOrNoFormFactory)
                                        (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with FlatMapSyntax {

  val form = formFactory.form("registerForFrs")("frs.registerFor")

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    Ok(views.html.pages.frs.frs_register_for(form)).pure)

    def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      form.bindFromRequest().fold(
        badForm => BadRequest(views.html.pages.frs.frs_register_for(badForm)).pure,
        view =>
          save(RegisterForFrsView(view.answer)).map(_ => view.answer).ifM(
            ifTrue = controllers.frs.routes.FrsStartDateController.show().pure,
            ifFalse = for {
              frs <- s4LService.fetchAndGet[S4LFlatRateScheme]()
              _ <- s4LService.save(frs.getOrElse(S4LFlatRateScheme()).copy(frsStartDate = None))
              _ <- vrs.submitVatFlatRateScheme()
              _ <- vrs.deleteElements(List(VatFrsWhenToJoin, VatFrsStartDate))
            } yield controllers.routes.SummaryController.show()
          ).map(Redirect)))
}

