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

package controllers.business

import itutil.ControllerISpec
import models.Business
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class LandAndPropertyControllerISpec extends ControllerISpec {

  val url: String = controllers.business.routes.LandAndPropertyController.show.url
  s"GET $url" must {
    "return OK" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].isEmpty
        .registrationApi.getSection[Business](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "return OK when there is an answer to prepop" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].contains(Business(hasLandAndProperty = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }
  }

  s"POST $url" must {
    "redirect to the next page" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].isEmpty
        .s4lContainer[Business].isUpdatedWith(Business(hasLandAndProperty = Some(true)))
        .registrationApi.getSection[Business](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.business.routes.BusinessActivityDescriptionController.show.url)
      }
    }

    "return BAD_REQUEST when no option is selected" in new Setup {
      given.user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post("")

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }
  }

}
