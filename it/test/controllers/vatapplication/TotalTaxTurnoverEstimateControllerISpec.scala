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

package controllers.vatapplication

import itutil.ControllerISpec
import models.api.vatapplication.VatApplication
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class TotalTaxTurnoverEstimateControllerISpec extends ControllerISpec {

  val url: String = controllers.vatapplication.routes.TotalTaxTurnoverEstimateController.show.url

  s"GET $url" must {
    "return an OK" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return an OK if there is data to prepop" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(VatApplication(turnoverEstimate = Some(testTurnover))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        val doc = Jsoup.parse(result.body)

        result.status mustBe OK
        doc.getElementById("value").hasAttr("checked")
      }
    }
  }

  s"POST $url" must {
    "redirect to sell-or-move-ni page if the form has no errors" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection[VatApplication](fullVatApplication.copy(acceptTurnOverEstimate = Some(true)))
        .registrationApi.getSection[VatApplication](Some(fullVatApplication.copy(acceptTurnOverEstimate = Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).post(Map(
        "value" -> "true"
      ))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SellOrMoveNipController.show.url)
      }
    }

    "update the page with errors if the form has errors" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).post(Map(
        "value" -> ""
      ))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }
  }

}
