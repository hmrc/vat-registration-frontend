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

package controllers.returns

import itutil.ControllerISpec
import models.api.returns.Returns
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class TurnoverEstimateControllerISpec extends ControllerISpec {

  val url: String = controllers.returns.routes.TurnoverEstimateController.show.url

  s"GET $url" must {
    "return an OK" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns())

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return an OK if there is data to prepop" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(Some(testTurnover)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        val doc = Jsoup.parse(result.body)

        result.status mustBe OK
        doc.select("input[id=turnoverEstimate]").`val`() mustBe testTurnover.toString
      }
    }
  }

  s"POST $url" must {
    "redirect to Zero Rated Supplies resolver if the form has no errors" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns())
        .s4lContainer[Returns].isUpdatedWith(Returns(Some(10000.54)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Json.obj(
        "turnoverEstimate" -> "10,000.535"
      ))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.ZeroRatedSuppliesResolverController.resolve.url)
      }
    }

    "update the page with errors if the form has errors" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns())

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Json.obj(
        "turnoverEstimate" -> "text"
      ))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }
  }

}
