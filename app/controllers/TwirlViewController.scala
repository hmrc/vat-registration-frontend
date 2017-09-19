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

package controllers

import javax.inject.Inject

import connectors.KeystoreConnector
import play.api.mvc.{Action, AnyContent, Result}
import services.SessionProfile

import scala.concurrent.Future

class TwirlViewController @Inject()(ds: CommonPlayDependencies) extends VatRegistrationController(ds) with SessionProfile {

  val keystoreConnector: KeystoreConnector = KeystoreConnector

  def renderViewAuthorised(viewName: String): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(
            Some(viewName).collect {
              case "use-this-service" => views.html.pages.vatEligibility.use_this_service()
            }.fold[Result](NotFound)(Ok(_))
          )
        }
  }
}
