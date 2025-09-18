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

package controllers.transactor

import featuretoggle.FeatureSwitch.StubEmailVerification
import itutil.ControllerISpec
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class TransactorEmailAddressVerifiedControllerISpec extends ControllerISpec {

  "GET /transactor-details/email-address-verified" must {
    "show the view correctly" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient("/your-email-address-verified").get())

      res.status mustBe OK
    }
  }

  "POST /transactor-details/email-address-verified" must {
    "redirect to Task list page" in new Setup {
      disable(StubEmailVerification)

      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient("/your-email-address-verified").post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }
  }

}
