/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class SaveAndRetrieveControllerISpec extends ControllerISpec {

  val url = s"/schemes/save-for-later"

  "GET /schemes/save-for-later" must {
    "return SEE_OTHER" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.ApplicationProgressSavedController.show.url)
    }
  }

}
