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

package controllers.registration.business

import itutil.ControllerISpec
import models.{TradingDetails, TradingNameView}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class ApplyForEoriControllerISpec extends ControllerISpec {

  s"GET ${routes.ApplyForEoriController.show.url}" must {
    "return OK when trading details aren't stored" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[TradingDetails].isEmpty
        .vatScheme.doesNotHave("trading-details")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/apply-for-eori").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
    "return OK when trading details are stored in S4L" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[TradingDetails].contains(TradingDetails(euGoods = Some(true)))
        .vatScheme.doesNotHave("trading-details")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/apply-for-eori").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
    "return OK when trading details are stored in the backend" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[TradingDetails].isEmpty
        .vatScheme.has(
        "trading-details",
        Json.toJson(TradingDetails(tradingNameView = Some(TradingNameView(yesNo = false, None)), Some(true)))(TradingDetails.writes)
      )

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/apply-for-eori").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }

  s"POST ${routes.ApplyForEoriController.submit.url}" must {
    "redirect to the next page" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[TradingDetails].contains(TradingDetails(tradingNameView = Some(TradingNameView(yesNo = false, None))))
        .s4lContainer[TradingDetails].clearedByKey
        .vatScheme.doesNotHave("trading-details")
        .vatScheme.isUpdatedWith(
        "tradingDetails",
        Json.toJson(TradingDetails(tradingNameView = Some(TradingNameView(yesNo = false, None)), Some(true)))
      )

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/apply-for-eori").post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.registration.returns.routes.ZeroRatedSuppliesResolverController.resolve.url)
      }
    }
  }

}
