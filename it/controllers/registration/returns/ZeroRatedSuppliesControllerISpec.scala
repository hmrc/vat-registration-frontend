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

package controllers.registration.returns

import itutil.ControllerISpec
import models.api.returns.Returns
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class ZeroRatedSuppliesControllerISpec extends ControllerISpec {

  val url: String = controllers.registration.returns.routes.ZeroRatedSuppliesController.show.url

  s"GET $url" must {
    "return an OK if turnoverEstimates are found" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, None))
        .vatScheme.has("turnover-estimates-data", Json.toJson(turnOverEstimates))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return an OK if turnoverEstimates are found and there is data to prepop" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(Some(10000), None, None, None, None))
        .vatScheme.has("turnover-estimates-data", Json.toJson(turnOverEstimates))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return an INTERNAL_SERVER_ERROR if turnoverEstimates aren't found" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, None))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  s"POST $url" must {
    "redirect to charge expectancy if turnoverEstimates exists and form has no errors" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, None))
        .s4lContainer[Returns].isUpdatedWith(Returns(Some(10000.54), None, None, None, None))
        .vatScheme.has("turnover-estimates-data", Json.toJson(turnOverEstimates))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Json.obj(
        "zeroRatedSupplies" -> "10,000.535"
      ))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.returns.routes.ClaimRefundsController.show.url)
      }
    }

    "update the page with errors if turnoverEstimates exists and form has errors" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, None))
        .vatScheme.has("turnover-estimates-data", Json.toJson(turnOverEstimates))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Json.obj(
        "zeroRatedSupplies" -> "text"
      ))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }

    "return an INTERNAL_SERVER_ERROR if turnoverEstimates doesn't exist" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns())

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Json.obj(
        "zeroRatedSupplies" -> "10,000.53"
      ))

      whenReady(res) { result =>
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
