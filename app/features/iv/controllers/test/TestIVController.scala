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

package controllers.test

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.test.TestIVForm
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, SessionProfile}

import scala.concurrent.Future

class TestIVController @Inject()(ds: CommonPlayDependencies)
  extends VatRegistrationController(ds) with CommonService with SessionProfile {

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          Future.successful(Ok(features.iv.views.html.test.testIVResponse(TestIVForm.form)))
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          TestIVForm.form.bindFromRequest().fold(
            badForm =>
              Future.successful(BadRequest(features.iv.views.html.test.testIVResponse(badForm))),
            success =>
              Future.successful(Ok(s"Test IV Response redirect somewhere with journeyId: ${success.journeyId} and result: ${success.ivResult}"))
          )
        }
  }
}
