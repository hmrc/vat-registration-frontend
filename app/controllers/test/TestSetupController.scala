/*
 * Copyright 2020 HM Revenue & Customs
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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.test.TestVatRegistrationConnector
import connectors.{KeystoreConnector, S4LConnector}
import controllers.BaseController
import forms.test.TestSetupEligibilityForm
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{S4LService, SessionProfile, TrafficManagementService}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestSetupController @Inject()(val s4LService: S4LService,
                                    val s4lConnector: S4LConnector,
                                    val authConnector: AuthClientConnector,
                                    val keystoreConnector: KeystoreConnector,
                                    val testVatRegConnector: TestVatRegistrationConnector)
                                   (implicit appConfig: FrontendAppConfig,
                                    val executionContext: ExecutionContext,
                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def showEligibility: Action[AnyContent] = isAuthenticatedWithProfile(checkTrafficManagement = false) {
    implicit request =>
      implicit profile =>
        Future.successful(Ok(views.html.pages.test.test_setup_eligibility(TestSetupEligibilityForm.form)))
  }

  def submitEligibility: Action[AnyContent] = isAuthenticatedWithProfile(checkTrafficManagement = false) {
    implicit request =>
      implicit profile =>
        TestSetupEligibilityForm.form.bindFromRequest().fold(
          badForm => {
            Future.successful(BadRequest(views.html.pages.test.test_setup_eligibility(badForm)))
          }, { data: String =>
            testVatRegConnector.updateEligibilityData(Json.parse(data)) map (_ => Ok("Eligibility updated"))
          }
        )
  }
}
