/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}

import connectors.KeystoreConnect
import play.api.mvc._
import services.SessionProfile
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.VATRegFeatureSwitches

import scala.concurrent.Future

@Singleton
class TwirlViewController @Inject()(ds: CommonPlayDependencies,
                                    vatRegFeatureSwitch: VATRegFeatureSwitches,
                                    val authConnector: AuthConnector,
                                    val keystoreConnector: KeystoreConnect) extends VatRegistrationController(ds) with SessionProfile {

  def useEligibilityFrontend: Boolean = !vatRegFeatureSwitch.disableEligibilityFrontend.enabled

  def renderViewAuthorised(viewName: String): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(getEligibilityUrl())
        }
  }

  def getEligibilityUrl()(implicit request: Request[AnyContent]): Result =
    if(useEligibilityFrontend) {
      val url = conf.getString("microservice.services.vat-registration-eligibility-frontend.entry-url").get
      Redirect(Call("GET", url))
    } else {
      Ok(views.html.pages.vatEligibility.use_this_service())
    }
}
