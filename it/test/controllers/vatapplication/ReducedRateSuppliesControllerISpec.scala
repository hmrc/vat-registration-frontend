/*
 * Copyright 2025 HM Revenue & Customs
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

class ReducedRateSuppliesControllerISpec extends ControllerISpec {

  val url: String = controllers.vatapplication.routes.ReducedRateSuppliesController.show.url

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
        .registrationApi.getSection[VatApplication](Some(VatApplication(reducedRateSupplies = Some(testTurnover))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        val doc = Jsoup.parse(result.body)

        result.status mustBe OK
        doc.select("input[id=reducedRateSupplies]").`val`() mustBe testTurnover.toString
      }
    }
  }

  s"POST $url" must {
    "redirect to Turnover Estimate resolver if the form has no errors" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection[VatApplication](VatApplication(reducedRateSupplies = Some(10000)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).post(Map(
        "reducedRateSupplies" -> "10000"
      ))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.ZeroRatedSuppliesController.show.url)
      }
    }

    "update the page with errors if the form has errors" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).post(Map(
        "reducedRateSupplies" -> "text"
      ))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }
  }

}
