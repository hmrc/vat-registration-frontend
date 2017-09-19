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
import services.{S4LService, SessionProfile, VatRegistrationService}
import views.html.pages.application_submission_confirmation

class ApplicationSubmissionController @Inject()(ds: CommonPlayDependencies)
                                               (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with SessionProfile {

  val keystoreConnector: KeystoreConnector = KeystoreConnector

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          for {
            vs           <- vrs.getVatScheme()
            Some(ackRef) <- vrs.getAckRef(profile.registrationId).value
          } yield Ok(application_submission_confirmation(ackRef, vs.financials))
        }
  }

}
