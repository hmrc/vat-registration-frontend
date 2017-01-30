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
import play.api.mvc._
import services.VatRegistrationService

import scala.concurrent.Future

class SummaryController @Inject()(vatRegistrationService: VatRegistrationService, ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  //access to root of application should by default direct the user to the proper URL for start of VAT registration
  def show: Action[AnyContent] = authorised.async {
    implicit user => implicit request =>
        Future.successful(Ok("hello"))
//      for {
//        oSummaryModel <- vatRegistrationService.getRegistrationSummary
//      } yield oSummaryModel match {
//        case Some(summaryModel) => Ok(views.html.pages.summary(summaryModel))
//        case None => InternalServerError(views.html.pages.error.restart())
//      }
  }

}
