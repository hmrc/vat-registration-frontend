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

package controllers.vatapplication

import itutil.ControllerISpec
import models.api.vatapplication.VatApplication
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class ImportsOrExportsControllerISpec extends ControllerISpec {

  s"GET ${routes.ImportsOrExportsController.show.url}" must {
    "return OK when trading details aren't stored" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[VatApplication].isEmpty
        .registrationApi.getSection[VatApplication](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/imports-or-exports").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
    "return OK when vat application is stored in S4L" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(tradeVatGoodsOutsideUk = Some(false)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/imports-or-exports").get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "No"
      }
    }
    "return OK when trading details are stored in the backend" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[VatApplication].isEmpty
        .registrationApi.getSection[VatApplication](Some(VatApplication(tradeVatGoodsOutsideUk = Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/imports-or-exports").get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "Yes"
      }
    }
  }

  s"POST ${routes.ImportsOrExportsController.submit.url}" must {
    "redirect to EORI when yes is selected" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[VatApplication].isEmpty
        .registrationApi.getSection[VatApplication](None)
        .s4lContainer[VatApplication].isUpdatedWith(VatApplication(tradeVatGoodsOutsideUk = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/imports-or-exports").post(Map("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.vatapplication.routes.ApplyForEoriController.show.url)
      }
    }

    "redirect to Turnover when no is selected" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[VatApplication].isEmpty
        .registrationApi.getSection[VatApplication](None)
        .s4lContainer[VatApplication].isUpdatedWith(VatApplication(tradeVatGoodsOutsideUk = Some(false)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/imports-or-exports").post(Map("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.vatapplication.routes.TurnoverEstimateController.show.url)
      }
    }

    "return BAD_REQUEST if no option is selected" in new Setup {
      given.user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient("/imports-or-exports").post("")

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }
  }
}
