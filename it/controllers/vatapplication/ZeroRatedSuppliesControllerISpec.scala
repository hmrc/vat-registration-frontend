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
import models.error.MissingAnswerException
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class ZeroRatedSuppliesControllerISpec extends ControllerISpec {

  val url: String = controllers.vatapplication.routes.ZeroRatedSuppliesController.show.url

  s"GET $url" must {
    "return an OK if turnoverEstimates are found" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(VatApplication(zeroRatedSupplies = Some(10000.53), appliedForExemption = None)))
        .registrationApi.getSection[VatApplication](Some(VatApplication(zeroRatedSupplies = Some(10000.53), turnoverEstimate = Some(10000.53))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return an OK if turnoverEstimates are found and there is data to prepop" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(VatApplication(zeroRatedSupplies = Some(10000.53), turnoverEstimate = Some(10000.53))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "tredirect to the missing answer page if turnoverEstimates aren't found" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get)

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
    }
  }

  s"POST $url" must {
    "redirect to Sell Or Move Northern Ireland Protocol page if turnoverEstimates exists and form has no errors" in new Setup {
      val vatApplication: VatApplication = fullVatApplication.copy(turnoverEstimate = Some(testTurnover))
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection[VatApplication](vatApplication.copy(zeroRatedSupplies = Some(10000.53), appliedForExemption = None))
        .registrationApi.getSection[VatApplication](Some(vatApplication.copy(zeroRatedSupplies = Some(10000.53), appliedForExemption = None)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Map(
        "zeroRatedSupplies" -> "10000.53"
      ))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SellOrMoveNipController.show.url)
      }
    }

    "update the page with errors if turnoverEstimates exists and form has errors" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(fullVatApplication.copy(zeroRatedSupplies = Some(BigDecimal(10)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Map(
        "zeroRatedSupplies" -> "text"
      ))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }

    "redirect to the missing answer page if turnoverEstimates doesn't exist" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map("zeroRatedSupplies" -> "10,000.53")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
    }
  }

}
