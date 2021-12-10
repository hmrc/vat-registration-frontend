/*
 * Copyright 2021 HM Revenue & Customs
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

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.test.TestVatRegistrationConnector
import controllers.BaseController
import play.api.libs.json.Json
import play.api.mvc._
import services.{SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import views.html.test.vat_submission_json

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RetrieveVatSubmissionController @Inject()(val authConnector: AuthConnector,
                                                val sessionService: SessionService,
                                                testVatRegistrationConnector: TestVatRegistrationConnector,
                                                view: vat_submission_json)
                                               (implicit val executionContext: ExecutionContext,
                                                bcc: BaseControllerComponents,
                                                appConfig: FrontendAppConfig) extends BaseController with SessionProfile with AuthorisedFunctions {

  def retrieveSubmissionJson: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request =>
      implicit profile =>
        testVatRegistrationConnector.retrieveVatSubmission(profile.registrationId) map (json => Ok(view(Json.prettyPrint(json))))
  }
}
