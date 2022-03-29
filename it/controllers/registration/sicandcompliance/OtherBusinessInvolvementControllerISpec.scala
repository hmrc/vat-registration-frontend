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

package controllers.registration.sicandcompliance

import itutil.ControllerISpec
import models.SicAndCompliance
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class OtherBusinessInvolvementControllerISpec extends ControllerISpec {

  val url: String = routes.OtherBusinessInvolvementController.show.url
  s"GET $url" must {
    "return OK" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[SicAndCompliance].isEmpty
        .vatScheme.doesNotHave("sicAndComp")

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
        .s4lContainer[SicAndCompliance].contains(SicAndCompliance(otherBusinessInvolvement = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }
  }

  s"POST $url" must {
    "redirect to next stage in process if no other business involvement" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[SicAndCompliance].isEmpty
        .s4lContainer[SicAndCompliance].isUpdatedWith(SicAndCompliance(otherBusinessInvolvement = Some(true)))
        .vatScheme.doesNotHave("sicAndComp")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Json.obj("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.routes.TradingNameResolverController.resolve.url)
      }
    }
  }

  s"POST $url" must {
    "redirect to other business involvement workflow" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[SicAndCompliance].isEmpty
        .s4lContainer[SicAndCompliance].isUpdatedWith(SicAndCompliance(otherBusinessInvolvement = Some(true)))
        .vatScheme.doesNotHave("sicAndComp")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.registration.otherbusinessinvolvements.routes.OtherBusinessNameController.show(1).url)
      }
    }
  }

  s"POST $url" must {
    "fail with bad request for submission without a selection" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[SicAndCompliance].isEmpty
        .s4lContainer[SicAndCompliance].isUpdatedWith(SicAndCompliance(otherBusinessInvolvement = Some(true)))
        .vatScheme.doesNotHave("sicAndComp")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Json.obj())

      whenReady(res) { _.status mustBe BAD_REQUEST }
    }
  }
}