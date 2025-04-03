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

class StandardRateSuppliesControllerISpec extends ControllerISpec {

  val url: String = controllers.vatapplication.routes.StandardRateSuppliesController.show.url

  s"GET $url" must {
    "return an OK if standardRatedSupplies are found" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(VatApplication(standardRateSupplies = Some(10000.53))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return an OK and prepop when turnoverEstimates are found but standardRatedSupplies not available" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

  }

  s"POST $url" must {

    "highlight the errors if the form has errors" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(fullVatApplication.copy(standardRateSupplies = Some(100))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).post(Map(
        "standardRatedSupplies" -> "9999999999999999"
      ))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
        Jsoup.parse(result.body).select("main p").get(2).text() contains
            "Give an estimate that is less than or equal to £999,999,999,999,999"
      }
    }

    "update the page with errors if form has errors" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(fullVatApplication.copy(standardRateSupplies = Some(BigDecimal(10)))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).post(Map(
        "standardRatedSupplies" -> "text"
      ))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }

    "redirect to the next page if form has no errors" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection[VatApplication](VatApplication(standardRateSupplies = Some(10000)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).post(Map(
        "standardRateSupplies" -> "10000"
      ))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.ReducedRateSuppliesController.show.url)
      }
    }
  }

}
