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
import play.api.mvc._
import services.SessionProfile
import utils.VATRegFeatureSwitch

import scala.concurrent.Future

class TwirlViewController @Inject()(ds: CommonPlayDependencies, vatRegFeatureSwitch: VATRegFeatureSwitch)
  extends VatRegistrationController(ds) with SessionProfile {

  val keystoreConnector: KeystoreConnector = KeystoreConnector
  def useEligibilityFrontend: Boolean = !vatRegFeatureSwitch.disableEligibilityFrontend.enabled

  def renderViewAuthorised(viewName: String): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(
            Some(viewName).collect {
              case "use-this-service" => getEligibilityUrl
            }.fold[Result](NotFound)(result => result)
          )
        }
  }

  def getEligibilityUrl()(implicit request: Request[AnyContent]): Result =
    if(useEligibilityFrontend) {
      val url = conf.getString("microservice.services.vat-registration-eligibility-frontend.entry-url")
        .getOrElse("http://localhost:9894/check-if-you-can-register-for-vat/use-this-service")
      Redirect(Call("GET", url))
    }else{
      Ok(views.html.pages.vatEligibility.use_this_service())
    }
}
