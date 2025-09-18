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

package controllers.business

import itutil.ControllerISpec
import models.Business
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class BusinessActivityDescriptionControllerISpec extends ControllerISpec {

  "GET /what-company-does" must {
    "return OK" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(controllers.business.routes.BusinessActivityDescriptionController.show.url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  "POST /what-company-does" must {
    "redirect to ICL on submit" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection[Business](businessDetails.copy(businessDescription = Some("foo")))
        .registrationApi.getSection[Business](Some(businessDetails.copy(businessDescription = Some("foo"))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(controllers.business.routes.BusinessActivityDescriptionController.submit.url)
        .post(Map("description" -> Seq("foo")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.sicandcompliance.routes.SicController.startICLJourney.url)
      }
    }
  }

}
