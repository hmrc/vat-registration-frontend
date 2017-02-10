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
import models.view.Summary
import play.api.mvc._
import services.VatRegistrationService

import scala.concurrent.Future

class SummaryController @Inject()(vatRegistrationService: VatRegistrationService, ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async {
    implicit user => implicit request =>
      // call vatRegistrationService.persistVatScheme here

      vatRegistrationService.getRegistrationSummary map {
        model: Summary => Ok(views.html.pages.summary(model))
      }
  }
}
