/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.flatratescheme

import itutil.ControllerISpec
import models.{FlatRateScheme, S4LKey}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class EstimateTotalSalesControllerISpec extends ControllerISpec {

  implicit val s4lFrsKey: S4LKey[FlatRateScheme] = FlatRateScheme.s4lKey

  val url: String = controllers.flatratescheme.routes.EstimateTotalSalesController.estimateTotalSales.url

  val testTotalSales = 123456
  val frsData: FlatRateScheme = FlatRateScheme(
    joinFrs = Some(true),
    overBusinessGoods = Some(true),
    estimateTotalSales = Some(testTotalSales),
    overBusinessGoodsPercent = None,
    useThisRate = None,
    frsStart = None,
    categoryOfBusiness = None,
    percent = None
  )
  val fullFrsData: FlatRateScheme = FlatRateScheme(
    joinFrs = Some(true),
    overBusinessGoods = Some(true),
    estimateTotalSales = Some(testTotalSales),
    overBusinessGoodsPercent = Some(true),
    useThisRate = Some(true),
    frsStart = Some(LocalDate.now()),
    categoryOfBusiness = Some("testCategory"),
    percent = Some(15)
  )

  s"GET $url" must {
    "return OK with prepop" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[FlatRateScheme](Some(fullFrsData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("totalSalesEstimate").attr("value") mustBe testTotalSales.toString
      }
    }

    "return OK without prepop" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[FlatRateScheme].clearedByKey
        .registrationApi.getSection[FlatRateScheme](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("totalSalesEstimate").attr("value") mustBe ""
      }
    }
  }

  s"POST $url" must {
    "redirect to the next FRS page when the user submits a valid estimate" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[FlatRateScheme].clearedByKey
        .registrationApi.getSection[FlatRateScheme](Some(frsData.copy(estimateTotalSales = None)))
        .registrationApi.replaceSection[FlatRateScheme](frsData)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Map("totalSalesEstimate" -> testTotalSales.toString))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.flatratescheme.routes.FlatRateController.annualCostsLimitedPage.url)
      }
    }

    "update the page with errors when the user submits an invalid estimate" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val invalidEstimates = Seq("", "a", "0", "999999999999999")

      invalidEstimates.map { estimate =>
        val res: Future[WSResponse] = buildClient(url).post(Map("totalSalesEstimate" -> estimate))

        whenReady(res) { result =>
          result.status mustBe BAD_REQUEST
        }
      }
    }
  }
}