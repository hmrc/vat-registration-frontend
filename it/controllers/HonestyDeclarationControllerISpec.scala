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

package controllers

import config.FrontendAppConfig
import connectors.RegistrationApiConnector.honestyDeclarationKey
import featuretoggle.FeatureToggleSupport
import itFixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.ApiKey
import play.api.http.HeaderNames
import play.api.http.Status.SEE_OTHER
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class HonestyDeclarationControllerISpec extends ControllerISpec with ITRegistrationFixtures with FeatureToggleSupport {

  val url: String = controllers.routes.HonestyDeclarationController.show.url
  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val userId = "user-id-12345"

  s"GET $url" must {
    "return an OK" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  s"POST $url" must {
    "redirect to Eligibility" in new Setup {
      implicit val key: ApiKey[Boolean] = honestyDeclarationKey
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection(true, testRegId)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(appConfig.eligibilityStartUrl(testRegId))
    }
  }
}
