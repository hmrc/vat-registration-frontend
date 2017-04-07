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

package controllers.sicAndCompliance

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.sicAndCompliance.BusinessActivityDescriptionForm
import models.view.sicAndCompliance.BusinessActivityDescription
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future


class BusinessActivityDescriptionController @Inject()(ds: CommonPlayDependencies)
                                                     (implicit s4l: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {

  import cats.instances.future._

  val form: Form[BusinessActivityDescription] = BusinessActivityDescriptionForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[BusinessActivityDescription].fold(form)(form.fill)
      .map(f => Ok(views.html.pages.sicAndCompliance.business_activity_description(f)))
  )

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      (formWithErrors) => Future.successful(BadRequest(views.html.pages.sicAndCompliance.business_activity_description(formWithErrors))),
      (data: BusinessActivityDescription) => s4l.saveForm[BusinessActivityDescription](data.copy(description = data.description.trim))
        .map(_ => Redirect(controllers.test.routes.SicStubController.show()))
    )
  )

}
