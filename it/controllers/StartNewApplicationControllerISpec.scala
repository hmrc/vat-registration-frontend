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

package controllers

import itutil.ControllerISpec
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class StartNewApplicationControllerISpec extends ControllerISpec {

  val showUrl = routes.StartNewApplicationController.show.url
  val submitUrl = routes.StartNewApplicationController.submit.url

  s"GET $showUrl" must {
    "return OK if the user is authorised" in new Setup {
      given
        .user.isAuthorised
        .vatScheme.contains(fullVatScheme)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(showUrl).get)

      res.status mustBe OK
    }
  }

  s"POST $submitUrl" must {
    "redirect to continue journey url if the answer is No" in new Setup {
      given
        .user.isAuthorised

      val res = await(buildClient(submitUrl).post(Json.obj("value" -> false)))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.WelcomeController.continueJourney.url)
    }

    "redirect to start new journey url if the answer is Yes" in new Setup {
      given
        .user.isAuthorised
        .trafficManagement.isCleared

      val res = await(buildClient(submitUrl).post(Json.obj("value" -> true)))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.WelcomeController.startNewJourney.url)
    }
    "Throw an exception if the answer is Yes but the clear TM API call fails" in new Setup {
      given
        .user.isAuthorised
        .trafficManagement.failsToClear

      val res = await(buildClient(submitUrl).post(Json.obj("value" -> true)))

      res.status mustBe INTERNAL_SERVER_ERROR
    }
  }

}
