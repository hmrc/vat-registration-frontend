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

package controllers.vatapplication

import itutil.ControllerISpec
import models.api.vatapplication.{OverseasCompliance, VatApplication}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class SendEUGoodsControllerISpec extends ControllerISpec {

  lazy val url: String = routes.SendEUGoodsController.show.url
  val testOverseasCompliance: OverseasCompliance = OverseasCompliance(Some(true), None)

  s"GET $url" must {
    "Return OK when there is no value for 'goodsToEu' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(overseasCompliance = Some(testOverseasCompliance)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "Return OK with prepop when there is a value for 'goodsToEu' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(overseasCompliance = Some(testOverseasCompliance.copy(goodsToEu = Some(true)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "Yes"
      }
    }
  }

  s"POST $url" must {
    "redirect to the storing goods page when the answer is yes" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(overseasCompliance = Some(testOverseasCompliance)))
        .s4lContainer[VatApplication].isUpdatedWith(VatApplication(overseasCompliance = Some(testOverseasCompliance.copy(goodsToEu = Some(true)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.StoringGoodsController.show.url)
      }
    }

    "redirect to the storing goods page when the answer is no" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(overseasCompliance = Some(testOverseasCompliance)))
        .s4lContainer[VatApplication].isUpdatedWith(VatApplication(overseasCompliance = Some(testOverseasCompliance.copy(goodsToEu = Some(false)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Json.obj("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.StoringGoodsController.show.url)
      }
    }
  }

}
